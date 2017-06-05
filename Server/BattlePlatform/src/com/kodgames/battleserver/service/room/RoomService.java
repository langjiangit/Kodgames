package com.kodgames.battleserver.service.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.battleserver.common.Constant;
import com.kodgames.battleserver.common.Constant.RoomDestroyReason;
import com.kodgames.battleserver.service.battle.BattleService;
import com.kodgames.battleserver.service.battle.common.xbean.BattleRoom;
import com.kodgames.battleserver.service.battle.common.xbean.IBattleBean;
import com.kodgames.battleserver.service.battle.common.xbean.PlayerVoteInfo;
import com.kodgames.battleserver.service.battle.common.xbean.RoomPlayerInfo;
import com.kodgames.battleserver.service.battle.common.xbean.RoomVoteInfo;
import com.kodgames.battleserver.service.battle.common.xbean.SameIpGroup;
import com.kodgames.battleserver.service.battle.common.xbean.SameIpInfo;
import com.kodgames.battleserver.service.battle.constant.ContinueInfo;
import com.kodgames.battleserver.service.battle.constant.PlayerStatus;
import com.kodgames.battleserver.service.global.GlobalService;
import com.kodgames.battleserver.service.server.ServerService;
import com.kodgames.core.task.Task;
import com.kodgames.core.task.TaskService;
import com.kodgames.corgi.core.constant.BattleConstant.RoomConst;
import com.kodgames.corgi.core.constant.DateTimeConstants;
import com.kodgames.corgi.core.constant.GlobalConstants;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.service.PublicService;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.corgi.core.util.IPUtils;
import com.kodgames.message.proto.game.GameProtoBuf.BGDestroyRoomSYN;
import com.kodgames.message.proto.game.GameProtoBuf.BGEnterRoomSYN;
import com.kodgames.message.proto.game.GameProtoBuf.BGInviterRoomInfoRES;
import com.kodgames.message.proto.game.GameProtoBuf.BGRoomCardModifySYNC;
import com.kodgames.message.proto.game.GameProtoBuf.GBInviterRoomInfoREQ;
import com.kodgames.message.proto.game.GameProtoBuf.InviterRoomInfoPROTO;
import com.kodgames.message.proto.game.GameProtoBuf.RoomConfigPROTO;
import com.kodgames.message.proto.record.RecordProtoBuf.BGRecordLogSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCContinueRoomPlayerInfoSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCDestroyRoomSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCIsContinueRoomSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCRoomPlayerInfoSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCSameIpSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCSecureDetectSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCVoteDestroyInfoSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.EMVote;
import com.kodgames.message.proto.room.RoomProtoBuf.RoomPlayerInfoPROTO;
import com.kodgames.message.proto.room.RoomProtoBuf.SameIpPROTO;
import com.kodgames.message.proto.room.RoomProtoBuf.SecurePlayerPROTO;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

public class RoomService extends PublicService
{
	private static final long serialVersionUID = -1482324861168149233L;
	private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

	public static final int POSITION_CREATOR = 1;
	private static final long VOTE_MAX_MILLISECOND = DateTimeConstants.MINUTE;
	private static final long COUNTDOWN_FOR_DESTROY_ROOM = DateTimeConstants.MINUTE * 10;

	private static String regionName = "";
	private static Map<Integer, RoomConfigPROTO> roomConfigs = new ConcurrentHashMap<>();
	private static Map<Integer, BattleRoom> rooms = new ConcurrentHashMap<>();

	public void setRegionName(String name)
	{
		regionName = name;

		// 通知Battle创建creator
		BattleService battleService = ServiceContainer.getInstance().getPublicService(BattleService.class);
		battleService.createBattleCreateor(regionName);
	}

	public String getRegionName()
	{
		return regionName;
	}

	/**
	 * 更新全局房间类型配置
	 */
	public synchronized void updateRoomConfig(List<RoomConfigPROTO> configs)
	{
		roomConfigs.clear();
		configs.forEach(config -> roomConfigs.put(config.getType(), config));
	}

	public RoomConfigPROTO getRoomConfig(int roomType)
	{
		return roomConfigs.get(roomType);
	}

	public BattleRoom getRoomInfo(final Integer roomId)
	{
		return rooms.get(roomId);
	}

	public int createRoom(final int roleId, final int roomId, final int roomType, final List<Integer> gameplays, final int countType, final int payType, final boolean voice)
	{
		// 检测创建参数的合法性
		BattleService battleService = ServiceContainer.getInstance().getPublicService(BattleService.class);
		if (battleService.getBattleCreator().checkRules(gameplays) == false)
			return PlatformProtocolsConfig.BG_CREATE_ROOM_FAILED_INVALID_GAMEPLAYS;

		RoomConfigPROTO rc = getRoomConfig(countType);
		if (null == rc)
		{
			return PlatformProtocolsConfig.BG_CREATE_ROOM_FAILED_INVALID_ROUNDCOUNT;
		}

		// 检测房间是否存在
		BattleRoom room = new BattleRoom();
		if (rooms.putIfAbsent(roomId, room) != null)
		{
			return PlatformProtocolsConfig.BG_CREATE_ROOM_FAILED_ROOM_EXIST;
		}

		// 可以创建房间, 填充房间参数
		room.setRoomId(roomId);
		room.setCreatorId(roleId);
		room.setRoomType(roomType);
		room.setCountType(countType);
		room.setPayType(payType);
		room.setVoice(voice);

		room.setTotalRoundCount(rc.getRoundCount());
		room.setTotalGameCount(rc.getGameCount());
		room.getGameplays().addAll(gameplays);
		room.setCreateTime(System.currentTimeMillis());
		room.setMaxMemberCount(battleService.getBattleCreator().getMaxPlayerSize(gameplays));
		room.setContinueInfo(ContinueInfo.NOCONTINUE);

		// 统计房间数量
		GlobalService service = ServiceContainer.getInstance().getPublicService(GlobalService.class);
		service.addTotalRoomCount();

		logger.info("createRoom : Room {} has been created successfully!", roomId);
		return PlatformProtocolsConfig.BG_CREATE_ROOM_SUCCESS;
	}

