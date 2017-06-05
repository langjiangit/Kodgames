package com.kodgames.client.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.room.RoomService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.room.RoomProtoBuf.BCEnterRoomRES;

@ActionAnnotation(actionClass = BCEnterRoomRESAction.class, messageClass = BCEnterRoomRES.class, serviceClass = RoomService.class)
public class BCEnterRoomRESAction extends ProtobufMessageHandler<RoomService, BCEnterRoomRES>
{
	private static final Logger logger = LoggerFactory.getLogger(BCEnterRoomRESAction.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, BCEnterRoomRES message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.onEnterRoom(connection, message, callback);
	}
}
