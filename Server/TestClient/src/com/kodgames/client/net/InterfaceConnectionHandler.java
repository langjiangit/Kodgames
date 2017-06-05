package com.kodgames.client.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.StressTool2;
import com.kodgames.client.common.client.Player;
import com.kodgames.client.task.PlayerOnInterfaceConnect;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.handler.connection.AbstractConnectionHandler;

public class InterfaceConnectionHandler extends AbstractConnectionHandler
{
	private static final Logger logger = LoggerFactory.getLogger(InterfaceConnectionHandler.class);

	private Player client;

	public InterfaceConnectionHandler(Player client)
	{
		this.client = client;
	}

	@Override
	public void handleConnectionActive(Connection connection)
	{
//		 client.onInterfaceConnect(connection);
		StressTool2.getInstance().putTaskIntoThreadPool(new PlayerOnInterfaceConnect(client, connection));	
	}

	@Override
	public void handleConnectionInactive(Connection connection)
	{
		client.onInterfaceDisconnect(connection);
	}
}
