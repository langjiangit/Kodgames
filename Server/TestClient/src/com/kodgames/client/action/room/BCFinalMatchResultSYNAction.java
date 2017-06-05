package com.kodgames.client.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.room.RoomService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCFinalMatchResultSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCEnterRoomRES;

@ActionAnnotation(actionClass = BCFinalMatchResultSYNAction.class, messageClass = BCFinalMatchResultSYN.class, serviceClass = RoomService.class)
public class BCFinalMatchResultSYNAction extends ProtobufMessageHandler<RoomService, BCFinalMatchResultSYN>
{
	private static final Logger logger = LoggerFactory.getLogger(BCFinalMatchResultSYNAction.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, BCFinalMatchResultSYN message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		
		service.onFinalMatchResultSYN(connection, message, callback);
	}
}
