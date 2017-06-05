package com.kodgames.client;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.common.client.ClientConfig;
import com.kodgames.client.common.client.Player;
import com.kodgames.client.common.client.card.DefaultHandCard;
import com.kodgames.client.start.NetInitializer;
import com.kodgames.corgi.core.net.Connection;

/**
 * h5版本的压测客户端
 * 
 * @author jiangzhen
 *
 */
public class StressToolH5 {
	private static Logger logger = LoggerFactory.getLogger(Player.class);
	
	/**
	 * 客户端数量
	 */
	private int clientCount = -1;
	
	/**
	 * 线程池
	 */
	private ExecutorService fixedThreadPool = null;
	
	/**
	 * 所有玩家的对应关系
	 */
	private ConcurrentHashMap<Integer, Player> dic_index_clients = new ConcurrentHashMap<Integer, Player>();
	
	/**
	 * 玩家是否登录成功
	 */
	private boolean[] loginSuccess;
	
	/**
	 * 已经登录的玩家数量
	 */
	private AtomicInteger loginNum = new AtomicInteger(0);
	
	/**
	 * 进入房间成功的人数
	 */
	private AtomicInteger enterRoomNum = new AtomicInteger(0);
	
	/**
	 * 标记顽疾
	 */
	
	/**
	 * 和game的连接
	 */
	private Connection gameConnection = null;
	
	/**
	 * 和battle的连接
	 */
	private Connection battleConnection = null;
	
	/**
	 * 网络层初始化
	 */
	private NetInitializer netInitializer = null;
	
	private static StressToolH5 instance;
	
	/**
	 * 构造函数
	 */
	private StressToolH5()
	{
		this.fixedThreadPool = Executors.newFixedThreadPool(1);
	}
	
	public static StressToolH5 getInstance()
	{
		if(instance == null)
		{
			synchronized (StressToolH5.class) 
			{
				if (instance == null){
					instance = new StressToolH5();	
				}
			}
		}
			
		return instance;
	}
	
	public Connection getGameConnection()
	{
		return this.gameConnection;
	}
	
	public Connection getConnection()
	{
		return this.battleConnection;
	}
	
	public Player getPlayerByIndex(int index)
	{
		return this.dic_index_clients.get(index);
	}
	
	public void playerEnterRoom()
	{
		this.enterRoomNum.incrementAndGet();
	}
	
	public boolean isAllEnterRoom()
	{
		return this.enterRoomNum.get()==this.clientCount;
	}
	
	public int enterRoomNum()
	{
		return this.enterRoomNum.get();
	}
	
	public void resetEnterRoomNum()
	{
		this.enterRoomNum.set(0);
	}
	
	public synchronized void playerLoginSuccess(int index)
	{
		this.loginNum.incrementAndGet();
//		logger.warn("loginNum={},Index={} login success!!!!!", loginNum.get(), index);	
		if (this.loginNum.get() == this.clientCount)
		{
			logger.warn("All players login success!!!!!!!");
		}
	}
	public void resetLoginNum()
	{
		this.loginNum.set(0);
	}
	
	/**
	 * 得到线程池
	 */
	public ExecutorService getThreadPool()
	{
		return this.fixedThreadPool;
	}
	
	/**
	 * 把一个任务放入线程池中
	 */
//	public void putTaskIntoThreadPool(Runnable command)
//	{
//		this.fixedThreadPool.execute(command);
//	}
	
	/**
	 * 设置某个玩家为登录成功
	 * @param index 玩家下标
	 */
	public void setLoginSuccess(int index)
	{
		this.loginSuccess[index] = true;
	}
	
