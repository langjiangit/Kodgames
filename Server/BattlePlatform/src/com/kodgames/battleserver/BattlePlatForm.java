package com.kodgames.battleserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.battleserver.start.NetInitializer;
import com.kodgames.battleserver.start.ServerConfigInitializer;
import com.kodgames.battleserver.start.TaskInitializer;
import com.kodgames.core.event.EventManager;
import com.kodgames.corgi.core.net.handler.message.MessageDispatcher;

public class BattlePlatForm
{
	static private Logger logger = LoggerFactory.getLogger(BattlePlatForm.class);

	public static void startServer()
	{
		// 消息派发不使用ZDB
		MessageDispatcher.getInstance().setOpenZDBProcedure(false);
		EventManager.getInstance().setOpenZDBProcedure(false);

		ServerConfigInitializer.getInstance().init("/battle.conf");
		TaskInitializer.getInstance().init();
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
