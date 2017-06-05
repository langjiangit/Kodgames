package com.kodgames.game.action.history;

import com.kodgames.game.start.CGProtobufMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.game.service.history.HistoryService;
import com.kodgames.message.proto.game.GameProtoBuf.CGHistoryREQ;
import com.kodgames.message.proto.game.GameProtoBuf.GCHistoryRES;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

@ActionAnnotation(messageClass = CGHistoryREQ.class, actionClass = CGHistoryREQAction.class, serviceClass = HistoryService.class)
public class CGHistoryREQAction extends CGProtobufMessageHandler<HistoryService, CGHistoryREQ>
{
	private static final Logger logger = LoggerFactory.getLogger(CGHistoryREQAction.class);

	@Override
	public void handleMessage(Connection connection, HistoryService service, CGHistoryREQ message, int callback)
	{
		logger.info("{} : {} -> {}.", getClass().getSimpleName(), connection.getConnectionID(), message);
		Integer roleId = connection.getRemotePeerID();

		int clubId = message.getClubId();

		GCHistoryRES.Builder builder = GCHistoryRES.newBuilder();
		builder.setResult(PlatformProtocolsConfig.GC_HISTORY_SUCCESS);

		if (clubId > 0) {
			int queryRoleId = message.getQueryRoleId() == 0 ? roleId : message.getQueryRoleId(); // 没有就是查自己
			builder.addAllRoomRecords(service.getClubHistoryList(
					queryRoleId,
					clubId,
					message.getVersion()
			));
			builder.setQueryRoleId(queryRoleId);
			builder.setClubId(clubId);
			builder.setVersion(service.getClubVersion(queryRoleId, clubId));
		} else {
			builder.setVersion(service.getVersion(roleId));
			builder.addAllRoomRecords(service.getHistoryList(roleId, message.getVersion()));
		}

		connection.write(callback, builder.build());
	}
}
