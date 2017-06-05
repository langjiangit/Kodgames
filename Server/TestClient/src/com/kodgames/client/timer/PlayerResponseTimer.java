package com.kodgames.client.timer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.StressToolH5;
import com.kodgames.client.common.client.Player;
import com.kodgames.client.util.response.PlayerResponseTimeUtil;
import com.kodgames.core.task.TaskService;
import com.kodgames.core.task.timeEvent.TaskTimeInfo;
import com.kodgames.core.timer.TimeUtils;
import com.kodgames.core.timer.event.TimerPair;

/**
 * 测试玩家响应时间的定时器
 * @author jiangzhen
 *
 */
public class PlayerResponseTimer
{
	private static Logger logger = LoggerFactory.getLogger(PlayerResponseTimer.class);
	private static long TIME_START_PERIOD = 20000;
	private static int TIME_PERIOD = 10000;
	
	/**
	 * 统计的玩家总数
	 */
	private static int TEST_PLAYER_NUM = 10;
	
	private static PlayerResponseTimer prt = null;
	private PlayerResponseTimer()
	{
		
	}
	public static PlayerResponseTimer getInstance()
	{
		if (prt == null)
		{
			synchronized(PlayerResponseTimer.class)
			{
				if (prt == null)
				{
					prt = new PlayerResponseTimer();
				}
			}
		}
		return prt;
	}
	
	public void init()
	{
		long now = System.currentTimeMillis();
		List<TimerPair> list = new ArrayList<TimerPair>();
		TimerPair pair = new TimerPair(now+TIME_START_PERIOD, 0);
		list.add(pair);

		TaskTimeInfo timeInfo = new TaskTimeInfo(now, 0, TimeUtils.Second, TIME_PERIOD/1000, list);
		TaskService.getInstance().registerScheduleTask((time, arg)->{
			float sum = 0;
			int playerNum = 0;
			for (int i = 0; i < TEST_PLAYER_NUM; ++i)
			{
				Player player = StressToolH5.getInstance().getPlayerByIndex(i);
				if (player == null)
				{
					//如果player是null,说明后面的player也是null
					break;
				}
				float avgTime = PlayerResponseTimeUtil.getInstance().getAvgResponseTimeAndClear(player.getRoleId());
				sum += avgTime;
				playerNum++;
				logger.warn("roleId="+player.getRoleId()+" avg response time: "+avgTime);
			}
			if (playerNum == 0)
			{
				logger.warn("test playerNum=0");
			}
			else
			{
				logger.warn("above "+playerNum+" player avg response time: "+sum/playerNum);
			}
		}, timeInfo);
	}
}
