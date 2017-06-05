package com.kodgames.client.service.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.common.client.Player;
import com.kodgames.client.service.account.RoleService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.service.PublicService;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.message.proto.chat.ChatProtoBuf.BCChatRES;
import com.kodgames.message.proto.chat.ChatProtoBuf.BCChatSYN;
import com.kodgames.message.protocol.ProtocolsConfig;

public class ChatService extends PublicService
{
	private static final long serialVersionUId = 8946765866189297886L;
	Logger logger = LoggerFactory.getLogger(ChatService.class);

	/*
	 * jiangzhen,2016.9.7
	 * 收到来天应答后的处理函数
	 */
	public void onChat(Connection connection, BCChatRES res, int callback)
	{
		if (ProtocolsConfig.BC_CHAT_SUCCESS == res.getResult()){
			RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
			Player player = roleService.getPlayerByRoleId(connection.getRemotePeerID());
			player.onChat(res, callback);
		}
	}

	public void onChatSYN(Connection connection, BCChatSYN message, int callback)
	{
		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
		Player player = roleService.getPlayerByRoleId(connection.getRemotePeerID());
		player.onChatSYN(message, callback);
	}
}
