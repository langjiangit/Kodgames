package com.kodgames.client.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.room.RoomService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.room.RoomProtoBuf.BCQuitRoomRES;

@ActionAnnotation(actionClass = BCQuitRoomRESAction.class, messageClass = BCQuitRoomRES.class, serviceClass = RoomService.class)
public class BCQuitRoomRESAction extends ProtobufMessageHandler<RoomService, BCQuitRoomRES>
{
	private static final Logger logger = LoggerFactory.getLogger(BCQuitRoomRESAction.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, BCQuitRoomRES message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.onQuitRoomResult(connection, message, callback);
	}
}