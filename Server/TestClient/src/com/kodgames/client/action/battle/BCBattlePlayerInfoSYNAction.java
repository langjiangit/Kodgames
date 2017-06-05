package com.kodgames.client.action.battle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.battle.BattleService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCBattlePlayerInfoSYN;

@ActionAnnotation(actionClass = BCBattlePlayerInfoSYNAction.class, messageClass = BCBattlePlayerInfoSYN.class, serviceClass = BattleService.class)
public class BCBattlePlayerInfoSYNAction extends ProtobufMessageHandler<BattleService, BCBattlePlayerInfoSYN>
{
	private static final Logger logger = LoggerFactory.getLogger(BCBattlePlayerInfoSYNAction.class);

	@Override
	public void handleMessage(Connection connection, BattleService service, BCBattlePlayerInfoSYN message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		// service.onBattlePlayerInfoSYN(connection, message, callback);
	}
}
