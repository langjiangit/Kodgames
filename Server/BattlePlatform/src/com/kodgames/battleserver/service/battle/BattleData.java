package com.kodgames.battleserver.service.battle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kodgames.battleserver.service.battle.common.xbean.BattleRoom;
import com.kodgames.battleserver.service.battle.common.xbean.RoomPlayerInfo;
import com.kodgames.battleserver.service.battle.core.IBattleHelper;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCMatchResultSYN;
import com.kodgames.message.proto.battle.BattleProtoBuf.PlayerMatchFinalResultPROTO;
import com.kodgames.message.proto.battle.BattleProtoBuf.PlayerMatchResultPROTO;
import com.kodgames.message.proto.battle.BattleProtoBuf.ResultGamePROTO;
import com.kodgames.message.proto.game.GameProtoBuf.BGFinalMatchResultSYN;
import com.kodgames.message.proto.game.GameProtoBuf.BGMatchResultSYN;
import com.kodgames.message.proto.game.GameProtoBuf.PlayerHistoryPROTO;
import com.kodgames.message.proto.game.GameProtoBuf.RoundReportPROTO;

public class BattleData
{

	// 将房间内的总战斗信息通知给gameserver
	public static BGFinalMatchResultSYN finalResultToBGFinalSYN(BattleRoom room)
	{
		BGFinalMatchResultSYN.Builder gameBuilder = BGFinalMatchResultSYN.newBuilder();

		gameBuilder.setRoomId(room.getRoomId());
		gameBuilder.setCreateTime(room.getCreateTime());
		gameBuilder.setRoundType(room.getCountType());

		// 构造玩家所有战局的数据
		for (RoomPlayerInfo player : room.getPlayers())
		{
			PlayerHistoryPROTO.Builder playerBuilder = PlayerHistoryPROTO.newBuilder();
			playerBuilder.setRoleId(player.getRoleId());
			playerBuilder.setPosition(player.getPosition());
			playerBuilder.setNickname(player.getNickname());
			playerBuilder.setHeadImgUrl(player.getHeadImageUrl());
			playerBuilder.setTotalPoint(player.getTotalPoint());
			playerBuilder.setSex(player.getSex());
			gameBuilder.addPlayerRecords(playerBuilder);
		}
		return gameBuilder.build();
	}

	// 将房间内的战斗信息转为历史战绩信息，用于发往game服务器
	public static BGMatchResultSYN roundBeanToRoomHistoryProto(BCMatchResultSYN.Builder calcBuilder, BattleRoom room, IBattleHelper battleHelper)
	{
		BGMatchResultSYN.Builder gameBuilder = BGMatchResultSYN.newBuilder();
		RoundReportPROTO.Builder roundReport = RoundReportPROTO.newBuilder();
		Map<Integer, Integer> playerPoint = new HashMap<>();
		roundReport.setStartTime(battleHelper.getBattleBean().getStartTime());
		for (PlayerMatchResultPROTO playerResult : calcBuilder.getMatchResultsList())
		{
			roundReport.addPlayerRecords(playerResult.toByteString());
			playerPoint.put(playerResult.getRoleId(), playerResult.getTotalPoint());
		}
		// 设置剩余牌
		roundReport.setLastCards(calcBuilder.getLastCards());
		gameBuilder.setCreateTime(room.getCreateTime());
		gameBuilder.setRoomId(room.getRoomId());
		gameBuilder.addAllGameplays(room.getGameplays());
		gameBuilder.setEnableMutilHu(battleHelper.enableMutilHu());
		gameBuilder.setPlaybackDatas(battleHelper.generatePlaybackData().toByteString());

		for (RoomPlayerInfo player : room.getPlayers())
		{
			PlayerHistoryPROTO.Builder playerBuilder = PlayerHistoryPROTO.newBuilder();
			playerBuilder.setRoleId(player.getRoleId());
			playerBuilder.setPosition(player.getPosition());
			playerBuilder.setNickname(player.getNickname());
			playerBuilder.setHeadImgUrl(player.getHeadImageUrl());
			playerBuilder.setTotalPoint(playerPoint.get(player.getRoleId()));
			playerBuilder.setSex(player.getSex());
			gameBuilder.addPlayerRecords(playerBuilder.build());
		}
		gameBuilder.setRoundType(room.getCountType());
		gameBuilder.setRoundCount(room.getTotalGameCount());
		gameBuilder.setPlayerMaxCardCount(battleHelper.getPlayCardCount());
		gameBuilder.setRoundReportRecord(roundReport.build());
		return gameBuilder.build();
	}

	// 获取房间的最终结算信息
	public static List<PlayerMatchFinalResultPROTO> roomBeanToMatchResultProtos(BattleRoom room)
	{
		List<PlayerMatchFinalResultPROTO> results = new ArrayList<>();
		for (RoomPlayerInfo playerInfo : room.getPlayers())
		{
			PlayerMatchFinalResultPROTO.Builder finalResultBuilder = PlayerMatchFinalResultPROTO.newBuilder();
			finalResultBuilder.setRoleId(playerInfo.getRoleId());
			finalResultBuilder.setTotalPoint(playerInfo.getTotalPoint());

			playerInfo.getGameScores().forEach(gameScore -> {
				ResultGamePROTO.Builder resultGame = ResultGamePROTO.newBuilder();
				resultGame.setType(gameScore.getScoreType());
				resultGame.setAddOperation(gameScore.getAddOperation());
				resultGame.setTimes(gameScore.getTimes());
				finalResultBuilder.addGameResult(resultGame);
			});

			results.add(finalResultBuilder.build());
		}

		return results;
	}
}