package com.kodgames.client.action.battle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.battle.BattleService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCPlayCardRES;
import com.kodgames.message.proto.game.GameProtoBuf.GCHistoryRES;

@ActionAnnotation(actionClass = GCHistoryRESAction.class, messageClass = GCHistoryRES.class, serviceClass = BattleService.class)
public class GCHistoryRESAction extends ProtobufMessageHandler<BattleService, GCHistoryRES>
{
	private static final Logger logger = LoggerFactory.getLogger(GCHistoryRESAction.class);

	@Override
	public void handleMessage(Connection connection, BattleService service, GCHistoryRES message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.onHistoryRES(connection, message, callback);
	}
}
