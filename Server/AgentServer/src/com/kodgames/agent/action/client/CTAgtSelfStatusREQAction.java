//package com.kodgames.agent.action.client;
//
//import com.kodgames.agent.service.server.ServerService;
//import com.kodgames.corgi.core.net.Connection;
//import com.kodgames.corgi.core.net.common.ActionAnnotation;
//import com.kodgames.corgi.core.net.handler.message.ProtobufMessageHandler;
//import com.kodgames.message.proto.agent.AgentProtoBuf.CTAgtSelfStatusREQ;
//
///**
// * client向Agent请求代理商状态
// * 
// * @author 张斌
// */
//@ActionAnnotation(messageClass = CTAgtSelfStatusREQ.class, actionClass = CTAgtSelfStatusREQAction.class, serviceClass = ServerService.class)
//public class CTAgtSelfStatusREQAction extends ProtobufMessageHandler<ServerService, CTAgtSelfStatusREQ>
//{
//	@Override
//	public void handleMessage(Connection connection,ServerService service,CTAgtSelfStatusREQ message,int callback)
//	{
//		service.checkAgtStatus(connection, message, callback);
//	}
//}
