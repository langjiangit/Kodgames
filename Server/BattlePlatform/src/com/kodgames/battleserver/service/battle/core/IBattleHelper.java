package com.kodgames.battleserver.service.battle.core;

import com.kodgames.battleserver.common.Macro;
import com.kodgames.battleserver.common.ThreadLocolVariable;
import com.kodgames.battleserver.service.battle.common.xbean.BattleRoom;
import com.kodgames.battleserver.service.battle.common.xbean.IBattleBean;
import com.kodgames.message.proto.battle.BattleProtoBuf.MatchPlaybackPROTO;

import net.sf.json.JSONObject;

public interface IBattleHelper
{
	boolean isRunning();
	
	void setCurrentInstance();
	
	void resetCurrentInstance();
	
	void rejoin(int roleId);
	
	void startBattle(JSONObject object);
	
	boolean processStep(int roleId, int playType, byte[] cards);
	
	static IBattleHelper getInstance()
	{
		IBattleHelper instance = (IBattleHelper)ThreadLocolVariable.Get();
		Macro.AssetFalse(instance != null);
		return instance;
	}
	
	void setRoomInfo(BattleRoom room);
	
	BattleRoom getRoomInfo();
	
	IBattleBean getBattleBean();
	
	IBattleHelper copy(BattleRoom roomInfo);
	
	MatchPlaybackPROTO generatePlaybackData();
	
	int getPlayCardCount();

	boolean enableMutilHu();
}
