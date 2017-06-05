/*
 * 2016.10.18
 * 
 * by jiangzhen
 * 
 * 在线人数记录
 */

package com.kodgames.game.service.role;

import java.util.ArrayList;
import java.util.List;

import com.kodgames.core.task.Task;
import com.kodgames.core.task.TaskService;
import com.kodgames.core.task.timeEvent.TaskTimeInfo;
import com.kodgames.core.timer.TimeUtils;
import com.kodgames.core.timer.event.TimerPair;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.corgi.core.util.DateTimeUtil;
import com.kodgames.game.util.KodBiLogHelper;

/**
 * 定期统计在线玩家数的任务
 */
public class OnlineRecordTask 
{
	public void init()
	{
		final int TEN_MINS =  10 * 60 * 1000;
		
		//变成以十分钟为基础整点输出 先秒数去余，然后在乘回来
		long now = (DateTimeUtil.getCurrentTimeMillis()/TEN_MINS)*TEN_MINS - TEN_MINS;
		List<TimerPair> list = new ArrayList<TimerPair>();	
		//十分钟分钟执行1次，一个小时执行六次
		for (int i = 1; i <= 6; i++)
		{
			TimerPair pair = new TimerPair(now + (i * TEN_MINS), i);
			list.add(pair);
		}

		TaskTimeInfo timeInfo = new TaskTimeInfo(now - 10, 0, TimeUtils.Hour, 1, list);
		TaskService.getInstance().registerScheduleTask(new Task()
		{
			public void run(long arg0, int arg1) {
				RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
				int onlineNum = roleService.getOnlinePlayerCount();
				KodBiLogHelper.onlinePlayersLog(onlineNum);	
			}
			
		}, timeInfo);
	}
}
