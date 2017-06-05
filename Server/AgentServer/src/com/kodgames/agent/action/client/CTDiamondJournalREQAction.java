package com.kodgames.agent.action.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.agent.service.server.ServerService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.message.proto.agent.AgentProtoBuf.CTDiamondJournalREQ;
import com.kodgames.message.proto.agent.AgentProtoBuf.TCDiamondJournalRES;
import com.kodgames.message.proto.agent.AgentProtoBuf.TGDiamondJournalREQ;
import com.kodgames.message.protocol.PlatformProtocolsConfig;


/**
 * client向Agent请求代理商钻石记录
 * 
 * @author 张斌
 */
@ActionAnnotation(messageClass = CTDiamondJournalREQ.class, actionClass = CTDiamondJournalREQAction.class, serviceClass = ServerService.class)
public class CTDiamondJournalREQAction extends ProtobufMessageHandler<ServerService, CTDiamondJournalREQ>
{
	private Logger logger = LoggerFactory.getLogger(CTDiamondJournalREQAction.class);

	@Override
	public void handleMessage(Connection connection, ServerService service, CTDiamondJournalREQ message, int callback)
	{
		ConnectionManager.getInstance().addConnection(connection);
		Connection gameConnection = service.getGameConnection();

		if (gameConnection == null)
		{
			logger.warn("CTDiamondJournalREQAction : gameConnection is null");
			connection.writeAndClose(callback, TCDiamondJournalRES.newBuilder().setResult(PlatformProtocolsConfig.TC_DIAMOND_JOURNAL_RES_NOConnection).build());
			return;
		}

		// 通过ID获取agentID、phone
//		gameConnection.write(callback, TGDiamondJournalREQ.newBuilder().setClientConnectionId(connection.getConnectionID()).setRoleId(connection.getRemotePeerID()).se.build());
	}

}