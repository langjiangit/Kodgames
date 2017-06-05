package com.kodgames.agent;

import com.kodgames.corgi.core.net.handler.message.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.agent.start.NetInitializer;
import com.kodgames.agent.start.ServerConfigInitializer;

import limax.xmlconfig.Service;

public class AgentServer
{
	private static Logger logger = LoggerFactory.getLogger(AgentServer.class);

	public static void main(String[] args)
	{
		// 消息派发不使用ZDB
		MessageDispatcher.getInstance().setOpenZDBProcedure(false);

		ServerConfigInitializer.getInstance().init("/agent.conf");
		try
		{
			NetInitializer.getInstance().init();
		}
		catch (Exception e)
		{
			logger.error("NetInitializer initialization failed! : {}", e);
		}
	}
}
