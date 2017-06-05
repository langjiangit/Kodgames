package com.kodgames.client.action.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.account.RoleService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.game.GameProtoBuf.GCLogoutRES;

@ActionAnnotation(actionClass = GCLogoutRESAction.class, messageClass = GCLogoutRES.class, serviceClass = RoleService.class)
public class GCLogoutRESAction extends ProtobufMessageHandler<RoleService, GCLogoutRES>
{
	private static final Logger logger = LoggerFactory.getLogger(GCLogoutRESAction.class);

	@Override
	public void handleMessage(Connection connection, RoleService service, GCLogoutRES message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.onLogout(connection, message, callback);
	}
}
