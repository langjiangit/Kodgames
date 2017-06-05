package com.kodgames.battleserver.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.battleserver.service.battle.common.xbean.BattleRoom;
import com.kodgames.battleserver.service.battle.common.xbean.RoomPlayerInfo;
import com.kodgames.battleserver.service.room.RoomService;
import com.kodgames.battleserver.service.server.ServerService;
import com.kodgames.corgi.core.constant.GlobalConstants;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.message.proto.game.GameProtoBuf.BGContinueEnterRoomRES;
import com.kodgames.message.proto.game.GameProtoBuf.BGQuitRoomSYN;
import com.kodgames.message.proto.game.GameProtoBuf.GBContinueEnterRoomREQ;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

@ActionAnnotation(actionClass = GBContinueEnterRoomREQAction.class, messageClass = GBContinueEnterRoomREQ.class, serviceClass = RoomService.class)
public class GBContinueEnterRoomREQAction extends ProtobufMessageHandler<RoomService, GBContinueEnterRoomREQ>
{
	final static Logger logger = LoggerFactory.getLogger(GBContinueEnterRoomREQAction.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, GBContinueEnterRoomREQ message, int callback)
	{
		final int roleId = message.getRoleId();
		final int roomId = message.getRoomId();

		logger.info("EnterRoom : The role {} attempt enter room {}", roleId, roomId);

		// 响应Client进入房间请求
		BGContinueEnterRoomRES.Builder bgResbuilder = BGContinueEnterRoomRES.newBuilder();
		int error = service.checkContinueEnterRoom(roleId, roomId);
		if (PlatformProtocolsConfig.BG_CONTINUE_ENTER_ROOM_SUCCESS != error)
		{
			// 通知给game，处理那些game上存在房间记录，但是battle上不存在房间
			// TODO: 其实手动输入输入房间号加入房间的情况，如果房间不存在不需要通知game，但现在不能区分手动输入还是自动加入
			BGQuitRoomSYN.Builder bgQuitbuilder = BGQuitRoomSYN.newBuilder();
			bgQuitbuilder.setRoleId(roleId);
			bgQuitbuilder.setRoomId(roomId);
			ServerService serverService = ServiceContainer.getInstance().getPublicService(ServerService.class);
			Connection gameConnection = serverService.getGameConnection();
			if (null != gameConnection)
				gameConnection.write(GlobalConstants.DEFAULT_CALLBACK, bgQuitbuilder.build());

			// 将错误返回给Game
			bgResbuilder.setResult(error);
			bgResbuilder.setIsHaveBeginFirstGame(false);
			bgResbuilder.setRoomType(0);
			bgResbuilder.setRoundCount(0);
			bgResbuilder.setMaxPlayerCount(0);
			bgResbuilder.setPayType(0);
			bgResbuilder.setRoleId(roleId);
			bgResbuilder.setRoomId(roomId);
			connection.write(callback, bgResbuilder.build());
			logger.info("enterRoom role {} roomId {} code {}", roleId, roomId, error);
			return;
		}

		int oldRoomId = message.getOldRoomId();
		BattleRoom oldRoom = service.getRoomInfo(oldRoomId);
		BattleRoom newRoom = service.getRoomInfo(roomId);
		RoomPlayerInfo playerInfo = service.getRoomPlayerInfo(oldRoomId, roleId);
		Connection existedConnection = ConnectionManager.getInstance().getClientVirtualConnection(roleId);
		BattleRoom room = service.enterRoom(roleId, roomId, playerInfo.getNickname(), playerInfo.getHeadImageUrl(), playerInfo.getSex(), existedConnection);

		// 判断是否已经开始了第一局
		bgResbuilder.setResult(error);
		bgResbuilder.setIsHaveBeginFirstGame(service.isHaveBeginFirstGame(room));
		bgResbuilder.setRoomType(room.getRoomType());
		bgResbuilder.addAllGameplays(room.getGameplays());
		bgResbuilder.setRoundCount(room.getCountType());
		bgResbuilder.setMaxPlayerCount(room.getMaxMemberCount());
		bgResbuilder.setPayType(room.getPayType());
		bgResbuilder.setRoleId(roleId);
		bgResbuilder.setRoomId(roomId);

		// 响应Game
		connection.write(callback, bgResbuilder.build());

		service.syncPlayerContinueEnterRoom(oldRoom, newRoom);
		service.quitRoomSavePlayer(playerInfo, oldRoom);
		service.quitRoom(roleId, oldRoom);
	}

	@Override
	public Object getMessageKey(Connection connection, GBContinueEnterRoomREQ message)
	{
		return message.getRoomId();
	}
}
