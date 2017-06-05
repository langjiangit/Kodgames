package com.kodgames.client.action.battle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.battle.BattleService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCPlayCardRES;

@ActionAnnotation(actionClass = BCPlayCardRESAction.class, messageClass = BCPlayCardRES.class, serviceClass = BattleService.class)
public class BCPlayCardRESAction extends ProtobufMessageHandler<BattleService, BCPlayCardRES>
{
	private static final Logger logger = LoggerFactory.getLogger(BCPlayCardRESAction.class);

	@Override
	public void handleMessage(Connection connection, BattleService service, BCPlayCardRES message, int callback)
	{
//		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.onPlayCard(connection, message, callback);
	}
}
