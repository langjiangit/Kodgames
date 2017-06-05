package com.kodgames.client.service.account;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.StressTool2;
import com.kodgames.client.common.client.Player;
import com.kodgames.client.task.PlayerOnAuthConnectTask;
import com.kodgames.client.task.PlayerOnLoginTask;
import com.kodgames.client.task.PlayerOnLogoutTask;
import com.kodgames.client.task.PlayerOnQueryBattleIdTask;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.service.PublicService;
import com.kodgames.message.proto.auth.AuthProtoBuf.ICAccountAuthRES;
//import com.kodgames.message.proto.auth.AuthProtoBuf.ICConnectAuthRES;
import com.kodgames.message.proto.game.GameProtoBuf.GCLoginRES;
import com.kodgames.message.proto.game.GameProtoBuf.GCLogoutRES;
import com.kodgames.message.proto.mail.MailProtoBuf.GCMailRES;
import com.kodgames.message.proto.game.GameProtoBuf.GCQueryBattleIdRES;

public class RoleService extends PublicService
{
	private static final long serialVersionUId = -2892979588411902322L;
	private static final Logger logger = LoggerFactory.getLogger(RoleService.class);
	public static AtomicInteger callbackSeed = new AtomicInteger(0);
	
	public static int x = 1;
	
	//jiangzhen,2016.9.7
	private ConcurrentHashMap<Integer, Player> idPlayerMap = null;
	private ConcurrentHashMap<Connection, Player> connPlayerMap = null;
	
	public RoleService(){
		idPlayerMap = new ConcurrentHashMap<Integer, Player>();
		this.connPlayerMap = new ConcurrentHashMap<Connection, Player>();
	}
	
	public Player getPlayerByRoleId(int id){
		return this.idPlayerMap.get(id);
	}
	
	public void addPlayerById(int id, Player player){
		this.idPlayerMap.put(id, player);
	}
	
	public void removePlayerById(int id){
		this.idPlayerMap.remove(id);
	}
	
	public void addPlayerByConn(Connection conn, Player player){
		this.connPlayerMap.put(conn, player);
	}
	
	public Player getPlayerByConn(Connection conn){
		return this.connPlayerMap.get(conn);
	}
	
	public void removePlayerByConn(Connection conn){
		this.connPlayerMap.remove(conn);
	}
	
	public void onLogin(Connection connection, GCLoginRES res, int callback)
	{
//		if (ProtocolsConfig.GC_LOGIN_SUCCESS == res.getResult())
//		{
//			int id = connection.getRoleId();
//			logger.info("role {} Login success", id);
//			Player p = this.idPlayerMap.get(id);
//			p.onLogin(res);
//		}
//		Player player = this.idPlayerMap.get(connection.getRoleId());
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnLoginTask(connection, res));
	}
	
	public void onLogout(Connection connection, GCLogoutRES res, int callback)
	{
		logger.info("RoleService onLogout()");
		Player player = this.connPlayerMap.get(connection);
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnLogoutTask(player, res));
	}
	
	public void onQueryBattleId(Connection connection, GCQueryBattleIdRES res, int callback){
		Player player = this.idPlayerMap.get(connection.getRemotePeerID());
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnQueryBattleIdTask(connection, res));
	}

//	public void onConnectAuth(Connection connection, ICConnectAuthRES message, int callback)
//	{
//		logger.info("role {} connectAuth result {}", message.getRoleId(), message.getResult());
//		
//		Player p = this.connPlayerMap.get(connection);
////		p.onAuthConnect(res);
//		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnAuthConnectTask(p, message));
//	}
	
	public void onMailRES(Connection connection, GCMailRES message, int callback)
	{
//		Player player = this.idPlayerMap.get(connection.getRemotePeerID());
//		player.onMailRES(message, callback);
	}

	public void onAccountAuth(Connection connection, ICAccountAuthRES message, int callback)
	{
		Player player = this.connPlayerMap.get(connection);
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnAuthConnectTask(player, message, callback));
	}
}
