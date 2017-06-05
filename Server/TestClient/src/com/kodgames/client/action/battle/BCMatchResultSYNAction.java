package com.kodgames.client.action.battle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.battle.BattleService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCMatchResultSYN;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCPlayCardRES;

@ActionAnnotation(actionClass = BCMatchResultSYNAction.class, messageClass = BCMatchResultSYN.class, serviceClass = BattleService.class)
public class BCMatchResultSYNAction extends ProtobufMessageHandler<BattleService, BCMatchResultSYN>
{
	private static final Logger logger = LoggerFactory.getLogger(BCMatchResultSYNAction.class);

	@Override
	public void handleMessage(Connection connection, BattleService service, BCMatchResultSYN message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.onMactchResultSYNAction(connection, message, callback);
	}
}
