package com.kodgames.client.action.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.account.RoleService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.message.proto.game.GameProtoBuf.GCLoginRES;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;

@ActionAnnotation(actionClass = GCLoginRESAction.class, messageClass = GCLoginRES.class, serviceClass = RoleService.class)
public class GCLoginRESAction extends ProtobufMessageHandler<RoleService, GCLoginRES>
{
	private static final Logger logger = LoggerFactory.getLogger(GCLoginRESAction.class);

	@Override
	public void handleMessage(Connection connection, RoleService service, GCLoginRES message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.onLogin(connection, message, callback);
	}
}
