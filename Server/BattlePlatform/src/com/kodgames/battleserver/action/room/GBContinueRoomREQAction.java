package com.kodgames.battleserver.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.battleserver.service.battle.common.xbean.BattleRoom;
import com.kodgames.battleserver.service.battle.common.xbean.RoomPlayerInfo;
import com.kodgames.battleserver.service.battle.constant.ContinueInfo;
import com.kodgames.battleserver.service.room.RoomService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.message.proto.game.GameProtoBuf.BGContinueRoomRES;
import com.kodgames.message.proto.game.GameProtoBuf.GBContinueRoomREQ;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

@ActionAnnotation(actionClass = GBContinueRoomREQ.class, messageClass = GBContinueRoomREQ.class, serviceClass = RoomService.class)
public class GBContinueRoomREQAction extends ProtobufMessageHandler<RoomService, GBContinueRoomREQ>
{
	final static Logger logger = LoggerFactory.getLogger(GBContinueRoomREQ.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, GBContinueRoomREQ message, int callback)
	{
		logger.info("CreateRoom : The role {} create room {} success!", message.getCreatorId(), message.getNewRoomId());

		int oldRoomId = message.getOldRoomId();
		BattleRoom oldRoomInfo = service.getRoomInfo(oldRoomId);

		BGContinueRoomRES.Builder builder = BGContinueRoomRES.newBuilder();
		int result = service.continueRoom(message.getCreatorId(), message.getNewRoomId(), oldRoomInfo.getRoomType(), oldRoomInfo.getGameplays(), oldRoomInfo.getCountType(), oldRoomInfo.getPayType());
		builder.setResult(result);
		builder.setRoomId(message.getNewRoomId());
		builder.setCreatorId(message.getCreatorId());
		builder.addAllGameplays(oldRoomInfo.getGameplays());

		if (PlatformProtocolsConfig.BG_CONTINUE_ROOM_SUCCESS != result)
		{
			logger.warn("ContinueRoom failed : The role {} create room {} failed result {}", message.getCreatorId(), message.getNewRoomId(), result);
		}

		Connection existedConnection = ConnectionManager.getInstance().getClientVirtualConnection(message.getCreatorId());
		RoomPlayerInfo player = service.getRoomPlayerInfo(oldRoomId, message.getCreatorId());
		service.enterRoom(message.getCreatorId(), message.getNewRoomId(), player.getNickname(), player.getHeadImageUrl(), player.getSex(), existedConnection);

		connection.write(callback, builder.build());

		service.isContinueRoom(oldRoomId, message.getNewRoomId());
		oldRoomInfo.setContinueRoomId(message.getNewRoomId());
		oldRoomInfo.setContinueInfo(ContinueInfo.CONTINUE);
	}

	@Override
	public Object getMessageKey(Connection connection, GBContinueRoomREQ message)
	{
		if (message.getNewRoomId() != 0)
		{
			return message.getNewRoomId();
		}
		else
		{
			logger.error("{} : message key miss roomId -> connectionId={}, message={}", getClass(), connection.getConnectionID(), message);
			return connection.getConnectionID();
		}
	}
}
