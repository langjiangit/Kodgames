package com.kodgames.client.service.marquee;

import com.kodgames.client.common.client.Player;
import com.kodgames.client.service.account.RoleService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.service.PublicService;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.message.proto.marquee.MarqueeProtoBuf.GCMarqueeRES;
import com.kodgames.message.proto.marquee.MarqueeProtoBuf.GCMarqueeVersionSYNC;

public class MarqueeService extends PublicService
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void onMarqueeSYN(Connection connection, GCMarqueeVersionSYNC message, int callback)
	{
		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
		Player player = roleService.getPlayerByRoleId(connection.getRemotePeerID());
		player.onMarqueeSYN(message, callback);
	}

	public void onMarqueeRES(Connection connection, GCMarqueeRES message, int callback)
	{
		Player player = ServiceContainer.getInstance()
			.getPublicService(RoleService.class)
			.getPlayerByRoleId(connection.getRemotePeerID());
		
		player.onMarqueeRES(message, callback);
	}
}
