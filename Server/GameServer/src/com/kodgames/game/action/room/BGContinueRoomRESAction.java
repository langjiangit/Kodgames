package com.kodgames.game.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.game.service.role.RoleService;
import com.kodgames.game.service.room.RoomService;
import com.kodgames.message.proto.game.GameProtoBuf.BGContinueRoomRES;
import com.kodgames.message.proto.game.GameProtoBuf.GCContinueRoomRES;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

@ActionAnnotation(messageClass = BGContinueRoomRES.class, actionClass = BGContinueRoomRES.class, serviceClass = RoomService.class)
public class BGContinueRoomRESAction extends ProtobufMessageHandler<RoomService, BGContinueRoomRES>
{
	private static final Logger logger = LoggerFactory.getLogger(BGContinueRoomRES.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, BGContinueRoomRES message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getRemotePeerID(), message);

		int battleId = connection.getRemotePeerID();
		int roomId = message.getRoomId();
		int creatorId = message.getCreatorId();
		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);

		int successCode;
		GCContinueRoomRES.Builder builder = GCContinueRoomRES.newBuilder();
		successCode = PlatformProtocolsConfig.GC_CONTINUE_ROOM_SUCCESS;
		if (PlatformProtocolsConfig.BG_CONTINUE_ROOM_SUCCESS != message.getResult())
		{
			if (message.getResult() != PlatformProtocolsConfig.BG_CONTINUE_ROOM_FAILED_ROOM_EXIST)
			{
				// 对于非"房间已存在"的错误,删除正在创建的房间信息
				service.deleteCreatingRoom(roomId);
			}

			// 创建失败
			logger.error("BattleServer can not create a room : {} -> {}!", message.getResult(), roomId);
			builder.setResult(message.getResult());
		}
		else
		{
			// 创建成功
			roleService.joinRoom(creatorId, roomId, battleId);
			service.createRoom(creatorId, roomId, message.getGameplaysList());
			builder.setResult(successCode);
			builder.setRoomId(roomId);
		}

		Connection roleConnection = ConnectionManager.getInstance().getClientVirtualConnection(creatorId);
		if (roleConnection != null)
			roleConnection.write(callback, builder.build());
	}

	@Override
	public Object getMessageKey(Connection connection, BGContinueRoomRES message)
	{
		return message.getCreatorId() == 0 ? connection.getConnectionID() : message.getCreatorId();
	}
}
