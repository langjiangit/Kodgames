package com.kodgames.battleserver.service.battle.common.xbean;

import java.util.ArrayList;
import java.util.List;

/** 战斗房间信息 */
public final class BattleRoom
{
	/** 房间id */
	private int roomId;

	/** 房间类型 */
	private int roomType;

	/** 对应GameServer中rule.xml */
	private int countType;

	/** 总圈数 */
	private int totalRoundCount;

	/** 总局数 */
	private int totalGameCount;

	/** 完成的圈数 */
	private int overRoundCount;

	/** 完成的局数 */
	private int overGameCount;

	/** 创建房间玩家id */
	private int creatorId;

	/** 房间状态 */
	private int status;

	/** 房间支付方式 */
	private int payType;

	/** 是否开启语音 */
	private boolean voice;

	/** 房间创建时间 */
	private long createTime;

	/** 房间内玩家 */
	private ArrayList<RoomPlayerInfo> players;

	/** 房间规则 */
	private ArrayList<Integer> gameplays;

	/** 每局战斗记录 */
	private ArrayList<IBattleBean> games;

	/** 房间投票解散信息 */
	private RoomVoteInfo voteInfo;

	/** 同IP检测信息 */
	private SameIpInfo sameIpCache;

	/** 房间内最大玩家数量 */
	private int maxMemberCount;

	/** 房间续房信息 */
	private int isContinueInfo;

	/** 续房房间号 */
	private int continueRoomId;

	/** 房间内真实已离开玩家列表 */
	private ArrayList<RoomPlayerInfo> alreadyQuitPlayers;

	public BattleRoom()
	{
		gameplays = new ArrayList<Integer>();
		players = new ArrayList<RoomPlayerInfo>();
		games = new ArrayList<IBattleBean>();
		alreadyQuitPlayers = new ArrayList<RoomPlayerInfo>();
		voteInfo = new RoomVoteInfo();
		sameIpCache = new SameIpInfo();
	}

	public BattleRoom(BattleRoom battleRoom)
	{
		this();
		copyFrom(battleRoom);
	}

	public void copyFrom(BattleRoom battleRoom)
	{
		this.roomId = battleRoom.roomId;
		this.roomType = battleRoom.roomType;
		this.countType = battleRoom.countType;
		this.totalRoundCount = battleRoom.totalRoundCount;
		this.totalGameCount = battleRoom.totalGameCount;
		this.overRoundCount = battleRoom.overRoundCount;
		this.overGameCount = battleRoom.overGameCount;
		this.creatorId = battleRoom.creatorId;
		this.status = battleRoom.status;
		this.payType = battleRoom.payType;
		this.createTime = battleRoom.createTime;
		this.voteInfo.copyFrom(battleRoom.voteInfo);
		this.sameIpCache.copyFrom(battleRoom.sameIpCache);
		this.maxMemberCount = battleRoom.maxMemberCount;
		this.isContinueInfo = battleRoom.isContinueInfo;
		this.continueRoomId = battleRoom.continueRoomId;

		this.gameplays.clear();
		this.gameplays.addAll(battleRoom.gameplays);

		this.players.clear();
		battleRoom.players.forEach(_v_ -> this.players.add(new RoomPlayerInfo(_v_)));

		this.games.clear();
		battleRoom.games.forEach(_v_ -> this.games.add(_v_.copy()));
		this.alreadyQuitPlayers.clear();
		battleRoom.alreadyQuitPlayers.forEach(_v_ -> this.alreadyQuitPlayers.add(new RoomPlayerInfo(_v_)));
	}

	public int getRoomId()
	{
		return this.roomId;
	}

	public int getRoomType()
	{
		return this.roomType;
	}

	public int getCountType()
	{
		return this.countType;
	}

	public List<Integer> getGameplays()
	{
		return this.gameplays;
	}

	public int getTotalRoundCount()
	{
		return this.totalRoundCount;
	}

	public int getTotalGameCount()
	{
		return this.totalGameCount;
	}

	public int getOverRoundCount()
	{
		return this.overRoundCount;
	}

	public int getOverGameCount()
	{
		return this.overGameCount;
	}

	public int getCreatorId()
	{
		return this.creatorId;
	}

	public List<RoomPlayerInfo> getPlayers()
	{
		return this.players;
	}

	public List<IBattleBean> getGames()
	{
		return this.games;
	}

	public RoomVoteInfo getVoteInfo()
	{
		return this.voteInfo;
	}

	public SameIpInfo getSameIpCache()
	{
		return this.sameIpCache;
	}

	public int getStatus()
	{
		return this.status;
	}

	public int getPayType()
	{
		return this.payType;
	}

	public boolean isVoice()
	{
		return this.voice;
	}

	public long getCreateTime()
	{
		return this.createTime;
	}

	public int getMaxMemberCount()
	{
		return this.maxMemberCount;
	}

	public int getContinueInfo()
	{
		return this.isContinueInfo;
	}
	
	public int getContinueRoomId()
	{
		return this.continueRoomId;
	}

	public List<RoomPlayerInfo> getAlreadyQuitPlayers()
	{
		return this.alreadyQuitPlayers;
	}

	public void setRoomId(int roomId)
	{
		this.roomId = roomId;
	}

