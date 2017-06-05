package com.kodgames.client.service.battle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.StressTool2;
import com.kodgames.client.task.PlayerOnBattlePlayerInfoSYNTask;
import com.kodgames.client.task.PlayerOnMatchResultTask;
import com.kodgames.client.task.PlayerOnPlayStepSYNTask;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.service.PublicService;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCBattlePlayerInfoSYN;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCMatchResultSYN;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCPlayCardRES;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCPlayStepSYN;
import com.kodgames.message.proto.game.GameProtoBuf.GCHistoryRES;

public class BattleService extends PublicService
{
	private static final long serialVersionUId = 6542931250264624613L;
	private static final Logger logger = LoggerFactory.getLogger(BattleService.class);
	
	/*
	 * jiangzhen 2016.9.19
	 * 
	 */
	public void onBattlePlayerInfoSYN(Connection connection, BCBattlePlayerInfoSYN message, int callback)
	{
//		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
//		Player player = roleService.getPlayerByRoleId(connection.getRoleId());
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnBattlePlayerInfoSYNTask(connection, message));
	}

	/*
	 * jiangzhen 2016.9.19
	 */
	public void onPlayStepSYN(Connection connection, BCPlayStepSYN message, int callback)
	{
//		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
//		Player player = roleService.getPlayerByRoleId(connection.getRoleId());
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnPlayStepSYNTask(connection, message));
	}
	
	public void onPlayCard(Connection connection, BCPlayCardRES message, int callback)
	{
//		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
//		Player player = roleService.getPlayerByRoleId(connection.getRoleId());
//		player.onPlayCard(message, callback);
	}
	
	public void onHistoryRES(Connection connection, GCHistoryRES message, int callback)
	{
//		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
//		Player player = roleService.getPlayerByRoleId(connection.getRoleId());
//		player.onHistoryRES(message, callback);
	}

	public void onMactchResultSYNAction(Connection connection, BCMatchResultSYN message, int callback)
	{
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnMatchResultTask(connection, message));
	}
}
