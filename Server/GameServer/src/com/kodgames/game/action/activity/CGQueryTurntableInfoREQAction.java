package com.kodgames.game.action.activity;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.game.service.activity.ActivityService;
import com.kodgames.game.start.CGProtobufMessageHandler;
import com.kodgames.message.proto.activity.ActivityProtoBuf.CGQueryTurntableInfoREQ;

@ActionAnnotation(messageClass = CGQueryTurntableInfoREQ.class, actionClass = CGQueryTurntableInfoREQAction.class, serviceClass = ActivityService.class)
public class CGQueryTurntableInfoREQAction extends CGProtobufMessageHandler<ActivityService, CGQueryTurntableInfoREQ>
{

	@Override
	public void handleMessage(Connection connection, ActivityService service, CGQueryTurntableInfoREQ message, int callback)
	{
		service.onCGQueryTurntableInfoREQ(connection, message, callback);
	}

}
