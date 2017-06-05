package com.kodgames.client.service.room;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.kodgames.message.proto.game.GameProtoBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.StressTool2;
import com.kodgames.client.common.client.Player;
import com.kodgames.client.common.client.Room;
import com.kodgames.client.service.account.RoleService;
import com.kodgames.client.service.battle.BattleService;
import com.kodgames.client.task.PlayerOnCreateRoomTask;
import com.kodgames.client.task.PlayerOnDestroyRoomSYNTask;
import com.kodgames.client.task.PlayerOnEnterRoomTask;
import com.kodgames.client.task.PlayerOnFinalMatchResultTask;
import com.kodgames.client.task.PlayerOnQuitRoomTask;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.service.PublicService;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCFinalMatchResultSYN;
import com.kodgames.message.proto.game.GameProtoBuf.GCRoomCardModifySYNC;
import com.kodgames.message.proto.marquee.MarqueeProtoBuf.GCMarqueeRES;
import com.kodgames.message.proto.room.RoomProtoBuf.BCDestroyRoomSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCEnterRoomRES;
import com.kodgames.message.proto.room.RoomProtoBuf.BCQuitRoomRES;
import com.kodgames.message.proto.room.RoomProtoBuf.BCRoomPlayerInfoSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCUpdateStatusRES;
import com.kodgames.message.proto.room.RoomProtoBuf.BCVoteDestroyRES;
public class RoomService extends PublicService
{
	private static final Logger logger = LoggerFactory.getLogger(BattleService.class);
	public static final int ROOM_SIZE = 4;
	public static AtomicInteger localRoomIdSeed = new AtomicInteger(0);

	private static final long serialVersionUId = -2892979588411902322L;

	private Map<Integer, Room> idRoomMap = null;

	public RoomService()
	{
		idRoomMap = new HashMap<Integer, Room>();
	}

	public void addRoomById(int id, Room room)
	{
		this.idRoomMap.put(id, room);
	}

	public Room getRoomBytId(int id)
	{
		return this.idRoomMap.get(id);
	}

	public void removeRoomById(int id)
	{
		this.idRoomMap.remove(id);
	}

	/*
	 * jiangzhen,2016.9.8
	 */
	public void onCreateRoom(Connection connection, GameProtoBuf.GCCreateRoomRES res, int callback)
	{
		// 获得创建房间的玩家
		// RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
		// Player p = roleService.getPlayerByRoleId(connection.getRoleId());
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnCreateRoomTask(connection, res));
	}

	public void onEnterRoom(Connection connection, BCEnterRoomRES res, int callback)
	{
		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
		Player player = roleService.getPlayerByRoleId(connection.getRemotePeerID());

		 player.onEnterRoom(res);
	}

	public void onVoteDestroy(BCVoteDestroyRES message)
	{
	}

	public void onQuitRoomResult(Connection connection, BCQuitRoomRES res, int callback)
	{
		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
		Player player = roleService.getPlayerByRoleId(connection.getRemotePeerID());

		player.onQuitRoom(res, callback);
	}

	public void onSyncPlayerInfoInRoom(Connection connection, BCRoomPlayerInfoSYN syn, int callback)
	{
		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
		Player player = roleService.getPlayerByRoleId(connection.getRemotePeerID());
		player.onRoomPlayerInfoSYN(syn, callback);
	}

	public void onUpdatePlayerStatus(Connection connection, BCUpdateStatusRES res, int callback)
	{
		// RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
		// Player player = roleService.getPlayerByRoleId(connection.getRoleId());
		// player.onUpdatePlayerStatus(res, callback);
	}

	public void onMarqueeRES(Connection connection, GCMarqueeRES message, int callback)
	{
		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
		Player player = roleService.getPlayerByRoleId(connection.getRemotePeerID());
		player.onMarqueeRES(message, callback);
	}

	public void onRoomCardModifySYNC(Connection connection, GCRoomCardModifySYNC message, int callback)
	{

	}

	public void onDestroyRoomSYN(Connection connection, BCDestroyRoomSYN message, int callback)
	{
		Player player = ServiceContainer.getInstance()
			.getPublicService(RoleService.class)
			.getPlayerByRoleId(connection.getRemotePeerID());
		
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnDestroyRoomSYNTask(player, message));
	}

	public void onFinalMatchResultSYN(Connection connection, BCFinalMatchResultSYN message, int callback)
	{
		Player player = ServiceContainer.getInstance().getPublicService(RoleService.class).getPlayerByRoleId(connection.getRemotePeerID());
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnFinalMatchResultTask(player, message));
	}
}