	/**
	 * 判断和玩家在同一个房间的所有玩家是否全部登录成功
	 * 
	 * @param index 进行判断的玩家
	 */
	public boolean isGroupAllLoginSuccess(int index)
	{
		// 和index在同一房间，但是下标比index小的玩家数量
		int leftNum = index%4;
		
		// 和index在同一房间，但是下标比index大的玩家数量
		int rightNum = 4-index%4-1;
		
		// 房主下标
		int hostIndex = index-index%4;
		
		this.loginSuccess[index] = true;
		
		for (int i = 0; i < leftNum; ++i)
		{
			if ((hostIndex+i+1) >= this.clientCount)
			{
				break;
			}
			if (!this.loginSuccess[i+hostIndex])
			{
				return false;
			}
		}
		
		for (int i = 0; i < rightNum; ++i)
		{
			if ((hostIndex+i+1) >=  this.clientCount)
			{
				break;
			}
			if (!this.loginSuccess[i+index+1])
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 房主创建房间
	 * 
	 * @param index 房间中最后一个登陆成功的玩家
	 */
	public void hostCreateRoom(int index)
	{
		// 得到房主下标
		int hostIndex = index-index % 4;
		Player player = this.dic_index_clients.get(hostIndex);
		player.createRoom(2, 2);
	}
	
	/**
	 * 和房主在同一房间的玩家对房间发送查询房间请求
	 * 
	 * @param index 房主下标
	 */
	public void playerQueryRoom(int index)
	{
		if (index < 0 || index >= this.clientCount)
		{
			logger.error("ClientThread RemainPlayerEnterRoom() error: index out bound");
			return;
		}

		Player player = this.dic_index_clients.get(index);

		// 遍历和房主在同一房间的玩家
		for (int i = index + 1; i < index + 4; ++i)
		{
			if (i >= this.clientCount)
			{
				break;
			}
			Player p = this.dic_index_clients.get(i);
			p.queryBattleId(player.getRoom().getRoomId());
//			this.putTaskIntoThreadPool(new PlayerQueryBattleIdTask(p, player.getRoomId()));
		} // end of for (int i = index+1; i < index+4; ++i)
	}
	
	/**
	 * 发送聊天请求定时器
	 */
	private class ChatSender extends TimerTask
	{
		private int i = -1;
		private int type = -1;
		private int index = 0;
		
		public ChatSender(int index)
		{
			this.type = 1;
			this.i = 0;
			this.index = index;
		}
		
		public void run()
		{
			Player player = dic_index_clients.get(this.index+this.i);
			player.chat(this.type, "aaaaaa", 1);
			
			i = (i+1)%4;
			type = (type+1)%4+1;
		}
	}
	
	/**
	 * 聊天测试开启
	 */
	public void startChat(int index)
	{
		new Timer().scheduleAtFixedRate(new ChatSender(index), 0, ClientConfig.getInstance().getChatInterval());
	}
	
	private class MarqueeSender extends TimerTask
	{
		private int i = -1;
		private int index = -1;
		
		public MarqueeSender(int index)
		{
			this.index = index;
			this.i = 0;
		}
		
		public void run()
		{
			Player player = dic_index_clients.get(this.index+this.i);
			if (player != null)
			{
//				player.sendMarquee();
			}
			
			i = (i+1)%4;
		}
	}
	
	/**
	 * 跑马灯测试开启
	 */
	public void startMarquee(int index)
	{
		new Timer().scheduleAtFixedRate(new MarqueeSender(index), 0, ClientConfig.getInstance().getMarqueeInterval());
	}
	
	public void init(int clientCount)
	{
		if (this.netInitializer == null)
		{
			netInitializer = new NetInitializer();
			try
			{
				netInitializer.init();
			}
			catch (Exception e)
			{
				logger.debug("StressTool2 init fails, exception={}", e);
				return ;
			}
		}
		
		this.clientCount = clientCount;
	}
	
	/**
	 * 开启测试
	 */
	public void startTest()
	{		
		// 设置玩家登陆成功标志
		int loginSuccessSize = clientCount%4==0?clientCount:clientCount/4*4+4;
		this.loginSuccess = new boolean[loginSuccessSize];
		for (int i = 0; i < clientCount; ++i)
		{
			loginSuccess[i] = false;
		}
		
		logger.warn("Player start login........");
		
		for (int i = 0; i < clientCount; ++i)
		{
			Player player = new Player(i, new DefaultHandCard());
			
			String name = UUID.randomUUID().toString()+"StressTest_"
				+ String.valueOf(ThreadLocalRandom.current().nextInt(1000));
			player.setUsername(name);
			
			dic_index_clients.put(i, player);
			fixedThreadPool.execute(()->{
				player.connect();
			});
		}
	}

	public void allVoteDestory()
	{
		
	}
}
