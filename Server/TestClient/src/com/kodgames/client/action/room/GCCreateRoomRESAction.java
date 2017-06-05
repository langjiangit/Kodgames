package com.kodgames.client.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.room.RoomService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.game.GameProtoBuf.GCCreateRoomRES;

@ActionAnnotation(actionClass = GCCreateRoomRESAction.class, messageClass = GCCreateRoomRES.class, serviceClass = RoomService.class)
public class GCCreateRoomRESAction extends ProtobufMessageHandler<RoomService, GCCreateRoomRES>
{
	private static final Logger logger = LoggerFactory.getLogger(GCCreateRoomRESAction.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, GCCreateRoomRES message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.onCreateRoom(connection, message, callback);
	}
}
