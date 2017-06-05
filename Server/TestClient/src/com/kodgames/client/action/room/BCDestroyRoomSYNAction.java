package com.kodgames.client.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.room.RoomService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.room.RoomProtoBuf.BCDestroyRoomSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCEnterRoomRES;

@ActionAnnotation(actionClass = BCDestroyRoomSYNAction.class, messageClass = BCDestroyRoomSYN.class, serviceClass = RoomService.class)
public class BCDestroyRoomSYNAction extends ProtobufMessageHandler<RoomService, BCDestroyRoomSYN>
{
	private static final Logger logger = LoggerFactory.getLogger(BCDestroyRoomSYNAction.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, BCDestroyRoomSYN message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.onDestroyRoomSYN(connection, message, callback);
	}
}
