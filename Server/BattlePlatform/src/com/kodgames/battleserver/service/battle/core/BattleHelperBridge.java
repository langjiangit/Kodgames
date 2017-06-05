package com.kodgames.battleserver.service.battle.core;

import com.kodgames.battleserver.service.battle.common.xbean.BattleRoom;

/**
 * 获取battleHelper，单例，再启动的时候初始化，启动时候初始化一次，调用的时候调用battleHelper的copy
 * 
 * @author 毛建伟
 */
public class BattleHelperBridge
{
	private IBattleHelper battleHelper;

	private static class InnerClass
	{
		private static final BattleHelperBridge instance = new BattleHelperBridge();
	}

	private BattleHelperBridge()
	{

	}

	public static BattleHelperBridge getInstance()
	{
		return InnerClass.instance;
	}

	public IBattleHelper getBattleHelper()
	{
		return battleHelper;
	}

	public synchronized void setBattleHelper(IBattleHelper battleHelper)
	{
		if (this.battleHelper == null)
		{
			this.battleHelper = battleHelper;
		}
	}

	public IBattleHelper newInstance(BattleRoom roomInfo)
	{
		return this.battleHelper.copy(roomInfo);
	}

}
