package com.kodgames.client;

import com.kodgames.client.common.client.ClientConfig;
import com.kodgames.client.common.client.ClientTestConstant;
import com.kodgames.client.constant.ClubConstants;
import com.kodgames.client.service.client.ClientService;
import com.kodgames.core.task.Task;
import com.kodgames.corgi.core.service.ServiceContainer;
import limax.xmlconfig.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestClient
{
	static
	{
		System.out.println("hello");
	}

	public static void main(String[] args)
		throws Exception
	{
		Service.addRunAfterEngineStartTask(() -> {
			ClientConfig.getInstance().init("test.xml");
			
			ClientService service = ServiceContainer.getInstance().getPublicService(ClientService.class);
			try
			{
			service.start();
			}
			catch (Exception e)
			{
			e.printStackTrace();
			}
			BufferedReader strin=new BufferedReader(new InputStreamReader(System.in));
			System.out.println("本程序会消耗比较大的cpu，压测人数最好不要超过1000，避免电脑卡死");
//			System.out.println("请输入压测的玩家数量：");
//			String str = "";
//			try
//			{
//			str = strin.readLine();
//			}
//			catch (IOException e)
//			{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			}
			
			int num = ClubConstants.PlayerNum;
//
//			try
//			{
//			num = Integer.valueOf(str);
//			}
//			catch (NumberFormatException e)
//			{
//			e.printStackTrace();
//			System.out.println("请输入正确的人数");
//			return;
//			}
//
//			if (num%4 != 0 || num <= 0)
//			{
//			System.out.println("人数必须是整数，且是4的倍数");
//			return;
//			}
			
			ClientTestConstant.THREAD_POOL_SIZE = (int)(num*0.6);
			if (ClientTestConstant.THREAD_POOL_SIZE < 1)
			{
			ClientTestConstant.THREAD_POOL_SIZE = 1;
			}
			
			StressTool2.getInstance().startTest(num);

//			TimerMgr.getTimerMgr().SetBaseTime(DateTimeUtil.getCurrentTimeMillis());
//			TimerMgr.getTimerMgr().start();
//			
//			final int ONE_MINUTE = 60*1000;
//			
//			long next = (DateTimeUtil.getCurrentTimeMillis()/ONE_MINUTE)*ONE_MINUTE+ONE_MINUTE;
//			
//			TimerPair pair = new TimerPair(next, 0);
//			List<TimerPair> list = new ArrayList<TimerPair>();
//			list.add(pair);
//			
//			TaskService.getInstance().registerScheduleTask(new MyTimer(num), new
//			TaskTimeInfo(System.currentTimeMillis(), 0, TimeUtils.Minute, 1, list));
		});
		Service.run("zdb_config.xml");
	}
}

class MyTimer implements Task
{
	private int num;

	public MyTimer(int num)
	{
		this.num = num;
	}

	public void run(long timer, int status)
	{
		StressTool2.getInstance().startTest(num);
	}
}
