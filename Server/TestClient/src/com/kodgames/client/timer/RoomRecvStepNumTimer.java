package com.kodgames.client.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.common.client.Room;
import com.kodgames.client.service.room.RoomService;
import com.kodgames.core.task.TaskService;
import com.kodgames.core.task.timeEvent.TaskTimeInfo;
import com.kodgames.core.timer.TimeUtils;
import com.kodgames.core.timer.event.TimerPair;
import com.kodgames.corgi.core.service.ServiceContainer;

public class RoomRecvStepNumTimer
{
	private static final Logger logger = LoggerFactory.getLogger(RoomRecvStepNumTimer.class);
	private static final long TIME_START_PERIOD = 20000;
	private static final int TIME_PERIOD = 10000;
	private static RoomRecvStepNumTimer rrsnt = null;
	private RoomRecvStepNumTimer()
	{
		
	}
	public static RoomRecvStepNumTimer getInstance()
	{
		if (rrsnt == null)
		{
			synchronized(RoomRecvStepNumTimer.class)
			{
				if (rrsnt == null)
				{
					rrsnt = new RoomRecvStepNumTimer();
				}
			}
		}
		return rrsnt;
	}
	
	public void init()
	{
		long now = System.currentTimeMillis();
		List<TimerPair> list = new ArrayList<TimerPair>();
		TimerPair pair = new TimerPair(now+TIME_START_PERIOD, 0);
		list.add(pair);

		TaskTimeInfo timeInfo = new TaskTimeInfo(now, 0, TimeUtils.Second, TIME_PERIOD/1000, list);
		TaskService.getInstance().registerScheduleTask((time, arg)->{
			RoomService roomService = ServiceContainer.getInstance().getPublicService(RoomService.class);
			Map<Integer, Room> map = roomService.getIdRoomMap();
			@SuppressWarnings("unused")
			int num = 0;
			for (Room room : map.values())
			{
				AtomicLong stepNum = room.getStepNum();
				if (stepNum.get() == room.getOldStepNum())
				{
					logger.warn("roomId="+room.getRoomId()+" is slient");
					++num;
				}
				else
				{
					room.setOldStepNum(stepNum.get());
				}
			}
			
			logger.warn("room num: "+roomService.getRoomNum());
		}, timeInfo);
	}
}
