package com.kodgames.game.action.history;

import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.game.service.room.RoomService;
import com.kodgames.game.service.server.ServerService;
import com.kodgames.game.service.security.SecurityService;
import com.kodgames.message.proto.club.ClubProtoBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.game.service.activity.WeiXinShareActivityService;
import com.kodgames.game.service.history.HistoryService;
import com.kodgames.game.service.room.RoomService;
//import com.kodgames.game.service.security.SecurityService;
import com.kodgames.game.service.server.ServerService;
import com.kodgames.message.proto.club.ClubProtoBuf;
import com.kodgames.message.proto.game.GameProtoBuf.BGMatchResultSYN;
import com.kodgames.message.proto.game.GameProtoBuf.PlayerHistoryPROTO;
import xbean.RoleRecord;

@ActionAnnotation(messageClass = BGMatchResultSYN.class, actionClass = BGMatchResultSYNAction.class, serviceClass = HistoryService.class)
public class BGMatchResultSYNAction extends ProtobufMessageHandler<HistoryService, BGMatchResultSYN>
{
	private static final Logger logger = LoggerFactory.getLogger(BGMatchResultSYNAction.class);

	@Override
	public void handleMessage(Connection connection, HistoryService service, BGMatchResultSYN message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);

		WeiXinShareActivityService weiXinShareActivityService = ServiceContainer.getInstance().getPublicService(WeiXinShareActivityService.class);
		
		// 微信分享活动处于开启状态
		if (weiXinShareActivityService.isActive())
		{
			for (PlayerHistoryPROTO proto : message.getPlayerRecordsList())
			{
				int roleId = proto.getRoleId();
				weiXinShareActivityService.rewardPromoterIfFinishTask(roleId);
			}
		}
		
		RoomService roomService = ServiceContainer.getInstance().getPublicService(RoomService.class);
		int roomId = message.getRoomId();
		int clubId = roomService.getRoomClubId(roomId);
		// 保存战绩
		if (clubId == 0) {
			service.saveHistory(message);
		} else {
			service.saveClubHistory(message, clubId);

			// 向club同步一下数据
			ClubProtoBuf.GCLMatchResultSYN.Builder builder = ClubProtoBuf.GCLMatchResultSYN.newBuilder();
			builder.setRoomId(roomId);
			builder.setClubId(clubId);
			message.getPlayerRecordsList().forEach( playerHis -> {
				ClubProtoBuf.ClubPlayerInfoPROTO.Builder pb = ClubProtoBuf.ClubPlayerInfoPROTO.newBuilder();
				pb.setRoleId(playerHis.getRoleId());
				pb.setRoleName(playerHis.getNickname());
				pb.setPosition(playerHis.getPosition());
				pb.setTotalPoint(playerHis.getTotalPoint());

				builder.addPlayers(pb.build());
			});

			Connection clubConnection = ServiceContainer.getInstance().getPublicService(ServerService.class).getClubConnection();
			clubConnection.write(callback, builder.build());
		}

		// 统计游戏局数, 用于判断登录安全组
		SecurityService securityService = ServiceContainer.getInstance().getPublicService(SecurityService.class);
		for (PlayerHistoryPROTO playerProto : message.getPlayerRecordsList()) {
			int rid = playerProto.getRoleId();
			securityService.updateCombatRecords(rid);
		}
	}

	@Override
	public Object getMessageKey(Connection connection, BGMatchResultSYN message)
	{
		return message.getRoomId() == 0 ? connection.getConnectionID() : message.getRoomId();
	}

}
