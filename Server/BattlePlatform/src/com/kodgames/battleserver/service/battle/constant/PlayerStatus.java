package com.kodgames.battleserver.service.battle.constant;

public class PlayerStatus
{
	/** 默认状态 */
	public static final int DEFAULT = 1;
	/** 是否准备好打牌 */
	public static final int READY = 1 << 1;
	/** 是房主 */
	public static final int HOST = 1 << 2;
	/** 庄家 */
	public static final int ZHUANGJIA = 1 << 8;
	/** 是否在线 */
	public static final int ONLINE = 1 << 13;
	/** 忽略同IP */
	public static final int IGNORE_SAME_IP = 1 << 15;
}