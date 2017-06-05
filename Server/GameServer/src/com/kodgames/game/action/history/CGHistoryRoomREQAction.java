package com.kodgames.game.action.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.game.start.CGProtobufMessageHandler;
import com.kodgames.game.service.history.HistoryService;
import com.kodgames.message.proto.game.GameProtoBuf.CGHistoryRoomREQ;
import com.kodgames.message.proto.game.GameProtoBuf.GCHistoryRoomRES;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

@ActionAnnotation(messageClass = CGHistoryRoomREQ.class, actionClass = CGHistoryRoomREQAction.class, serviceClass = HistoryService.class)
public class CGHistoryRoomREQAction extends CGProtobufMessageHandler<HistoryService, CGHistoryRoomREQ>
{
	private static final Logger logger = LoggerFactory.getLogger(CGHistoryREQAction.class);

	@Override
	public void handleMessage(Connection connection, HistoryService service, CGHistoryRoomREQ message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);

		if (service.getRoomHistory(message.getCreateTime(), message.getRoomId()) == null)
		{
			GCHistoryRoomRES.Builder builder = GCHistoryRoomRES.newBuilder();
			builder.setResult(PlatformProtocolsConfig.GC_HISTORY_FAILED_ROOM_HISTORY_NOT_EXIST);
			builder.setCreateTime(0);
			builder.setRoomId(0);
			builder.setQueryRoleId(0);
			connection.write(callback, builder.build());
			return ;
		}
		
		GCHistoryRoomRES.Builder builder = GCHistoryRoomRES.newBuilder();
		builder.setResult(PlatformProtocolsConfig.GC_HISTORY_ROOM_SUCCESS);
		builder.setRoomId(message.getRoomId());
		builder.setCreateTime(message.getCreateTime());
		builder.setQueryRoleId(message.getQueryRoleId());
		builder.addAllRoundReportRecords(service.getRoundReportDetailList(message.getCreateTime(), message.getRoomId()));

		connection.write(callback, builder.build());
	}
}
