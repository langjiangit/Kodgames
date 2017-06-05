package com.kodgames.game.action.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.game.common.Constant;
import com.kodgames.game.common.rule.RoomTypeConfig;
import com.kodgames.game.common.rule.RuleManager;
import com.kodgames.game.service.activity.ActivityService;
import com.kodgames.game.service.activity.LimitedCostlessActivityService;
import com.kodgames.game.service.role.RoleService;
import com.kodgames.game.service.room.RoomService;
import com.kodgames.game.start.CGProtobufMessageHandler;
import com.kodgames.message.proto.game.GameProtoBuf.CGContinueRoomREQ;
import com.kodgames.message.proto.game.GameProtoBuf.GBContinueRoomREQ;
import com.kodgames.message.proto.game.GameProtoBuf.GCContinueRoomRES;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

import limax.zdb.exception.ZdbRollbackException;
import xbean.RoleInfo;
import xbean.RoomInfo;

@ActionAnnotation(messageClass = CGContinueRoomREQ.class, actionClass = CGContinueRoomREQAction.class, serviceClass = RoomService.class)
public class CGContinueRoomREQAction extends CGProtobufMessageHandler<RoomService, CGContinueRoomREQ>
{
	private static final Logger logger = LoggerFactory.getLogger(CGContinueRoomREQAction.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, CGContinueRoomREQ message, int callback)
	{
		int creatorId = connection.getRemotePeerID();
		int oldRoomId = ServiceContainer.getInstance().getPublicService(RoleService.class).getRoleMemInfo(creatorId).getRoomId();
		int newRoomId = service.getNewRoomId();

		RoleInfo roleInfo = service.getRoleInfoByRoleId(creatorId);
		RoomInfo oldRoomInfo = service.getRoom(oldRoomId);
		if (oldRoomInfo == null)
		{
			GCContinueRoomRES.Builder builder = GCContinueRoomRES.newBuilder();
			builder.setResult(PlatformProtocolsConfig.GC_CONTINUE_ROOM_FAILED_NO_OLD_ROOM);
			connection.write(callback, builder.build());

			logger.debug("createRoomReq: Time out, old room already destroy -> roomId = {}", oldRoomId);
			throw new ZdbRollbackException(String.format("createRoomReq: Time out, old room already destroy -> roomId = {}", oldRoomId));
		}
		Integer battleId = oldRoomInfo.getBattleId();

		// 不支持的房间圈数局数
		RoomTypeConfig rc = RuleManager.getInstance().getRoomConfig(oldRoomInfo.getRoundCount());
		int needCardCount = 0;
		if (oldRoomInfo.getPayType() == Constant.PAY_TYPE.CREATOR_PAY)
			needCardCount = (null == rc) ? 0 : rc.getCardCount();
		if (oldRoomInfo.getPayType() == Constant.PAY_TYPE.AA_PAY)
			needCardCount = (null == rc) ? 0 : rc.getAACardCount();

		if (needCardCount <= 0)
		{
			GCContinueRoomRES.Builder builder = GCContinueRoomRES.newBuilder();
			builder.setResult(PlatformProtocolsConfig.GC_CONTINUE_ROOM_FAILED_INVALID_ROUND_COUNT);
			connection.write(callback, builder.build());

			logger.debug("createRoomReq: invalid roundCount -> roleId={}, roundCount={}", creatorId, oldRoomInfo.getRoundCount());
			throw new ZdbRollbackException(String.format("createRoom creator %d with invalid roundCount %d", creatorId, oldRoomInfo.getRoundCount()));
		}

		// 判断是否以限免方式创建房间
		LimitedCostlessActivityService lcaService = ServiceContainer.getInstance().getPublicService(ActivityService.class).getLca();
		boolean isLCARoom = false;
		if (lcaService.isLCACreateRoom(message.getFreeActivityId()))
		{
			if (!lcaService.isActive(message.getFreeActivityId()))
			{
				GCContinueRoomRES.Builder builder = GCContinueRoomRES.newBuilder();
				builder.setResult(PlatformProtocolsConfig.GC_CONTINUE_ROOM_NO_LCA);
				connection.write(callback, builder.build());
				return;
			}
			isLCARoom = true;
			needCardCount = 0;
		}

		// 玩家房卡不足
		if (!isLCARoom)
		{
			if (roleInfo.getCardCount() < needCardCount)
			{
				GCContinueRoomRES.Builder builder = GCContinueRoomRES.newBuilder();
				builder.setResult(PlatformProtocolsConfig.GC_CONTINUE_ROOM_FAILED_ROOMCARD_NOT_ENOUGTH);
				connection.write(callback, builder.build());

				logger.debug("createRoom: roleId={}, roundCount={}, needCardCount={}, roleCardCount={}", creatorId, oldRoomInfo.getRoundCount(), needCardCount, roleInfo.getCardCount());
				throw new ZdbRollbackException(String.format("createRoom creator %d without RoleInfo or cardCount not enough", creatorId));
			}
		}

		// 将房间号置为创建中
		service.creatingRoom(battleId, newRoomId, oldRoomInfo.getRoundCount(), isLCARoom, 0, 0, oldRoomInfo.getPayType());

		// 向Battle请求创建房间
		GBContinueRoomREQ.Builder builder = GBContinueRoomREQ.newBuilder();
		builder.setNewRoomId(newRoomId);
		builder.setOldRoomId(oldRoomId);
		builder.setCreatorId(creatorId);

		Connection battleConnection = ConnectionManager.getInstance().getServerConnnection(battleId);
		battleConnection.write(callback, builder.build());
	}

	@Override
	public Object getMessageKey(Connection connection, CGContinueRoomREQ message)
	{
		return ConnectionManager.getInstance().getMsgInitializer().getProtocolID(message.getClass());
	}

}
