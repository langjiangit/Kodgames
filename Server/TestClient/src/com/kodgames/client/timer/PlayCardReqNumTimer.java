package com.kodgames.client.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.core.task.TaskService;
import com.kodgames.core.task.timeEvent.TaskTimeInfo;
import com.kodgames.core.timer.TimeUtils;
import com.kodgames.core.timer.event.TimerPair;

/**
 * 统计一段时间内的打牌请求数定时器
 * 
 * @author jiangzhen
 *
 */
public class PlayCardReqNumTimer
{
	private static PlayCardReqNumTimer pcrt = null;
	private AtomicLong num = null;
	private static Logger logger = LoggerFactory.getLogger(PlayCardReqNumTimer.class);
	
	/**
	 * 程序启动TIME_START时间后，执行定时器
	 */
	private static int TIME_START_PERIOD = 20000;
	
	/**
	 * 记录请求数的时间间隔(ms)
	 */
	private static int TIME_PERIOD = 10000;

	private PlayCardReqNumTimer()
	{
		num = new AtomicLong(0);
	}

	public static PlayCardReqNumTimer getInstance()
	{
		if (pcrt == null)
		{
			synchronized (PlayCardReqNumTimer.class)
			{
				if (pcrt == null)
				{
					pcrt = new PlayCardReqNumTimer();
				}
			}
		}

		return pcrt;
	}

	public void init()
	{
		long now = System.currentTimeMillis();
		List<TimerPair> list = new ArrayList<TimerPair>();
		TimerPair pair = new TimerPair(now+TIME_START_PERIOD, 0);
		list.add(pair);

		TaskTimeInfo timeInfo = new TaskTimeInfo(now, 0, TimeUtils.Second, TIME_PERIOD/1000, list);
		TaskService.getInstance().registerScheduleTask((time, arg)->{
			logger.warn("play card req num:"+this.num.get());
			this.num.set(0);
		}, timeInfo);
	}
	
	public void addReqNum()
	{
		this.num.incrementAndGet();
	}
}
