package com.kodgames.game.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.game.service.room.RoomService;
import com.kodgames.message.proto.game.GameProtoBuf.BGContinueEnterRoomRES;
import com.kodgames.message.proto.game.GameProtoBuf.GBContinueEnterRoomOverSYN;
import com.kodgames.message.proto.game.GameProtoBuf.GCContinueEnterRoomRES;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

@ActionAnnotation(messageClass = BGContinueEnterRoomRES.class, actionClass = BGContinueEnterRoomRESAction.class, serviceClass = RoomService.class)
public class BGContinueEnterRoomRESAction extends ProtobufMessageHandler<RoomService, BGContinueEnterRoomRES>
{
	final static Logger logger = LoggerFactory.getLogger(BGContinueEnterRoomRESAction.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, BGContinueEnterRoomRES message, int callback)
	{
		GCContinueEnterRoomRES.Builder builder = GCContinueEnterRoomRES.newBuilder();
		int successCode = PlatformProtocolsConfig.GC_CONTINUE_ENTER_ROOM_SUCCESS;
		if (PlatformProtocolsConfig.BG_CONTINUE_ENTER_ROOM_SUCCESS != message.getResult())
		{
			if (message.getResult() == PlatformProtocolsConfig.BG_CONTINUE_ENTER_ROOM_FAILED_ROOM_FULL)
				builder.setResult(PlatformProtocolsConfig.GC_CONTINUE_ENTER_ROOM_FAILED_ROOM_FULL);
			if (message.getResult() == PlatformProtocolsConfig.BG_CONTINUE_ENTER_ROOM_FAILED_ROOM_NOT_EXIST)
				builder.setResult(PlatformProtocolsConfig.GC_CONTINUE_ENTER_ROOM_FAILED_ROOM_NOT_EXIST);
			if (message.getResult() == PlatformProtocolsConfig.BG_CONTINUE_ENTER_ROOM_CONNECTION_ERROR)
				builder.setResult(PlatformProtocolsConfig.GC_CONTINUE_ENTER_ROOM_CONNECTION_ERROR);
			builder.setIsHaveBeginFirstGame(false);
			builder.setRoomType(0);
			builder.addAllGameplays(message.getGameplaysList());
			builder.setRoundCount(0);
			builder.setMaxPlayerCount(0);
			builder.setPayType(0);
			Connection roleConnection = ConnectionManager.getInstance().getClientVirtualConnection(message.getRoleId());
			if (roleConnection != null)
				// 有可能已经离线了
				roleConnection.write(callback, builder.build());
			logger.info("enterRoom code {}", message.getResult());

			return;
		}

		builder.setResult(successCode);
		builder.setIsHaveBeginFirstGame(message.getIsHaveBeginFirstGame());
		builder.setRoomType(message.getRoomType());
		builder.addAllGameplays(message.getGameplaysList());
		builder.setRoundCount(message.getRoundCount());
		builder.setMaxPlayerCount(message.getMaxPlayerCount());
		builder.setPayType(message.getPayType());

		Connection roleConnection = ConnectionManager.getInstance().getClientVirtualConnection(message.getRoleId());
		if (roleConnection != null)
			// 有可能已经离线了
			roleConnection.write(callback, builder.build());

		GBContinueEnterRoomOverSYN.Builder gbSynBuilder = GBContinueEnterRoomOverSYN.newBuilder();
		gbSynBuilder.setResult(true);
		gbSynBuilder.setRoleId(message.getRoleId());
		gbSynBuilder.setRoomId(message.getRoomId());
		connection.write(callback, gbSynBuilder.build());
	}
}
