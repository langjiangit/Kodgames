package com.kodgames.client.action.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.account.RoleService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.auth.AuthProtoBuf.ICAccountAuthRES;
import com.kodgames.message.proto.game.GameProtoBuf.GCLogoutRES;

@ActionAnnotation(actionClass = ICAccountAuthRESAction.class, messageClass = ICAccountAuthRES.class, serviceClass = RoleService.class)
public class ICAccountAuthRESAction extends ProtobufMessageHandler<RoleService, ICAccountAuthRES>
{
	private static final Logger logger = LoggerFactory.getLogger(ICAccountAuthRESAction.class);

	@Override
	public void handleMessage(Connection connection, RoleService service, ICAccountAuthRES message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		logger.info("{} <---> {}", connection.getConnectionID(), message.getRoleId());
		service.onAccountAuth(connection, message, callback);
	}
}
