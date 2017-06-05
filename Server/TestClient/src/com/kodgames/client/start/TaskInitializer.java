package com.kodgames.client.start;

import com.kodgames.client.timer.PlayCardReqNumTimer;
import com.kodgames.client.timer.PlayerResponseTimer;
import com.kodgames.client.timer.RoomRecvStepNumTimer;
import com.kodgames.core.timer.TimerMgr;
import com.kodgames.corgi.core.util.DateTimeUtil;

/**
 * 初始化定时器任务
 * @author jiangzhen
 *
 */
public class TaskInitializer
{
	private static TaskInitializer ti = null;
	
	private TaskInitializer()
	{
		
	}
	
	public static TaskInitializer getInstance()
	{
		if (ti == null)
		{
			synchronized (TaskInitializer.class)
			{
				if (ti == null)
				{
					ti = new TaskInitializer();
				}
			}
		}
		
		return ti;
	}
	
	public void init()
	{
		TimerMgr.getTimerMgr().SetBaseTime(DateTimeUtil.getCurrentTimeMillis());
		TimerMgr.getTimerMgr().start();
		
		PlayCardReqNumTimer.getInstance().init();
		PlayerResponseTimer.getInstance().init();
		RoomRecvStepNumTimer.getInstance().init();
	}
}
