package com.kodgames.game.action.history;

import com.kodgames.game.service.room.RoomService;
import com.kodgames.game.service.server.ServerService;
import com.kodgames.message.proto.club.ClubProtoBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.game.common.Constant.ActivityId;
import com.kodgames.game.service.activity.ActivityService;
import com.kodgames.game.service.history.HistoryService;
import com.kodgames.game.service.role.RoleService;
import com.kodgames.message.proto.game.GameProtoBuf.BGFinalMatchResultSYN;
import com.kodgames.message.proto.game.GameProtoBuf.PlayerHistoryPROTO;

@ActionAnnotation(messageClass = BGFinalMatchResultSYN.class, actionClass = BGFinalMatchResultSYNAction.class, serviceClass = HistoryService.class)
public class BGFinalMatchResultSYNAction extends ProtobufMessageHandler<HistoryService, BGFinalMatchResultSYN>
{
	private static final Logger logger = LoggerFactory.getLogger(BGMatchResultSYNAction.class);

	@Override
	public void handleMessage(Connection connection, HistoryService service, BGFinalMatchResultSYN message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);

		// 向club通知结算数据
		RoomService roomService = ServiceContainer.getInstance().getPublicService(RoomService.class);
		int roomId = message.getRoomId();
		int clubId = roomService.getRoomClubId(roomId);
		if (clubId != 0) {
			ClubProtoBuf.GCLFinalMatchResultSYN.Builder builder = ClubProtoBuf.GCLFinalMatchResultSYN.newBuilder();
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

		// 更新活动排名
		ActivityService activityService = ServiceContainer.getInstance().getPublicService(ActivityService.class);
		if (activityService.isRankSupported(message.getRoundType()) && activityService.isActivated(ActivityId.SCORE_RANK.getId()) == true)
		{
			// 如果该房间的玩法规则支持排行榜，且当前处于活动期间，才更新排行榜
			activityService.updateRank(message);
		}
		// 向玩家发出防沉迷警告
		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
		for (PlayerHistoryPROTO playerHistory : message.getPlayerRecordsList())
		{
			int roleId = playerHistory.getRoleId();
			if (roleService.needAlertAddiction(roleId))
			{
				roleService.syncAddictionAlertToPlayer(roleId);
			}
		}
	}

	@Override
	public Object getMessageKey(Connection connection, BGFinalMatchResultSYN message)
	{
		return message.getRoomId() == 0 ? connection.getConnectionID() : message.getRoomId();
	}
}
