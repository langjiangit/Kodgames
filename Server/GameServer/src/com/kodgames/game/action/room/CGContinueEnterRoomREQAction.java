package com.kodgames.game.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.game.common.Constant.PAY_TYPE;
import com.kodgames.game.common.rule.RoomTypeConfig;
import com.kodgames.game.common.rule.RuleManager;
import com.kodgames.game.service.room.RoomService;
import com.kodgames.game.start.CGProtobufMessageHandler;
import com.kodgames.message.proto.game.GameProtoBuf.CGContinueEnterRoomREQ;
import com.kodgames.message.proto.game.GameProtoBuf.GBContinueEnterRoomREQ;
import com.kodgames.message.proto.game.GameProtoBuf.GCContinueEnterRoomRES;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

import xbean.RoleInfo;
import xbean.RoomInfo;

@ActionAnnotation(messageClass = CGContinueEnterRoomREQ.class, actionClass = CGContinueEnterRoomREQ.class, serviceClass = RoomService.class)
public class CGContinueEnterRoomREQAction extends CGProtobufMessageHandler<RoomService, CGContinueEnterRoomREQ>
{
	private static final Logger logger = LoggerFactory.getLogger(CGContinueEnterRoomREQAction.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, CGContinueEnterRoomREQ message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);

		int roleId = message.getRoleId();
		int roomId = message.getRoomId();

		RoleInfo roleInfo = service.getRoleInfoByRoleId(roleId);
		RoomInfo roomInfo = service.getRoom(roomId);
		int oldRoomId = roleInfo.getHistoryRooms().get(roleInfo.getHistoryRooms().size() - 1).getRoomId();
		RoomInfo oldRoomInfo = service.getRoom(oldRoomId);
		GCContinueEnterRoomRES.Builder builder = GCContinueEnterRoomRES.newBuilder();

		if (oldRoomInfo == null)
		{
			int error = PlatformProtocolsConfig.GC_CONTINUE_ENTER_ROOM_FAILED_NO_OLD_ROOM;
			builder.setResult(error);
			connection.write(callback, builder.build());
			String log = String.format("%s : 0x%x", getClass().getSimpleName(), error);
			logger.info(log);

			return;
		}

		int battleId = service.getBattleIdByRoomId(roomId);
		if (0 == battleId || roomInfo == null)
		{
			int error = PlatformProtocolsConfig.GC_CONTINUE_ENTER_ROOM_FAILED_ROOM_NOT_EXIST;
			builder.setResult(error);
			connection.write(callback, builder.build());
			String log = String.format("%s : 0x%x", getClass().getSimpleName(), error);
			logger.info(log);

			return;
		}

		RoomTypeConfig rc = RuleManager.getInstance().getRoomConfig(roomInfo.getRoundCount());
		int needCardCount = (roomInfo.getPayType() == PAY_TYPE.CREATOR_PAY) ? 0 : rc.getAACardCount();

		if (roleInfo.getCardCount() < needCardCount)
		{
			int error = PlatformProtocolsConfig.GC_CONTINUE_ENTER_ROOM_FAILED_ROOMCARD_NOT_ENOUGTH;
			builder.setResult(error);
			connection.write(callback, builder.build());
			String log = String.format("%s : 0x%x", getClass().getSimpleName(), error);
			logger.info(log);

			return;
		}

		if (roomInfo != null && roomInfo.getClubId() > 0)
		{
			int error = PlatformProtocolsConfig.GC_CONTINUE_ENTER_ROOM_FAILED_CLUB_ROOM;
			builder.setResult(error);
			connection.write(callback, builder.build());
			String log = String.format("%s : 0x%x", getClass().getSimpleName(), error);
			logger.info(log);

			return;
		}

		GBContinueEnterRoomREQ.Builder gbBuilder = GBContinueEnterRoomREQ.newBuilder();

		gbBuilder.setRoleId(roleId);
		gbBuilder.setRoomId(roomId);
		gbBuilder.setOldRoomId(roleInfo.getHistoryRooms().get(roleInfo.getHistoryRooms().size() - 1).getRoomId());

		Connection battleConnection = ConnectionManager.getInstance().getServerConnnection(battleId);
		battleConnection.write(callback, gbBuilder.build());
	}

	@Override
	public Object getMessageKey(Connection connection, CGContinueEnterRoomREQ message)
	{
		return message.getRoomId();
	}
}
