package com.kodgames.authserver.action.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.authserver.service.server.ServerService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.message.proto.server.ServerProtoBuf.SSGetLaunchInfoRES;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

@ActionAnnotation(actionClass = SSGetLaunchInfoRESAction.class, messageClass = SSGetLaunchInfoRES.class, serviceClass = ServerService.class)
public class SSGetLaunchInfoRESAction extends ProtobufMessageHandler<ServerService, SSGetLaunchInfoRES>
{
	final static Logger logger = LoggerFactory.getLogger(SSGetLaunchInfoRESAction.class);

	@Override
	public void handleMessage(Connection connection, ServerService service, SSGetLaunchInfoRES message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		if (message.getResult() != PlatformProtocolsConfig.SS_GET_LAUNCH_INFO_SUCCESS)
		{
			logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!getlaunchInfo failed!!!!!!!!!!!!!!!!!!!!");
			return;
		}

		service.onGetLanchInfo(connection, message, callback);
	}
}