	public int continueRoom(final int roleId, final int roomId, final int roomType, final List<Integer> gameplays, final int countType, final int payType)
	{
		// 检测创建参数的合法性
		BattleService battleService = ServiceContainer.getInstance().getPublicService(BattleService.class);
		if (battleService.getBattleCreator().checkRules(gameplays) == false)
			return PlatformProtocolsConfig.BG_CONTINUE_ROOM_FAILED_INVALID_GAMEPLAYS;

		RoomConfigPROTO rc = getRoomConfig(countType);
		if (null == rc)
		{
			return PlatformProtocolsConfig.BG_CONTINUE_ROOM_FAILED_INVALID_ROUNDCOUNT;
		}

		// 检测房间是否存在
		BattleRoom room = new BattleRoom();
		if (rooms.putIfAbsent(roomId, room) != null)
		{
			return PlatformProtocolsConfig.BG_CONTINUE_ROOM_FAILED_ROOM_EXIST;
		}

		// 可以创建房间, 填充房间参数
		room.setRoomId(roomId);
		room.setCreatorId(roleId);
		room.setRoomType(roomType);
		room.setCountType(countType);
		room.setPayType(payType);

		room.setTotalRoundCount(rc.getRoundCount());
		room.setTotalGameCount(rc.getGameCount());
		room.getGameplays().addAll(gameplays);
		room.setCreateTime(System.currentTimeMillis());
		room.setMaxMemberCount(battleService.getBattleCreator().getMaxPlayerSize(gameplays));
		room.setContinueInfo(ContinueInfo.NOCONTINUE);

		// 统计房间数量
		GlobalService service = ServiceContainer.getInstance().getPublicService(GlobalService.class);
		service.addTotalRoomCount();

		logger.info("createRoom : Room {} has been created successfully!", roomId);
		return PlatformProtocolsConfig.BG_CONTINUE_ROOM_SUCCESS;
	}

	public int checkEnterRoom(final int roleId, final int roomId)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (null == room)
		{
			logger.info("enterRoom : Room {} does not exist!", roomId);
			return PlatformProtocolsConfig.BC_ENTER_ROOM_FAILED_ROOM_NOT_EXIST;
		}

		// 判断是否是玩家掉线后重新加入
		for (RoomPlayerInfo play : room.getPlayers())
		{
			if (play.getRoleId() == roleId)
			{
				// 并不清除玩家房间id和座位，方便坐在原来位置
				return PlatformProtocolsConfig.BC_ENTER_ROOM_SUCCESS;
			}
		}

		if (room.getPlayers().size() == room.getMaxMemberCount())
		{
			logger.info("enterRoom : Room {} is full!", roomId);
			return PlatformProtocolsConfig.BC_ENTER_ROOM_FAILED_ROOM_FULL;
		}

		if (room.getPlayers().size() > room.getMaxMemberCount())
		{
			logger.error("enterRoom : Room size -> " + room.getPlayers().size() + "!");
			return PlatformProtocolsConfig.BC_ENTER_ROOM_FAILED_MEMBERS_ERROR;
		}

