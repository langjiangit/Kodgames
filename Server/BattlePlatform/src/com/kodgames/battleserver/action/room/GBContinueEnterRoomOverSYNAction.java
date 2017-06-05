package com.kodgames.battleserver.action.room;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.battleserver.service.battle.BattleService;
import com.kodgames.battleserver.service.battle.common.xbean.BattleRoom;
import com.kodgames.battleserver.service.room.RoomService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.message.proto.game.GameProtoBuf.GBContinueEnterRoomOverSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCSameIpSYN;

@ActionAnnotation(actionClass = GBContinueEnterRoomOverSYNAction.class, messageClass = GBContinueEnterRoomOverSYN.class, serviceClass = RoomService.class)
public class GBContinueEnterRoomOverSYNAction extends ProtobufMessageHandler<RoomService, GBContinueEnterRoomOverSYN>
{
	final static Logger logger = LoggerFactory.getLogger(GBContinueEnterRoomOverSYNAction.class);

	@Override
	public void handleMessage(Connection connection, RoomService service, GBContinueEnterRoomOverSYN message, int callback)
	{
		int roleId = message.getRoleId();
		int roomId = message.getRoomId();

		BattleRoom room = service.getRoomInfo(roomId);
		BattleService battleService = ServiceContainer.getInstance().getPublicService(BattleService.class);

		service.synBattleToGame(roleId, roomId);

//		Map<Integer, BCSameIpSYN> sameIpSyncInfo = null;
//		// 如果牌局未开始，则检测房间的相同IP信息，并将需要被告警的人置为未准备
//		if (!service.isHaveBeginFirstGame(room))
//		{
//			sameIpSyncInfo = service.checkSameIpInfo(room);
//		}

		// 将玩家数据同步给Client
		service.syncRoomPlayerInfoToMembers(room);

//		// 如果牌局未开始，则将IP相同的玩家信息同步给客户端
//		if (!service.isHaveBeginFirstGame(room) && sameIpSyncInfo != null)
//		{
//			service.syncSameIpInfoToPlayers(sameIpSyncInfo);
//		}

		// 房间内正在打牌，向该玩家同步打牌信息
		if (battleService.isBattleRunning(roomId))
		{
			battleService.rejoin(roomId, roleId, room.getPlayers().stream().collect(Collectors.toList()));
			logger.debug("role {} rejoin room {}", roleId, roomId);
		}
		// 如果可以开牌就开牌
		else if (!battleService.isBattleRunning(roomId) && service.isRoomReadyForBattle(room))
		{
			battleService.startGame(room);
		}
		// 如果房间内正在投票，向该玩家同步投票信息
		if (service.isRoomVotingDestroy(room))
		{
			service.syncVoteDestroyInfoToPlayers(room, roleId);
		}
	}
}
