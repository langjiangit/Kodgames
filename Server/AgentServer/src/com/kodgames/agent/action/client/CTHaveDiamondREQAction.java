package com.kodgames.agent.action.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.agent.service.server.ServerService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.corgi.core.session.ConnectionManager;

import com.kodgames.message.proto.agent.AgentProtoBuf.CTHaveDiamondREQ;
import com.kodgames.message.proto.agent.AgentProtoBuf.TCHaveDiamondRES;
import com.kodgames.message.proto.agent.AgentProtoBuf.TGHaveDiamondREQ;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

/**
 * client向Agent请求代理商钻石数量
 * 
 * @author 张斌
 */
@ActionAnnotation(messageClass = CTHaveDiamondREQ.class, actionClass = CTHaveDiamondREQAction.class, serviceClass = ServerService.class)
public class CTHaveDiamondREQAction extends ProtobufMessageHandler<ServerService, CTHaveDiamondREQ>
{
	private Logger logger = LoggerFactory.getLogger(CTHaveDiamondREQAction.class);

	@Override
	public void handleMessage(Connection connection, ServerService service, CTHaveDiamondREQ message, int callback)
	{
		ConnectionManager.getInstance().addConnection(connection);
		Connection gameConnection = service.getGameConnection();

		if (gameConnection == null)
		{
			logger.warn("CTHaveDiamondREQAction : gameConnection is null");
			connection.writeAndClose(callback, TCHaveDiamondRES.newBuilder().setResult(PlatformProtocolsConfig.TC_HAVE_DIAMOND_RES_NOConnection).setDiamondHave(0).build());
			return;
		}

		// 通过ID获取agentID、phone
		gameConnection.write(callback, TGHaveDiamondREQ.newBuilder().setClientConnectionId(connection.getConnectionID()).setRoleId(connection.getRemotePeerID()).build());
	}

}