		return PlatformProtocolsConfig.BC_ENTER_ROOM_SUCCESS;
	}

	public int checkContinueEnterRoom(final int roleId, final int roomId)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (null == room)
		{
			logger.info("enterRoom : Room {} does not exist!", roomId);
			return PlatformProtocolsConfig.BG_CONTINUE_ENTER_ROOM_FAILED_ROOM_NOT_EXIST;
		}

		// 判断是否是玩家掉线后重新加入
		for (RoomPlayerInfo play : room.getPlayers())
		{
			if (play.getRoleId() == roleId)
			{
				// 并不清除玩家房间id和座位，方便坐在原来位置
				return PlatformProtocolsConfig.BG_CONTINUE_ENTER_ROOM_SUCCESS;
			}
		}

		if (room.getPlayers().size() == room.getMaxMemberCount())
		{
			logger.info("enterRoom : Room {} is full!", roomId);
			return PlatformProtocolsConfig.BG_CONTINUE_ENTER_ROOM_FAILED_ROOM_FULL;
		}

		Connection existedConnection = ConnectionManager.getInstance().getClientVirtualConnection(roleId);
		if (existedConnection == null)
		{
			logger.error("enterRoom : Connection {} is null!", existedConnection);
			return PlatformProtocolsConfig.BG_CONTINUE_ENTER_ROOM_CONNECTION_ERROR;
		}

		return PlatformProtocolsConfig.BG_CONTINUE_ENTER_ROOM_SUCCESS;
	}

	private void sitPosition(RoomPlayerInfo info, int position)
	{
		if (position == RoomService.POSITION_CREATOR)
		{
			info.setStatus(info.getStatus() | PlayerStatus.HOST);
		}
		info.setPosition(position);
	}

	public boolean isRoomPositive(BattleRoom room)
	{
		if (null == room)
		{
			logger.error("RoomService isRoomPositive : room is null");
			return false;
		}
		if (room.getPlayers().size() < room.getMaxMemberCount())
		{
			return false;
		}

		for (RoomPlayerInfo info : room.getPlayers())
		{
			if (PlayerStatus.ONLINE == (info.getStatus() & PlayerStatus.ONLINE))
			{
				return true;
			}
		}

		return false;
	}

	public boolean isRoomSilent(BattleRoom room)
	{
		if (null == room)
		{
			logger.error("RoomService isRoomSilent : room is null");
			return false;
		}

		for (RoomPlayerInfo info : room.getPlayers())
		{
			if (PlayerStatus.ONLINE == (info.getStatus() & PlayerStatus.ONLINE))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * 更新房间统计状态（活跃或沉默）
	 */
	private void updateRoomStatStatus(BattleRoom room, int oldStatus)
	{
		if (null == room)
		{
			logger.error("RoomService checkSyncRoomStatusToGame : room is null");
			return;
		}

		GlobalService service = ServiceContainer.getInstance().getPublicService(GlobalService.class);
		int newStatus = getRoomStatStatus(room);

		// 减旧的统计数据
		if (RoomConst.ROOM_STATUS_POSITIVE == oldStatus && RoomConst.ROOM_STATUS_POSITIVE != newStatus)
		{
			service.minusPositiveRoomCount();
		}
		if (RoomConst.ROOM_STATUS_SILENT == oldStatus && RoomConst.ROOM_STATUS_SILENT != newStatus)
		{
			service.minusSilentRoomCount();
		}

		// 加新的统计数据
		if (RoomConst.ROOM_STATUS_POSITIVE != oldStatus && RoomConst.ROOM_STATUS_POSITIVE == newStatus)
		{
			service.addPositiveRoomCount();
		}
		else if (RoomConst.ROOM_STATUS_SILENT != oldStatus && RoomConst.ROOM_STATUS_SILENT == newStatus)
		{
			service.addSilentRoomCount();
		}
	}

	private int getRoomStatStatus(BattleRoom room)
	{
		if (isRoomPositive(room))
		{
			return RoomConst.ROOM_STATUS_POSITIVE;
		}
		else if (isRoomSilent(room))
		{
			return RoomConst.ROOM_STATUS_SILENT;
		}
		else
		{
			return RoomConst.ROOM_STATUS_DEFAULT;
		}
	}

	public BattleRoom enterRoom(int roleId, int roomId, String nickName, String headUrl, int sex, Connection connection)
	{
		// 绑定房间
		connection.setRoomID(roomId);
		logger.debug("enterroom set role {} connection roomID {}", roleId, roomId);

		BattleRoom room = getRoomInfo(roomId);
		if (null == room)
		{
			logger.error("RoomService enterRoom : room is null -> roleId {}, roomId {}", roleId, roomId);
			return null;
		}

		int oldRoomStatStatus = getRoomStatStatus(room);
		for (RoomPlayerInfo info : room.getPlayers())
		{
			if (info.getRoleId() == roleId)
			{
				// 如果是重新加入,依然坐原位置，只是需要更新状态
				info.setStatus(info.getStatus() | PlayerStatus.ONLINE);
				info.setIp(IPUtils.ipToStr(connection.getRemotePeerIP()));
				updateRoomStatStatus(room, oldRoomStatStatus);
				return room;
			}
		}

		RoomPlayerInfo player = new RoomPlayerInfo();
		player.setNickname(nickName);
		player.setHeadImageUrl(headUrl);
		player.setSex(sex);
		player.setRoleId(roleId);
		player.setIp(IPUtils.ipToStr(connection.getRemotePeerIP()));
		player.setStatus(PlayerStatus.DEFAULT | PlayerStatus.ONLINE | PlayerStatus.READY);

		if (roleId == room.getCreatorId())
		{
			// 创建者坐东风位
			sitPosition(player, POSITION_CREATOR);
		}
		else
		{
			// 检查空位,pos按位表示座位，从低位开始 东北西南，不是房主，东位已有人
			int allPos = 0;
			for (RoomPlayerInfo info : room.getPlayers())
			{
				allPos += 1 << (info.getPosition() - 1);
			}

			// 从低到高，检查空闲座位
			int pos = POSITION_CREATOR;
			while (allPos >= 1)
			{
				allPos = allPos >>> 1;
				pos++;
				if ((allPos & 0x1) == 0)
				{
					break;
				}
			}

			sitPosition(player, pos);
		}
		room.getPlayers().add(player);

		updateRoomStatStatus(room, oldRoomStatStatus);

		logger.info("role {} enter room {}", roleId, roomId);

		return room;
	}

	/**
	 * 同步房间的gps信息到客户端
	 * 
	 * @param room
	 */
	public void syncSecureDetectInfoToPlayers(BattleRoom room)
	{
		BCSecureDetectSYN.Builder builder = BCSecureDetectSYN.newBuilder();
		for (RoomPlayerInfo player : room.getPlayers())
		{
			SecurePlayerPROTO.Builder securePlayerInfoBuilder = SecurePlayerPROTO.newBuilder();
			securePlayerInfoBuilder.setIsOpen(player.getGps().getIsOpen());
			securePlayerInfoBuilder.setLatitude(player.getGps().getLatitude());
			securePlayerInfoBuilder.setLongitude(player.getGps().getLongitude());
			securePlayerInfoBuilder.setRoleId(player.getRoleId());
			builder.addPlayers(securePlayerInfoBuilder.build());
		}

		for (RoomPlayerInfo player : room.getPlayers())
		{
			Connection playerConnection = ConnectionManager.getInstance().getClientVirtualConnection(player.getRoleId());
			if (null != playerConnection)
				playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, builder.build());
		}
	}

	/**
	 * 检查房间相同IP信息，并将需要告警的玩家置为UnReady
	 */
	public Map<Integer, BCSameIpSYN> checkSameIpInfo(BattleRoom room)
	{
		if (null == room)
			return null;

		// 当前的相同IP信息
		Map<String, SameIpGroup> current = getCurrentSameIpInfo(room.getPlayers());

		// 缓存的相同IP信息
		SameIpInfo info = room.getSameIpCache();
		Map<String, SameIpGroup> cache = info.getSameGroups();

		// IP各不相同或全部相同，则不需要告警，否则告警
		boolean isCacheNeedAlert = info.getIsNeedAlert();
		boolean isCurrentNeedAlert = isSameIpNeedAlert(current, room.getPlayers().size());

		// 清除旧的忽略状态
		if (!isSameIpSubSet(current, cache) || (!isCacheNeedAlert && isCurrentNeedAlert))
		{
			clearIngoreSameIp(room);
		}

		// 设置新的忽略状态
		if (!isCurrentNeedAlert)
		{
			setIngoreSameIp(room);
		}

		// 提示当前的相同IP信息
		BCSameIpSYN currentSyn =
			BCSameIpSYN.newBuilder().addAllSameGroups(current.values().stream().map(g -> SameIpPROTO.newBuilder().addAllPlayers(g.getPlayers()).build()).collect(Collectors.toList())).build();

		Map<Integer, BCSameIpSYN> sameIpSyncInfo = new HashMap<>();
		for (RoomPlayerInfo player : room.getPlayers())
		{
			// 玩家未忽略相同IP，则弹出提示
			if (0 == (PlayerStatus.IGNORE_SAME_IP & player.getStatus()))
			{
				sameIpSyncInfo.put(player.getRoleId(), currentSyn);
				player.setStatus((~PlayerStatus.READY & player.getStatus()));
			}
		}

		// 更新相同IP信息缓存
		info.setIsNeedAlert(isCurrentNeedAlert);
		cache.clear();
		cache.putAll(current);

		return sameIpSyncInfo;
	}

	/**
	 * 将IP相同的玩家信息同步给客户端
	 */
	public void syncSameIpInfoToPlayers(Map<Integer, BCSameIpSYN> sameIpSyncInfo)
	{
		if (null == sameIpSyncInfo)
			return;

		for (Map.Entry<Integer, BCSameIpSYN> player : sameIpSyncInfo.entrySet())
		{
			Connection playerConnection = ConnectionManager.getInstance().getClientVirtualConnection(player.getKey());
			if (null != playerConnection)
			{
				playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, player.getValue());
			}
		}
	}

	private void setIngoreSameIp(BattleRoom room)
	{
		if (null == room)
		{
			logger.error("RoomService setIngoreSameIp : room is null");
			return;
		}

		for (RoomPlayerInfo player : room.getPlayers())
		{
			player.setStatus((PlayerStatus.IGNORE_SAME_IP | player.getStatus()));
		}
	}

	private void clearIngoreSameIp(BattleRoom room)
	{
		if (null == room)
		{
			logger.error("RoomService clearIngoreSameIp : room is null");
			return;
		}

		for (RoomPlayerInfo player : room.getPlayers())
		{
			player.setStatus((~PlayerStatus.IGNORE_SAME_IP & player.getStatus()));
		}
	}

	private boolean isSameIpNeedAlert(final Map<String, SameIpGroup> info, final int playerCount)
	{
		if (null == info)
		{
			logger.error("RoomService isSameIpNeedAlert : Same IP Info is null");
			return false;
		}

		if (info.isEmpty())
		{
			return false;
		}
		if (1 == info.size())
		{
			for (SameIpGroup group : info.values())
			{
				if (playerCount == group.getPlayers().size())
				{
					return false;
				}
			}
		}

		return true;
	}

	private boolean isSameIpSubSet(final Map<String, SameIpGroup> subset, final Map<String, SameIpGroup> set)
	{
		if (null == subset)
		{
			logger.error("RoomService isSameIpSubSet : subset is null");
			return true;
		}
		if (null == set)
		{
			logger.error("RoomService isSameIpSubSet : set is null");
			return false;
		}

		for (Map.Entry<String, SameIpGroup> entry : subset.entrySet())
		{
			final String ip = entry.getKey();
			if (!set.containsKey(ip))
			{
				return false;
			}

			final Set<Integer> subPlayers = entry.getValue().getPlayers();
			final Set<Integer> players = set.get(ip).getPlayers();
			for (Integer player : subPlayers)
			{
				if (!players.contains(player))
				{
					return false;
				}
			}
		}

		return true;
	}

	private Map<String, SameIpGroup> getCurrentSameIpInfo(List<RoomPlayerInfo> players)
	{
		if (null == players)
		{
			logger.error("RoomService getCurrentSameIpInfo : players is null");
			return null;
		}

		Map<String, SameIpGroup> map = new HashMap<>();
		for (RoomPlayerInfo player : players)
		{
			String ip = player.getIp();
			SameIpGroup group = map.get(ip);
			if (null == group)
			{
				group = new SameIpGroup();
				map.put(ip, group);
			}
			group.getPlayers().add(player.getRoleId());
		}

		return map.entrySet().stream().filter(e -> e.getValue().getPlayers().size() > 1).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	public int checkQuitRoom(final int roleId, final int roomId)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (null == room)
		{
			logger.error("checkQuitRoom : roomId {} is not exist!", roomId);
			return PlatformProtocolsConfig.BC_QUIT_ROOM_FAILED_NOT_IN_ROOM;
		}

		if (isHaveBeginFirstGame(room) && !isAllRoundsAndGamesOver(roomId))
		{
			logger.info("checkQuitRoom found room {} is in battle", roomId);
			return PlatformProtocolsConfig.BC_QUIT_ROOM_FAILED_IN_BATTLE;
		}

		return PlatformProtocolsConfig.BC_QUIT_ROOM_SUCCESS;
	}

	// 已经开始了第一局
	public boolean isHaveBeginFirstGame(BattleRoom room)
	{
		if ((room != null) && (room.getGames().size() > 0))
			return true;
		return false;
	}

	// 玩家正常退出房间
	public void quitRoom(final int roleId, BattleRoom room)
	{
		if (null == room)
		{
			logger.error("quitRoom : roleId {} -> room is null", roleId);
			return;
		}
		for (RoomPlayerInfo playerInfo : room.getPlayers())
		{
			if (playerInfo.getRoleId() == roleId)
			{
				room.getPlayers().remove(playerInfo);
				break;
			}
		}

		logger.info("quitRoom : Role {} quits room {} successfully!", roleId, room.getRoomId());
	}

	// 玩家退出房间，但服务器依然保留玩家信息（为结算断线重连保留）
	public void quitRoomSavePlayer(final RoomPlayerInfo player, BattleRoom room)
	{
		if (room == null)
		{
			return;
		}

		room.getAlreadyQuitPlayers().add(player);

		if (room.getAlreadyQuitPlayers().size() == room.getMaxMemberCount())
			destroyNoBattleRoom(room.getRoomId(), RoomDestroyReason.GAMEOVER);
	}

	public int checkVoteInfo(final int roleId, int roomId)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (0 == roomId || null == room)
		{
			logger.error("voteForDestroyRoom : Role {} is not in any room!", roleId);
			return PlatformProtocolsConfig.BC_VOTE_DESTROY_FAILED_NOT_IN_ROOM;
		}

		RoomVoteInfo roomVoteInfo = room.getVoteInfo();
		if (roomVoteInfo == null)
		{
			return PlatformProtocolsConfig.BC_VOTE_DESTROY_FAILED_WITHOUT_VOTE;
		}
		else if (roomVoteInfo.getPlayerVoteList().stream().filter(value -> value.getRoleId() == roleId).count() > 0)
		{
			logger.error("voteForDestroyRoom : Role {} has voted!", roleId);
			return PlatformProtocolsConfig.BC_VOTE_DESTROY_FAILED_HAS_VOTED;
		}
		// else if (roomVoteInfo.getPlayerVoteList().size() >= roomSize)
		// {
		// logger.error("voteForDestroyRoom : Voting role " + roleId + " -> Voted roles "
		// + roomVoteInfo.getPlayerVoteList() + "!");
		// return PlatformProtocolsConfig.BC_VOTE_DESTROY_FAILED_MEMBERS_ERROR;
		// }

		return PlatformProtocolsConfig.BC_VOTE_DESTROY_SUCCESS;
	}

	public RoomVoteInfo voteForDestroyRoom(final int roleId, final int roomId, final int type)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (null == room)
		{
			logger.error("voteForDestroyRoom : roomId {} -> room is null", roomId);
			return null;
		}

		RoomVoteInfo roomVoteInfo = room.getVoteInfo();

		if (roomVoteInfo.getPlayerVoteList().stream().filter(value -> value.getRoleId() == roleId).count() == 0)
		{
			PlayerVoteInfo playerVoteInfo = new PlayerVoteInfo();
			playerVoteInfo.setRoleId(roleId);
			playerVoteInfo.setVoteType(type);

			roomVoteInfo.getPlayerVoteList().add(playerVoteInfo);
		}
		return roomVoteInfo;
	}

	public boolean isNeedDestroyRoom(BattleRoom room, final RoomVoteInfo roomVote)
	{
		List<PlayerVoteInfo> playerVoteList = roomVote.getPlayerVoteList();

		if (room.getMaxMemberCount() > playerVoteList.size())
		{
			return false;
		}

		boolean destroy = true;
		for (PlayerVoteInfo playerVote : playerVoteList)
		{
			if (EMVote.VOTE_DISAGREE_VALUE == playerVote.getVoteType())
			{
				return false;
			}
		}

		return destroy;
	}

	// (暂时没有用，拆分为下面的两个方法)
	public void destroyRoom(final int roomId, final int reason)
	{
		logger.info("destroyroom {}, reason:{}", roomId, reason);
		BattleRoom room = getRoomInfo(roomId);
		if (null == room)
		{
			return;
		}

		try
		{
			BattleService battleService = ServiceContainer.getInstance().getPublicService(BattleService.class);
			battleService.removeBattle(roomId);
			// 结算
			if (room.getOverGameCount() > 0)
			{
				battleService.handleMatchResult(roomId, reason);
			}
		}
		finally
		{
			// 销毁房间
			rooms.remove(roomId);
			syncDestroyRoomToGame(room, reason);
			syncDestroyRoomToPlayers(room, reason);

			// 更新房间统计信息
			GlobalService service = ServiceContainer.getInstance().getPublicService(GlobalService.class);
			if (isRoomPositive(room))
			{
				service.minusPositiveRoomCount();
			}
			else if (isRoomSilent(room))
			{
				service.minusSilentRoomCount();
			}
			service.minusTotalRoomCount();
		}
	}

	// 发送房间总结算但不销毁房间
	public void endBattleNotDestroyRoom(final int roomId, final int reason)
	{
		logger.info("endBattleRoomId {}", roomId);
		BattleRoom room = getRoomInfo(roomId);
		if (null == room)
		{
			return;
		}

		BattleService battleService = ServiceContainer.getInstance().getPublicService(BattleService.class);

		// 结算
		if (room.getOverGameCount() > 0)
		{
			battleService.handleMatchResult(roomId, reason);
		}
		if (Constant.InValidReason(reason))
			destroyNoBattleRoom(roomId, reason);
		else
			countDownForDestroyRoom(roomId);
	}

	// 销毁已经结束战斗的房间
	public void destroyNoBattleRoom(final int roomId, final int reason)
	{
		BattleRoom room = getRoomInfo(roomId);

		// 销毁房间
		BattleService battleService = ServiceContainer.getInstance().getPublicService(BattleService.class);
		battleService.removeBattle(roomId);
		rooms.remove(roomId);
		syncDestroyRoomToGame(room, reason);
		syncDestroyRoomToPlayers(room, reason);

		// 更新房间统计信息
		GlobalService service = ServiceContainer.getInstance().getPublicService(GlobalService.class);
		if (isRoomPositive(room))
		{
			service.minusPositiveRoomCount();
		}
		else if (isRoomSilent(room))
		{
			service.minusSilentRoomCount();
		}
		service.minusTotalRoomCount();

	}

	public RoomVoteInfo startVote(int roleId, int roomId)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (null == room)
		{
			logger.error("RoomService startVote : room is null : ", roomId);
			return null;
		}

		RoomVoteInfo roomVote = room.getVoteInfo();
		roomVote.setApplicant(roleId);
		long voteTime = System.currentTimeMillis();
		roomVote.setStartTime(voteTime);
		List<PlayerVoteInfo> playerVotes = roomVote.getPlayerVoteList();
		playerVotes.clear();
		PlayerVoteInfo startVote = new PlayerVoteInfo();
		startVote.setRoleId(roleId);
		startVote.setVoteType(EMVote.VOTE_AGREE_VALUE);
		playerVotes.add(startVote);

		// 投票超时则销毁房间
		// TODO: room 和 roomvote都是xbean对象不能保存taskhandler， 所以没有办法cancel task
		TaskService.getInstance().registerOnceTimeTask(new Task()
		{
			@Override
			public void run(long currentTime, int state)
			{
				// logger.info("voting for destory room:{} has timed out", roomId);
				// 必须自己重新从table中获取数据
				BattleRoom localRoom = getRoomInfo(roomId);
				if (null == localRoom)
				{
					// logger.error("RoomService startVote taskHandler : room is null : ", roomId);
					return;
				}

				// 这个任务可能被提前终止了
				RoomVoteInfo taskVote = localRoom.getVoteInfo();
				if (null == taskVote || taskVote.getStartTime() != voteTime || taskVote.getApplicant() != roleId)
				{
					return;
				}

				endBattleNotDestroyRoom(roomId, RoomDestroyReason.VOTE);
			}
		}, System.currentTimeMillis() + VOTE_MAX_MILLISECOND);

		return roomVote;
	}

	public void countDownForDestroyRoom(int roomId)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (room == null)
		{
			logger.error("RoomService timeToDestroyRoom : room is null : ", roomId);
		}

		TaskService.getInstance().registerOnceTimeTask(new Task()
		{
			@Override
			public void run(long currentTime, int state)
			{
				BattleRoom localRoom = getRoomInfo(roomId);
				if (localRoom == null)
					return;

				destroyNoBattleRoom(roomId, RoomDestroyReason.GAMEOVER);
			}
		}, System.currentTimeMillis() + COUNTDOWN_FOR_DESTROY_ROOM);
	}

	public void cancelVoteTask(BattleRoom room)
	{
		if (null == room)
		{
			return;
		}

		RoomVoteInfo roomVoteInfo = room.getVoteInfo();

		// 结束投票
		// if (taskHandler != null)
		// {
		// taskHandler.cancel();
		// taskHandler = null;
		// }
		// 取消room的投票信息
		roomVoteInfo.setApplicant(0);
		roomVoteInfo.getPlayerVoteList().clear();
	}

	public void syncDestroyRoomToGame(BattleRoom room, final int reason)
	{
		BGDestroyRoomSYN.Builder bgSynbuilder = BGDestroyRoomSYN.newBuilder();
		bgSynbuilder.setRoomId(room.getRoomId());
		bgSynbuilder.setReason(reason);
		for (RoomPlayerInfo player : room.getPlayers())
		{
			bgSynbuilder.addRoleList(player.getRoleId());
		}

		ServerService serverService = ServiceContainer.getInstance().getPublicService(ServerService.class);
		Connection gameConnection = serverService.getGameConnection();
		if (null != gameConnection)
			serverService.getGameConnection().write(GlobalConstants.DEFAULT_CALLBACK, bgSynbuilder.build());
	}

	public void syncDestroyRoomToPlayers(final BattleRoom room, final int reason)
	{
		BCDestroyRoomSYN.Builder bcSynBuilder = BCDestroyRoomSYN.newBuilder().setReason(reason);
		BCDestroyRoomSYN bcSyn = bcSynBuilder.build();
		for (RoomPlayerInfo player : room.getPlayers())
		{

			if ((player.getStatus() & PlayerStatus.ONLINE) != PlayerStatus.ONLINE)
			{
				continue;
			}
			Connection playerConnection = ConnectionManager.getInstance().getClientVirtualConnection(player.getRoleId()); // RoleManager.getInstance().getRoleConnection(player.getRoleId());

			if (null != playerConnection)
			{
				playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, bcSyn);
				playerConnection.setRoomID(0);
			}
			else
			{
				logger.debug("syncDestroyRoom to player {} but connection null", player.getRoleId());
			}
		}
	}

	// roleID = 0 means all the players in the room, roleID > 0 means specified player
	public void syncVoteDestroyInfoToPlayers(RoomVoteInfo roomVote, BattleRoom room, int roleID)
	{
		BCVoteDestroyInfoSYN.Builder voteSynBuilder = BCVoteDestroyInfoSYN.newBuilder();
		voteSynBuilder.setApplicant(roomVote.getApplicant());
		voteSynBuilder.setRemainTime((int)(VOTE_MAX_MILLISECOND - (System.currentTimeMillis() - roomVote.getStartTime())));
		for (PlayerVoteInfo vote : roomVote.getPlayerVoteList())
		{
			if (EMVote.VOTE_AGREE_VALUE == vote.getVoteType())
			{
				voteSynBuilder.addAgreePlayers(vote.getRoleId());
			}
			else
			{
				voteSynBuilder.addDisagreePlayers(vote.getRoleId());
			}
		}

		if (roomVote.getPlayerVoteList().isEmpty())
		{
			logger.error("syncVoteDestory to clients but voteList is empty roomId {} starter {}", room.getRoomId(), roomVote.getApplicant());
		}

		BCVoteDestroyInfoSYN voteSyn = voteSynBuilder.build();
		if (roleID > 0)
		{
			Connection playerConnection = ConnectionManager.getInstance().getClientVirtualConnection(roleID); // RoleManager.getInstance().getRoleConnection(player.getRoleId());
			if (null != playerConnection)
			{
				playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, voteSyn);
			}
		}
		else
		{
			for (RoomPlayerInfo player : room.getPlayers())
			{

				if ((player.getStatus() & PlayerStatus.ONLINE) != PlayerStatus.ONLINE)
				{
					continue;
				}
				Connection playerConnection = ConnectionManager.getInstance().getClientVirtualConnection(player.getRoleId()); // RoleManager.getInstance().getRoleConnection(player.getRoleId());
				if (null != playerConnection)
				{
					playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, voteSyn);
				}
			}
		}
	}

	public void syncVoteDestroyInfoToPlayers(int roomId, int roleID)
	{
		BattleRoom room = getRoomInfo(roomId);
		this.syncVoteDestroyInfoToPlayers(room, roleID);
	}

	public void syncVoteDestroyInfoToPlayers(BattleRoom room, int roleID)
	{
		RoomVoteInfo roomVote = room.getVoteInfo();
		if (null == roomVote)
		{
			logger.error("room {} is votingDestroy but no voteInfo", room.getRoomId());
			return;
		}
		this.syncVoteDestroyInfoToPlayers(roomVote, room, roleID);
	}

	public boolean isRoomExist(int roomId)
	{
		return getRoomInfo(roomId) != null;
	}

	/**
	 * 一局牌结束时调用
	 */
	public void addOverGameCount(int roomId, int zhuang, int nextZhuang)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (null == room)
		{
			logger.error("RoomService addOverGameCount : room is null", roomId);
			return;
		}
		room.setOverGameCount(room.getOverGameCount() + 1);

		// 不管几圈，都是第一局打完就扣，2圈就扣2张
		if (room.getOverGameCount() == 1)
		{
			// 通知Game扣卡
			BGRoomCardModifySYNC.Builder builder = BGRoomCardModifySYNC.newBuilder();
			builder.setRoomId(roomId);
			builder.setCreatorId(room.getCreatorId());
			for (int i = 0; i < room.getPlayers().size(); i++)
			{
				builder.addRoleIds(room.getPlayers().get(i).getRoleId());
			}

			RoomConfigPROTO rc = getRoomConfig(room.getCountType());
			if (null != rc)
			{
				builder.setRoundCount(rc.getType());

				Connection gameConnection = ServiceContainer.getInstance().getPublicService(ServerService.class).getGameConnection();
				if (null != gameConnection)
					gameConnection.write(GlobalConstants.DEFAULT_CALLBACK, builder.build());
			}
			else
			{
				logger.error("Invalid round count key : room -> " + roomId + ", totalRoundCount -> " + room.getTotalRoundCount() + ", totalGameCount -> " + room.getTotalGameCount());
			}
		}

		if (zhuang != nextZhuang && nextZhuang == room.getCreatorId())
			room.setOverRoundCount(room.getOverRoundCount() + 1);

		// 更新玩家状态
		resetPlayerStatus(room, PlayerStatus.READY | PlayerStatus.ZHUANGJIA);

		if (isAllRoundsAndGamesOver(roomId))
		{
			roundFinishRecord(roomId, true);
			endBattleNotDestroyRoom(roomId, RoomDestroyReason.GAMEOVER);
		}
		else
		{
			roundFinishRecord(roomId, false);
		}
	}

	private void resetPlayerStatus(BattleRoom room, int status)
	{
		status = (~status);
		for (RoomPlayerInfo playerInfo : room.getPlayers())
		{
			System.out.println();
			playerInfo.setStatus(playerInfo.getStatus() & status);
		}
	}

	public boolean isAllRoundsAndGamesOver(int roomId)
	{
		BattleRoom room = getRoomInfo(roomId);
		return isAllRoundOver(room) && isAllGameOver(room);
	}

	private boolean isAllRoundOver(BattleRoom room)
	{
		if (null == room)
		{
			logger.error("RoomService isAllRoundOver : room is null");
			return true;
		}

		return room.getOverRoundCount() >= room.getTotalRoundCount();
	}

	private boolean isAllGameOver(BattleRoom room)
	{
		if (null == room)
		{
			logger.error("RoomService isAllGameOver : room is null");
			return true;
		}

		return room.getOverGameCount() >= room.getTotalGameCount();
	}

	public boolean isRoomPlayingCard(BattleRoom room)
	{
		return room.getStatus() == RoomConst.ROOM_STATUS_RUNNING;
	}

	public boolean isRoomVotingDestroy(BattleRoom room)
	{
		RoomVoteInfo vote = room.getVoteInfo();
		return vote != null && vote.getApplicant() != 0;
	}

	/** 玩家是否准备 */
	private boolean isAllPlayersReady(BattleRoom room)
	{
		List<RoomPlayerInfo> list = room.getPlayers();
		if (list.size() < room.getMaxMemberCount())
			return false;

		for (RoomPlayerInfo player : list)
		{
			int status = player.getStatus();
			if (0 == (status & PlayerStatus.READY))
				return false;
		}

		return true;
	}

	public boolean isRoomReadyForBattle(BattleRoom room)
	{
		BattleService battleService = ServiceContainer.getInstance().getPublicService(BattleService.class);
		if (room.getPlayers().size() < room.getMaxMemberCount() || battleService.isBattleRunning(room.getRoomId()))
		{
			return false;
		}

		return isAllPlayersReady(room);
	}

	public void roleDisconnect(int roleId, int roomId)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (room == null)
		{
			return;
		}

		int oldRoomStatStatus = getRoomStatStatus(room);
		for (RoomPlayerInfo info : room.getPlayers())
		{
			if (info.getRoleId() == roleId)
			{
				int status = info.getStatus();
				status = status & (~PlayerStatus.ONLINE);
				info.setStatus(status);
				updateRoomStatStatus(room, oldRoomStatStatus);
				syncRoomPlayerInfoToMembers(room);
				break;
			}
		}
	}

	// update之前需要确认玩家已经在房间内
	public int updatePlayerStatus(final int roleId, final int roomId, final int status)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (room == null)
		{
			logger.error("updatePlayerStatusInRoom : room {} is not exist!", roomId);
			return PlatformProtocolsConfig.BC_UPDATE_PLAYERSTATUS_FAILED_NOT_IN_ROOM;
		}

		for (RoomPlayerInfo player : room.getPlayers())
		{
			if (player.getRoleId() == roleId)
			{
				int newStatus = player.getStatus() | status;
				if (0 != (PlayerStatus.ONLINE & newStatus) && 0 != (PlayerStatus.IGNORE_SAME_IP & newStatus))
				{
					newStatus |= PlayerStatus.READY;
				}

				player.setStatus(newStatus);
				logger.info("updatePlayerStatus : Role {} update status {} input {} successfully!", roleId, Integer.toHexString(newStatus), Integer.toHexString(status));
				return PlatformProtocolsConfig.BC_UPDATE_PLAYERSTATUS_SUCCESS;
			}
		}

		logger.error("updatePlayerStatusInRoom : Role {} is not in room {}!", roleId, roomId);
		return PlatformProtocolsConfig.BC_UPDATE_PLAYERSTATUS_FAILED_NOT_IN_ROOM;
	}

	// 三人或二人玩法中，改变除自身外其他玩家的映射
	public void changeMapping(RoomPlayerInfo player, BCRoomPlayerInfoSYN.Builder synBuilder, Connection playerConnection, BattleRoom room)
	{
		int searchIndex = -1; // 要更改位置映射的玩家索引
		int replacePos = -1; // 要将玩家更改到的位置索引

		BattleService battleService = ServiceContainer.getInstance().getPublicService(BattleService.class);
		PosMapInfo posMapInfo = battleService.getBattleCreator().getPlayerPosition(room.getMaxMemberCount(), player.getPosition(), room.getGameplays());
		if (posMapInfo != null)
		{
			searchIndex = posMapInfo.getSearchIndex();
			replacePos = posMapInfo.getReplacePos();
		}

		// 将更改位置后的玩家映射到客户端
		if (searchIndex != -1 && room.getPlayers().size() > searchIndex)
		{
			BCRoomPlayerInfoSYN.Builder sendBuilder = synBuilder.clone();

			for (int i = 0; i < sendBuilder.getPlayerInfoBuilderList().size(); i++)
			{
				if (sendBuilder.getPlayerInfo(i).getRoleId() != room.getPlayers().get(searchIndex).getRoleId())
					continue;

				RoomPlayerInfoPROTO.Builder changePlayerBuilder = RoomPlayerInfoPROTO.newBuilder(sendBuilder.getPlayerInfo(i));
				changePlayerBuilder.setPosition(replacePos);

				sendBuilder.setPlayerInfo(i, changePlayerBuilder.build());
				playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, sendBuilder.build());
				break;
			}
		}
		else
			playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, synBuilder.build());
	}

	public void syncRoomPlayerInfoToMembers(BattleRoom room)
	{
		BCRoomPlayerInfoSYN.Builder synBuilder = BCRoomPlayerInfoSYN.newBuilder();
		for (RoomPlayerInfo player : room.getPlayers())
		{
			synBuilder.addPlayerInfo(RoomData.playerInfoBeanToRoomProto(player));
		}
		for (RoomPlayerInfo player : room.getAlreadyQuitPlayers())
		{
			synBuilder.addPlayerInfo(RoomData.playerInfoBeanToRoomProto(player));
		}
		synBuilder.setNowRoundCount(room.getOverGameCount() + 1);
		synBuilder.setTotalRoundCount(room.getTotalGameCount());

		for (RoomPlayerInfo player : room.getPlayers())
		{
			if ((player.getStatus() & PlayerStatus.ONLINE) == PlayerStatus.ONLINE)
			{
				Connection playerConnection = ConnectionManager.getInstance().getClientVirtualConnection(player.getRoleId()); // RoleManager.getInstance().getRoleConnection(player.getRoleId());
				if (null != playerConnection)
				{
					if (room.getMaxMemberCount() != RoomConst.MAX_ROOM_MEMBER_COUNT)
					{
						changeMapping(player, synBuilder, playerConnection, room);
					}
					else
						playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, synBuilder.build());
				}
			}
		}
	}

	public void isContinueRoom(int oldRoomId, int newRoomId)
	{
		BattleRoom oldRoomInfo = getRoomInfo(oldRoomId);

		BCIsContinueRoomSYN.Builder synBuilder = BCIsContinueRoomSYN.newBuilder();
		synBuilder.setNewRoomId(newRoomId);

		for (RoomPlayerInfo player : oldRoomInfo.getPlayers())
		{
			if ((player.getStatus() & PlayerStatus.ONLINE) == PlayerStatus.ONLINE)
			{
				Connection playerConnection = ConnectionManager.getInstance().getClientVirtualConnection(player.getRoleId());
				if (null != playerConnection)
				{
					playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, synBuilder.build());
				}
			}
		}
	}

	public void finalRejoin(BattleRoom room, int roleId)
	{
		if (!isAllRoundsAndGamesOver(room.getRoomId()))
			return;
		else
		{
			if (room.getContinueInfo() != ContinueInfo.CONTINUE)
				return;

			BattleRoom oldRoomInfo = getRoomInfo(room.getRoomId());

			BCIsContinueRoomSYN.Builder synBuilder = BCIsContinueRoomSYN.newBuilder();
			synBuilder.setNewRoomId(room.getContinueRoomId());

			for (RoomPlayerInfo player : oldRoomInfo.getPlayers())
			{
				if (player.getRoleId() != roleId)
					continue;
				if ((player.getStatus() & PlayerStatus.ONLINE) == PlayerStatus.ONLINE)
				{
					Connection playerConnection = ConnectionManager.getInstance().getClientVirtualConnection(player.getRoleId());
					if (null != playerConnection)
					{
						playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, synBuilder.build());
					}
				}
			}
		}
	}

	public void syncPlayerContinueEnterRoom(BattleRoom oldRoom, BattleRoom newRoom)
	{
		int playerId = 0;
		BCContinueRoomPlayerInfoSYN.Builder synBuilder = BCContinueRoomPlayerInfoSYN.newBuilder();
		for (RoomPlayerInfo player : newRoom.getPlayers())
		{
			synBuilder.clearPlayerInfo();
			synBuilder.addPlayerInfo(RoomData.playerInfoBeanToRoomProto(player));
			playerId = player.getRoleId();
		}

		for (RoomPlayerInfo player : oldRoom.getPlayers())
		{
			if (player.getRoleId() == playerId)
				continue;
			if ((player.getStatus() & PlayerStatus.ONLINE) == PlayerStatus.ONLINE)
			{
				Connection playerConnection = ConnectionManager.getInstance().getClientVirtualConnection(player.getRoleId());
				if (null != playerConnection)
				{
					playerConnection.write(GlobalConstants.DEFAULT_CALLBACK, synBuilder.build());
				}
			}
		}
	}

	public boolean isPlayerInRoom(int roleId, BattleRoom room)
	{
		for (RoomPlayerInfo playerInfo : room.getPlayers())
		{
			if (roleId == playerInfo.getRoleId())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * 获取新的玩家打牌协议序列号
	 */
	public int getAndSetNewPlayProtocolSequence(int roomId, int roleId)
	{
		RoomPlayerInfo player = getRoomPlayerInfo(roomId, roleId);
		if (null == player)
		{
			return 0;
		}

		int newSequence = player.getPlayProtoSeq() + 1;
		player.setPlayProtoSeq(newSequence);
		return newSequence;
	}

	public RoomPlayerInfo getRoomPlayerInfo(int roomId, int roleId)
	{
		BattleRoom room = getRoomInfo(roomId);
		if (null == room)
		{
			return null;
		}

		for (RoomPlayerInfo pi : room.getPlayers())
		{
			if (roleId == pi.getRoleId())
			{
				return pi;
			}
		}

		return null;
	}

	/**
	 * 请求的处理方法
	 * 
	 * @param connection 客户端连接
	 * @param message 请求消息
	 * @param callback 回调值
	 */
	public void onGBInviterRoomInfoREQ(Connection connection, GBInviterRoomInfoREQ message, int callback)
	{
		BattleRoom battleRoom = this.getRoomInfo(message.getRoomdId());
		if (battleRoom == null)
		{
			logger.info("onCBInviterRoom: room is null, roomId={}", message.getRoomdId());
			BGInviterRoomInfoRES.Builder resBuilder = BGInviterRoomInfoRES.newBuilder();
			resBuilder.setResult(PlatformProtocolsConfig.BG_INVITER_ROOMINFO_FAILED_ROOM_NOT_EXIST);

			// 因为客户端想用roomid来判断请求是佛成功，所以失败的时候还要设置roomid
			resBuilder.setInfo(InviterRoomInfoPROTO.newBuilder().setRoomId(0).setCreatorId(-1).addGamePlays(0).setHeadImageUrl("").setNickname("").setRoundType(-1).setPayType(0));
			resBuilder.setRoleId(message.getRoleId());
			connection.write(callback, resBuilder.build());
			return;
		}

		RoomPlayerInfo creatorInfo = this.getRoomPlayerInfo(message.getRoomdId(), battleRoom.getCreatorId());
		if (creatorInfo == null)
		{
			logger.error("onCBInviterRoomInfoREQ: creatorInfo is null, roomId={}", message.getRoomdId());
			BGInviterRoomInfoRES.Builder resBuilder = BGInviterRoomInfoRES.newBuilder();
			resBuilder.setResult(PlatformProtocolsConfig.BG_INVITER_ROOMINFO_FAILED_INVITER_NOT_EXIST);
			resBuilder.setInfo(InviterRoomInfoPROTO.newBuilder().setRoomId(0));
			resBuilder.setRoleId(message.getRoleId());
			connection.write(callback, resBuilder.build());
			return;
		}

		BGInviterRoomInfoRES.Builder resBuilder = BGInviterRoomInfoRES.newBuilder();
		InviterRoomInfoPROTO.Builder protoBuilder = InviterRoomInfoPROTO.newBuilder();
		protoBuilder.setCreatorId(battleRoom.getCreatorId());
		protoBuilder.addAllGamePlays(battleRoom.getGameplays());
		protoBuilder.setHeadImageUrl(creatorInfo.getHeadImageUrl());
		protoBuilder.setNickname(creatorInfo.getNickname());
		protoBuilder.setRoomId(message.getRoomdId());
		protoBuilder.setRoundType(battleRoom.getCountType());
		protoBuilder.setPayType(battleRoom.getPayType());

		resBuilder.setResult(PlatformProtocolsConfig.GC_INVITER_ROOMINFO_SUCCEE);
		resBuilder.setInfo(protoBuilder);
		resBuilder.setRoleId(message.getRoleId());

		connection.write(callback, resBuilder.build());
	}

	/**
	 * 一局结束后调用
	 * 
	 * @param roomId 房间id
	 * @param isFinish true:所有牌局都结束了
	 */
	private void roundFinishRecord(int roomId, boolean isFinish)
	{
		BattleRoom battleRoom = this.getRoomInfo(roomId);
		List<IBattleBean> battleBeans = battleRoom.getGames();
		IBattleBean battleBean = battleBeans.get(battleBeans.size() - 1);

		long roomStartTime = battleRoom.getCreateTime();
		List<Integer> playerIDs = new ArrayList<Integer>();
		battleRoom.getPlayers().forEach(player -> {
			playerIDs.add(player.getRoleId());
		});
		int ownerId = battleRoom.getCreatorId();
		int roomType = battleRoom.getCountType();
		long roundStartTime = battleBean.getStartTime();
		int currRoundCount = battleRoom.getOverGameCount();

		BGRecordLogSYN.Builder syn = BGRecordLogSYN.newBuilder();
		syn.setCurrRoundCount(currRoundCount);
		syn.setIsFinished(isFinish);
		syn.setOwnerID(ownerId);
		syn.addAllPlayerIDs(playerIDs);
		syn.setRoomID(roomId);
		syn.setRoomStartTime(roomStartTime);
		syn.setRoomType(roomType);
		syn.setRoundRecordStartTime(roundStartTime);

		Connection connection = ServiceContainer.getInstance().getPublicService(ServerService.class).getGameConnection();
		connection.write(GlobalConstants.DEFAULT_CALLBACK, syn.build());
	}

	public void synBattleToGame(int roleId, int roomId)
	{
		// 将玩家信息同步到Game
		BGEnterRoomSYN.Builder builder = BGEnterRoomSYN.newBuilder();
		builder.setRoleId(roleId);
		builder.setRoomId(roomId);
		ServerService serverService = ServiceContainer.getInstance().getPublicService(ServerService.class);
		Connection gameConnection = serverService.getGameConnection();
		if (null != gameConnection)
			gameConnection.write(GlobalConstants.DEFAULT_CALLBACK, builder.build());
	}

}
