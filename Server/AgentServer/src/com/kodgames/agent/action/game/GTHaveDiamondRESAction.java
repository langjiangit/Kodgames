package com.kodgames.agent.action.game;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.kodgames.agent.service.diamond.DiamondService;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.message.proto.agent.AgentProtoBuf.GTHaveDiamondRES;
import com.kodgames.message.proto.agent.AgentProtoBuf.TCHaveDiamondRES;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

/**
* Agent向Client响应代理商钻石数量
* 
* @author 张斌
*/
@ActionAnnotation(messageClass = GTHaveDiamondRES.class, actionClass = GTHaveDiamondRESAction.class, serviceClass = DiamondService.class)
public class GTHaveDiamondRESAction extends ProtobufMessageHandler<DiamondService, GTHaveDiamondRES>
{
//	private Logger logger = LoggerFactory.getLogger(GTHaveDiamondRESAction.class);
	
	@Override
	public void handleMessage(Connection connection, DiamondService service, GTHaveDiamondRES message, int callback)
	{
		Connection clientConnection = ConnectionManager.getInstance().getConnection(message.getClientConnectionId());
		TCHaveDiamondRES.Builder haveDiamondRES = TCHaveDiamondRES.newBuilder();
		if(message.getResult() == PlatformProtocolsConfig.GT_HAVE_DIAMOND_RES_FAILED)
		{
			clientConnection.write(callback, haveDiamondRES.setResult(PlatformProtocolsConfig.TC_HAVE_DIAMOND_RES_FAILED).setDiamondHave(0).build());
			return;
		}
		//请求Web_AG代理商钻石数量并返回给客户端
		service.getDiamond(message.getAgentId(),message.getPhoneNum(),clientConnection,callback);
	}
}