	public void setRoomType(int roomType)
	{
		this.roomType = roomType;
	}

	public void setCountType(int countType)
	{
		this.countType = countType;
	}

	public void setTotalRoundCount(int totalRoundCount)
	{
		this.totalRoundCount = totalRoundCount;
	}

	public void setTotalGameCount(int totalGameCount)
	{
		this.totalGameCount = totalGameCount;
	}

	public void setOverRoundCount(int overRoundCount)
	{
		this.overRoundCount = overRoundCount;
	}

	public void setOverGameCount(int overGameCount)
	{
		this.overGameCount = overGameCount;
	}

	public void setCreatorId(int creatorId)
	{
		this.creatorId = creatorId;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public void setPayType(int payType)
	{
		this.payType = payType;
	}

	public void setVoice(boolean voice)
	{
		this.voice = voice;
	}

	public void setCreateTime(long createTime)
	{
		this.createTime = createTime;
	}

	public void setMaxMemberCount(int maxMemberCount)
	{
		this.maxMemberCount = maxMemberCount;
	}
	
	public void setContinueRoomId(int continueRoomId)
	{
		this.continueRoomId = continueRoomId;
	}

	public void setContinueInfo(int continueInfo)
	{
		this.isContinueInfo = continueInfo;
	}

	@Override
	public final boolean equals(Object objec)
	{
		if (objec instanceof BattleRoom == false)
			return false;

		BattleRoom battleRoom = (BattleRoom)objec;
		if (this.roomId != battleRoom.roomId)
			return false;
		if (this.roomType != battleRoom.roomType)
			return false;
		if (this.countType != battleRoom.countType)
			return false;
		if (!this.gameplays.equals(battleRoom.gameplays))
			return false;
		if (this.totalRoundCount != battleRoom.totalRoundCount)
			return false;
		if (this.totalGameCount != battleRoom.totalGameCount)
			return false;
		if (this.overRoundCount != battleRoom.overRoundCount)
			return false;
		if (this.overGameCount != battleRoom.overGameCount)
			return false;
		if (this.creatorId != battleRoom.creatorId)
			return false;
		if (!this.players.equals(battleRoom.players))
			return false;
		if (!this.games.equals(battleRoom.games))
			return false;
		if (!this.voteInfo.equals(battleRoom.voteInfo))
			return false;
		if (!this.sameIpCache.equals(battleRoom.sameIpCache))
			return false;
		if (this.status != battleRoom.status)
			return false;
		if (this.payType != battleRoom.payType)
			return false;
		if (this.createTime != battleRoom.createTime)
			return false;
		if (this.isContinueInfo != battleRoom.isContinueInfo)
			return false;
		if (this.continueRoomId != battleRoom.continueRoomId)
			return false;
		if (this.alreadyQuitPlayers != battleRoom.alreadyQuitPlayers)
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int _h_ = 0;
		_h_ += _h_ * 31 + this.roomId;
		_h_ += _h_ * 31 + this.roomType;
		_h_ += _h_ * 31 + this.countType;
		_h_ += _h_ * 31 + this.gameplays.hashCode();
		_h_ += _h_ * 31 + this.totalRoundCount;
		_h_ += _h_ * 31 + this.totalGameCount;
		_h_ += _h_ * 31 + this.overRoundCount;
		_h_ += _h_ * 31 + this.overGameCount;
		_h_ += _h_ * 31 + this.creatorId;
		_h_ += _h_ * 31 + this.players.hashCode();
		_h_ += _h_ * 31 + this.games.hashCode();
		_h_ += _h_ * 31 + this.voteInfo.hashCode();
		_h_ += _h_ * 31 + this.sameIpCache.hashCode();
		_h_ += _h_ * 31 + this.status;
		_h_ += _h_ * 31 + this.payType;
		_h_ += _h_ * 31 + this.isContinueInfo;
		_h_ += _h_ * 31 + this.continueRoomId;
		_h_ += _h_ * 31 + this.alreadyQuitPlayers.hashCode();
		_h_ += _h_ * 31 + (int)(this.createTime ^ (this.createTime >>> 32));
		return _h_;
	}

	@Override
	public String toString()
	{
		StringBuilder _sb_ = new StringBuilder(super.toString());
		_sb_.append("=(");
		_sb_.append(this.roomId).append(",");
		_sb_.append(this.roomType).append(",");
		_sb_.append(this.countType).append(",");
		_sb_.append(this.gameplays).append(",");
		_sb_.append(this.totalRoundCount).append(",");
		_sb_.append(this.totalGameCount).append(",");
		_sb_.append(this.overRoundCount).append(",");
		_sb_.append(this.overGameCount).append(",");
		_sb_.append(this.creatorId).append(",");
		_sb_.append(this.players).append(",");
		_sb_.append(this.games).append(",");
		_sb_.append(this.voteInfo).append(",");
		_sb_.append(this.sameIpCache).append(",");
		_sb_.append(this.status).append(",");
		_sb_.append(this.payType).append(",");
		_sb_.append(this.createTime).append(",");
		_sb_.append(this.isContinueInfo).append(",");
		_sb_.append(this.continueRoomId).append(",");
		_sb_.append(this.alreadyQuitPlayers).append(",");
		_sb_.append(")");
		return _sb_.toString();
	}
}