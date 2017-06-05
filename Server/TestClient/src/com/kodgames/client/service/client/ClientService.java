package com.kodgames.client.service.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodgames.client.common.client.ClientConfig;
import com.kodgames.client.common.client.Player;
import com.kodgames.client.common.client.Room;
import com.kodgames.client.common.server.ServerInfo;
import com.kodgames.client.start.NetInitializer;
import com.kodgames.corgi.core.service.PublicService;

public class ClientService extends PublicService
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ClientConfig clientConfig = null;

	public void start()
		throws Exception
	{
		Player player = new Player();
		
		//得到网关地址
//		String gwIp = ClientConfig.getInstance().getGwIp();
//		int gwPort = ClientConfig.getInstance().getGwPort();
//		
//		//得到auth服务器地址
//		String authInfo = player.getAuthServerInfo(gwIp, gwPort);
//		
//		//得到auth服务器返回的结果
//		String authResultJson = player.getAuthResult(authInfo);
//
//		JsonNode authResult = new ObjectMapper().readTree(authResultJson).findValue("result");
//		
//		//缓存auth结果
//		ClientConfig.getInstance().addAuthResult(authResult, player);
//		
//		//获取Interface的地址
//		JsonNode interfaceNode = authResult.findValue("interface");
//		String interfaceIp = interfaceNode.findValue("socket_ip").asText();
//		int interfacePort = interfaceNode.findValue("socket_port").asInt();
//
//		//跟Interface进行连接
//		SocketAddress address = new InetSocketAddress(interfaceIp, interfacePort);
//		NetInitializer net = new NetInitializer(player);
//		net.connectToInterface(address);
	}
}
