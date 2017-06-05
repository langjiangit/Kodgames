package com.kodgames.game.action.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.net.common.ActionAnnotation;
import com.kodgames.game.service.history.HistoryService;
import com.kodgames.game.start.CGProtobufMessageHandler;
import com.kodgames.message.proto.game.GameProtoBuf.CGHistoryPlaybackREQ;
import com.kodgames.message.proto.game.GameProtoBuf.GCHistoryPlaybackRES;

/**
 * 查询战绩回放数据
 */
@ActionAnnotation(messageClass = CGHistoryPlaybackREQ.class, actionClass = CGHistoryPlaybackREQAction.class, serviceClass = HistoryService.class)
public class CGHistoryPlaybackREQAction extends CGProtobufMessageHandler<HistoryService, CGHistoryPlaybackREQ>
{
	private static final Logger logger = LoggerFactory.getLogger(CGHistoryPlaybackREQAction.class);

	@Override
	public void handleMessage(Connection connection, HistoryService service, CGHistoryPlaybackREQ message, int callback)
	{
		logger.info("QueryHistoryPlaybackDataReq: roomId={} createTime={} index={}.", message.getRoomId(), message.getCreatTime(), message.getRecordIndex());
		GCHistoryPlaybackRES.Builder builder = service.queryPlaybackData(message);
		connection.write(callback, builder.build());
	}
}
