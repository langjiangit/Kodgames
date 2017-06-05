package com.kodgames.client.action.battle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.battle.BattleService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCPlayCardRES;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCPlayStepSYN;

@ActionAnnotation(actionClass = BCPlayStepSYNAction.class, messageClass = BCPlayStepSYN.class, serviceClass = BattleService.class)
public class BCPlayStepSYNAction extends ProtobufMessageHandler<BattleService, BCPlayStepSYN>
{
	private static final Logger logger = LoggerFactory.getLogger(BCPlayStepSYNAction.class);

	@Override
	public void handleMessage(Connection connection, BattleService service, BCPlayStepSYN message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.onPlayStepSYN(connection, message, callback);
	}
}
