package com.kodgames.client.start;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.kodgames.client.common.client.Player;
import com.kodgames.client.net.ClientMessageInitializer;
import com.kodgames.client.net.InterfaceConnectionHandler;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.ServerMessageInitializer;
import com.kodgames.corgi.core.net.server.SimpleClient;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.message.protocol.PlatformProtocolsConfig;
import com.kodgames.message.protocol.ProtocolsConfig;

public class NetInitializer
{
	private SimpleClient interfaceClient;

	public NetInitializer(final Player client)
		throws Exception
	{
		List<String> packageNameList = new ArrayList<String>();
		List<Class> protocolClassList = new ArrayList<Class>();
		
		packageNameList.add("com.kodgames.client.action");
		protocolClassList.add(PlatformProtocolsConfig.class);
		protocolClassList.add(ProtocolsConfig.class);

		ClientMessageInitializer interfaceMsgInitializer = new ClientMessageInitializer(packageNameList, protocolClassList)
		{
			@Override
			protected void initMessages()
				throws Exception
			{
				super.initMessages();
				InterfaceConnectionHandler cbConnectionHandler = new InterfaceConnectionHandler(client);
				setConnectionHandler(cbConnectionHandler);
			}
		};

		interfaceMsgInitializer.initialize();
		ConnectionManager.getInstance().init(interfaceMsgInitializer);
		interfaceClient = new SimpleClient();
		interfaceClient.initialize(new ClientDispatchNettyInitializer(interfaceMsgInitializer), interfaceMsgInitializer);
		
		ConnectionManager.getInstance().init(interfaceMsgInitializer);
	}

	public void connectToInterface(SocketAddress address)
	{
		interfaceClient.connectTo(address, 1);
//		ConnectionManager.getInstance().addServerInfo(0, address);
	}
}
