package com.kodgames.corgi.core.net.server;


import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.atomic.AtomicInteger;

import com.kodgames.corgi.core.net.NetParameter;
import com.kodgames.corgi.core.net.handler.AbstractMessageInitializer;
import com.kodgames.corgi.core.net.handler.netty.HeartBeatHandler;
import com.kodgames.corgi.core.net.handler.netty.MessageProcessor;
import com.kodgames.corgi.core.net.handler.netty.SnappyFramedCodec;


public class DispatchNettyInitializer extends ChannelInitializer<Channel>
{
	static AtomicInteger handlerId=new AtomicInteger(0);
	AbstractMessageInitializer messageInitializer;
	LengthFieldPrepender lengthFieldPrepender;
	SnappyFramedCodec snappyFramedCodec;
	MessageProcessor messageProcessor;

	public DispatchNettyInitializer(AbstractMessageInitializer messageInitializer)
	{
		this.messageInitializer = messageInitializer;
		lengthFieldPrepender = new LengthFieldPrepender(4,false);
		snappyFramedCodec = new SnappyFramedCodec();
		messageProcessor = new MessageProcessor(messageInitializer);
	}

	@Override
	protected void initChannel(Channel ch) throws Exception
	{
		ChannelPipeline p = ch.pipeline();
		String nameId = "-" + handlerId.incrementAndGet();

		// length excludes the lenghFieldlength and the data decoded exclude the length field
		p.addLast("FrameDecoder" + nameId, new LengthFieldBasedFrameDecoder(204800, 0, 4, 0, 4));
		p.addLast("LengthFieldPrepender" + nameId, lengthFieldPrepender);
		p.addLast("IdleHandler", new IdleStateHandler(NetParameter.SSHeartHalfTimeout, 0, 0));
        p.addLast("HeartBeatHandler", new HeartBeatHandler());
		//p.addLast("DispatchHandler" + nameId, new DispatchHandler(messageInitializer));
		p.addLast("MessageProcessor" + nameId, messageProcessor);
	}
}
