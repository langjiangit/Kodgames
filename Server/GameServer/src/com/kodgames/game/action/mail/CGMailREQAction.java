package com.kodgames.game.action.mail;

import com.kodgames.game.start.CGProtobufMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.game.service.mail.MailService;
import com.kodgames.message.proto.mail.MailProtoBuf;

/**
 * Created by lz on 2016/8/27.
 * 请求邮件列表
 */
@ActionAnnotation(messageClass = MailProtoBuf.CGMailREQ.class,
				  actionClass = CGMailREQAction.class,
				  serviceClass = MailService.class)
public class CGMailREQAction extends CGProtobufMessageHandler<MailService, MailProtoBuf.CGMailREQ>
{
	private static final Logger logger = LoggerFactory.getLogger(CGMailREQAction.class);
	
	@Override public void handleMessage(Connection connection, MailService service, MailProtoBuf.CGMailREQ message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		service.getNewMailList(connection, callback, message.getTime());
	}
}
