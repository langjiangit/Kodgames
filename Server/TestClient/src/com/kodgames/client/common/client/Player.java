package com.kodgames.client.common.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.kodgames.battleserver.service.battle.constant.MahjongConstant;
import com.kodgames.client.StressTool2;
import com.kodgames.client.common.club.Club;
import com.kodgames.client.common.club.Clubs;
import com.kodgames.client.constant.ClubConstants;
import com.kodgames.client.net.http.GmtHttpClient;
import com.kodgames.client.service.account.RoleService;
import com.kodgames.client.service.room.RoomService;
import com.kodgames.client.start.NetInitializer;
import com.kodgames.club.utils.InviteCodeEncoder;
import com.kodgames.core.net.http.HttpClient;
import com.kodgames.core.net.http.HttpUri;
import com.kodgames.core.net.http.ProtocolType;
import com.kodgames.core.util.converter.Converter;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.message.proto.auth.AuthProtoBuf.CIAccountAuthREQ;
import com.kodgames.message.proto.auth.AuthProtoBuf.ICAccountAuthRES;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCBattlePlayerInfoSYN;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCFinalMatchResultSYN;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCMatchResultSYN;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCPlayCardRES;
import com.kodgames.message.proto.battle.BattleProtoBuf.BCPlayStepSYN;
import com.kodgames.message.proto.battle.BattleProtoBuf.BattlePlayerInfoPROTO;
import com.kodgames.message.proto.battle.BattleProtoBuf.CBPlayCardREQ;
import com.kodgames.message.proto.battle.BattleProtoBuf.PlayStepPROTO;
import com.kodgames.message.proto.chat.ChatProtoBuf.BCChatRES;
import com.kodgames.message.proto.chat.ChatProtoBuf.BCChatSYN;
import com.kodgames.message.proto.chat.ChatProtoBuf.CBChatREQ;
import com.kodgames.message.proto.club.ClubProtoBuf;
import com.kodgames.message.proto.club.ClubProtoBuf.CCLCreateRoomREQ;
import com.kodgames.message.proto.club.ClubProtoBuf.CCLEnterRoomREQ;
import com.kodgames.message.proto.club.ClubProtoBuf.CLCCreateRoomRES;
import com.kodgames.message.proto.game.GameProtoBuf.CGCreateRoomREQ;
import com.kodgames.message.proto.game.GameProtoBuf.CGHistoryREQ;
import com.kodgames.message.proto.game.GameProtoBuf.CGLoginREQ;
import com.kodgames.message.proto.game.GameProtoBuf.CGLogoutREQ;
import com.kodgames.message.proto.game.GameProtoBuf.CGQueryBattleIdREQ;
import com.kodgames.message.proto.game.GameProtoBuf.GCCreateRoomRES;
import com.kodgames.message.proto.game.GameProtoBuf.GCHistoryRES;
import com.kodgames.message.proto.game.GameProtoBuf.GCLoginRES;
import com.kodgames.message.proto.game.GameProtoBuf.GCLogoutRES;
import com.kodgames.message.proto.game.GameProtoBuf.GCQueryBattleIdRES;
import com.kodgames.message.proto.mail.MailProtoBuf.CGMailREQ;
import com.kodgames.message.proto.mail.MailProtoBuf.GCMailRES;
import com.kodgames.message.proto.marquee.MarqueeProtoBuf.CGMarqueeREQ;
import com.kodgames.message.proto.marquee.MarqueeProtoBuf.GCMarqueeRES;
import com.kodgames.message.proto.room.RoomProtoBuf.BCDestroyRoomSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCEnterRoomRES;
import com.kodgames.message.proto.room.RoomProtoBuf.BCQuitRoomRES;
import com.kodgames.message.proto.room.RoomProtoBuf.BCRoomPlayerInfoSYN;
import com.kodgames.message.proto.room.RoomProtoBuf.BCUpdateStatusRES;
import com.kodgames.message.proto.room.RoomProtoBuf.CBEnterRoomREQ;
import com.kodgames.message.proto.room.RoomProtoBuf.CBQuitRoomREQ;
import com.kodgames.message.proto.room.RoomProtoBuf.CBUpdateStatusREQ;
import com.kodgames.message.proto.room.RoomProtoBuf.CBVoteDestroyREQ;
import com.kodgames.message.protocol.PlatformProtocolsConfig;
import com.kodgames.message.protocol.ProtocolsConfig;

//import com.kodgames.message.proto.auth.AuthProtoBuf.CIConnectAuthREQ;
//import com.kodgames.message.proto.auth.AuthProtoBuf.ICConnectAuthRES;

public class Player
{
	private static Logger logger = LoggerFactory.getLogger(Player.class);

	private Connection interfaceConnection = null;
	private Connection gameConnection = null;
	private Connection clubConnection = null;
	private Connection battleConnection = null;

	// jiangzhen,2016.9.7
	private int roomId = -1;

	private int localChairId = -1;
	private int localRoomId = -1;
	private int accountId = -1;
	private int roleId = -1;
	private String username = "";

	private int clubId = 0;

	/*
	 * 玩家状态
	 */
	private int status = MahjongConstant.PlayerStatus.DEFAULT;

	/*
	 * 玩家手牌
	 */
	private HandCard handCards = null;

	/*
	 * 该玩家在线程中的下标
	 */
	private int threadIndex = -1;

	public Player()
	{

	}

	public Player(int threadIndex)
	{
		this.threadIndex = threadIndex;

		this.interfaceConnection = null;
		this.gameConnection = null;
		this.clubConnection = null;
		this.battleConnection = null;

		handCards = new HandCard();
	}

	public boolean isManager()
	{
		return this.threadIndex % 50 == 19; // 每50个人中有一个是经理！
	}

	public boolean isClubRoomCreator()
	{
		return this.threadIndex % 4 == 1; // 每4个人中有一个是房间建立者，其他为进入者
	}

	public int getLocalPlayerId()
	{
		return (this.localRoomId << 16) + this.localChairId;
	}

	public void setRoomId(int roomId)
	{
		this.roomId = roomId;
	}

	public int getRoomId()
	{
		return roomId;
	}

	public void setRoleId(int roleId)
	{
		this.roleId = roleId;
	}

	public int getRoleId()
	{
		return this.roleId;
	}

	public void setAccountId(int accountId)
	{
		this.accountId = accountId;
	}

	public int getAccountId()
	{
		return this.accountId;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getUsername()
	{
		return this.username;
	}

	private void initGameConnection(int gameId)
	{
		if (this.gameConnection == null)
		{
			this.gameConnection = new Connection(this.interfaceConnection.getConnectionID(), this.interfaceConnection.getNettyNode(), this.interfaceConnection.getRemotePeerIP());
			this.gameConnection.setConnectionType(Connection.CONNECTION_TYPE_INTERFACE_TRANSFER_CLIENT);
			this.gameConnection.setRemotePeerID(this.roleId);
			this.gameConnection.setRemoteConnectionID(gameId);
			this.gameConnection.setTransferConnectoin(this.interfaceConnection);
		}
	}

	private void initClubConnection(int clubId)
	{
		if (this.clubConnection == null)
		{
			this.clubConnection = new Connection(this.interfaceConnection.getConnectionID(), this.interfaceConnection.getNettyNode(), this.interfaceConnection.getRemotePeerIP());
			this.clubConnection.setConnectionType(Connection.CONNECTION_TYPE_INTERFACE_TRANSFER_CLIENT);
			this.clubConnection.setRemotePeerID(this.roleId);
			this.clubConnection.setRemoteConnectionID(clubId);
			this.clubConnection.setTransferConnectoin(this.interfaceConnection);
		}
	}

	private void initBattleConnection(int battleId)
	{
		if (this.battleConnection == null)
		{
			this.battleConnection = new Connection(this.interfaceConnection.getConnectionID(), this.interfaceConnection.getNettyNode(), this.interfaceConnection.getRemotePeerIP());
			this.battleConnection.setConnectionType(Connection.CONNECTION_TYPE_INTERFACE_TRANSFER_CLIENT);
			this.battleConnection.setRemotePeerID(this.roleId);
			this.battleConnection.setRemoteConnectionID(battleId);
			this.battleConnection.setTransferConnectoin(this.interfaceConnection);
		}
	}

	/*
	 * jiangzhen 2016.9.9 玩家连接网关
	 */
	// public void connec2()
	// throws Exception
	// {
	// // 获取网关地址
	// String gwIp = ClientConfig.getInstance().getGwIp();
	// int gwPort = ClientConfig.getInstance().getGwPort();
	//
	// // 连接网关后获取auth server的地址
	// String authInfo = this.getAuthServerInfo(gwIp, gwPort);
	//
	// // 得到auth server返回的信息
	// String authResultJson = this.getAuthResult(authInfo);
	//
	// JsonNode authResult = new ObjectMapper().readTree(authResultJson).findValue("result");
	//
	// // 缓存auth server发回的结果
	// ClientConfig.getInstance().addAuthResult(authResult, this);
	//
	// // 得到interface的地址
	// JsonNode interfaceNode = authResult.findValue("interface");
	// String interfaceIp = interfaceNode.findValue("socket_ip").asText();
	// int interfacePort = interfaceNode.findValue("socket_port").asInt();
	//
	// // 连接interface
	// SocketAddress address = new InetSocketAddress(interfaceIp, interfacePort);
	// NetInitializer net = new NetInitializer(this);
	// net.connectToInterface(address);
	// }

	/*
	 * 第二版服务器登录过程
	 */
	public void connect()
		throws Exception
	{
		String interfaceIp = ClientConfig.getInstance().getInterfaceIp();
		int interfacePort = ClientConfig.getInstance().getInterfacePort();

		SocketAddress address = new InetSocketAddress(interfaceIp, interfacePort);
		NetInitializer net = new NetInitializer(this);
		net.connectToInterface(address);
	}

	public String getAuthServerInfo(String ip, int port)
		throws Exception
	{
		final String arg = "{\"version\":\"0.0.1\",\"platform\":\"Android\",\"revision\":\"1\",\"gmrevision\":\"1\",\"channelId\":\"1\",\"subChannelId\":\"1\",\"deviceType\":\"Android\"}";
		HashMap<String, Object> httpArgs = new HashMap<>();
		httpArgs.put("json", Converter.jsonToUrl(arg));

		HttpUri gwUri = new HttpUri(ProtocolType.HTTP, ip, port, new String[] {"client"}, httpArgs);
		String authInfo = new HttpClient().get(gwUri);
		logger.info("AuthServerInfo : {}.", authInfo);
		return authInfo;
	}

	public String getAuthResult(String authInfo)
		throws Exception
	{
		@SuppressWarnings("unchecked")
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String> authMap = mapper.readValue(authInfo, HashMap.class);
		String ip = authMap.get("ip");
		HashMap<String, Object> authArgs = new HashMap<>();
		authArgs.put("platform", "test");
		authArgs.put("channel", "test");
		authArgs.put("username", username);
		authArgs.put("code", "code");
		HttpUri authUri = new HttpUri(ip, authArgs);
		String authResult = new HttpClient().get(authUri);
		logger.info("AuthResult : {}.", authResult);
		return authResult;
	}

	protected void sendToInterface(GeneratedMessage message)
	{
		this.interfaceConnection.write(RoleService.callbackSeed.getAndIncrement(), message);
	}

	protected void sendToGame(GeneratedMessage message)
	{
		this.gameConnection.write(RoleService.callbackSeed.getAndIncrement(), message);
	}

	protected void sendToBattle(GeneratedMessage message)
	{
		this.battleConnection.write(RoleService.callbackSeed.getAndIncrement(), message);
	}

	protected void sendToClub(GeneratedMessage message)
	{
		this.clubConnection.write(RoleService.callbackSeed.getAndIncrement(), message);
	}

	// private void authConnect(int accountId, int areaId, String connectToken)
	// {
	// // CIConnectAuthREQ.Builder builder = CIConnectAuthREQ.newBuilder();
	// // builder.setAccountId(accountId);
	// // builder.setAreaId(areaId);
	// // builder.setConnectToken(connectToken);
	// // sendToInterface(builder.build(), ProtocolsConfig.P_CI_CONNECT_AUTH_REQ);
	// }

	public void loginGame(int gameId)
	{
		this.initGameConnection(gameId);

		CGLoginREQ.Builder builder = CGLoginREQ.newBuilder();
		builder.setRoleId(this.roleId);
		builder.setNickname("nickname");
		builder.setHeadImageUrl("head");
		builder.setSex(1);
		builder.setAccountId(this.accountId);
		builder.setChannel("test");
		sendToGame(builder.build());
	}

	public void createRoom(int type, int count)
	{
		logger.info("create room(): " + this.roleId);
		CGCreateRoomREQ.Builder builder = CGCreateRoomREQ.newBuilder();
		builder.setRoomType(type);
		builder.setRoundCount(count);

		List<Integer> list = ClientConfig.getInstance().getGamePlaysList();

		builder.addAllGameplays(list);

		this.sendToGame(builder.build());
	}

	public void enterRoom(int roomId)
	{
		logger.info("enter room(): " + this.roleId);
		CBEnterRoomREQ.Builder builder = CBEnterRoomREQ.newBuilder();
		builder.setRoomId(roomId);
		builder.setNickname(this.username);
		builder.setRoleId(this.roleId);
		builder.setSex(0);
		builder.setHeadImageUrl("https://www.baidu.com/");

		this.sendToBattle(builder.build());
	}

	public void playCard(int playType, ByteString card)
	{
		logger.debug("playCard(), roleId={}, playType={}, card={}, handCard={}", roleId, playType, card.toByteArray(), this.handCards);

		CBPlayCardREQ.Builder builder = CBPlayCardREQ.newBuilder();
		builder.setCards(card);
		builder.setPlayType(playType);

		this.sendToBattle(builder.build());
	}

	public void voteDestroyRoom(int type)
	{
		CBVoteDestroyREQ.Builder builder = CBVoteDestroyREQ.newBuilder();
		builder.setType(type);
		this.sendToGame(builder.build());
	}

	public void quitRoom()
	{
		logger.debug("quitRoom(): " + this.roleId);

		CBQuitRoomREQ.Builder builder = CBQuitRoomREQ.newBuilder();
		this.sendToBattle(builder.build());
	}

	public void queryBattleId(int roomId)
	{
		this.roomId = roomId;

		CGQueryBattleIdREQ.Builder builder = CGQueryBattleIdREQ.newBuilder();
		builder.setRoomId(roomId);

		this.sendToGame(builder.build());
	}

	public void updateStatus()
	{
		CBUpdateStatusREQ.Builder builder = CBUpdateStatusREQ.newBuilder();
		builder.setStatus(this.status);

		this.sendToBattle(builder.build());
	}

	public void getContactInfo()
	{

	}

	public void chat(int type, String content, int code)
	{
		if (type < 1 || type > 3)
		{
			logger.error("Player chat(): type error");
			return;
		}

		CBChatREQ.Builder builder = CBChatREQ.newBuilder();
		builder.setType(type);
		builder.setContent(content);
		builder.setCode(code);
		this.sendToBattle(builder.build());
	}

	public void sendMarquee()
	{
		CGMarqueeREQ.Builder builder = CGMarqueeREQ.newBuilder();
		builder.setVersion(1);

		this.sendToGame(builder.build());
	}

	public void sendMail()
	{
		CGMailREQ.Builder builder = CGMailREQ.newBuilder();
		builder.setTime(10);

		this.sendToGame(builder.build());
	}

	public void sendHistoryREQ()
	{
		CGHistoryREQ.Builder builder = CGHistoryREQ.newBuilder();
		builder.setVersion(1);

		this.sendToGame(builder.build());
	}

	public void onInterfaceConnect(Connection connection)
	{
		this.interfaceConnection = connection;
		this.interfaceConnection.setConnectionType(Connection.CONNECTION_TYPE_CLIENT);

		// 保存玩家和connection的对应关系
		RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
		roleService.addPlayerByConn(connection, this);

		CIAccountAuthREQ.Builder builder = CIAccountAuthREQ.newBuilder();
		builder.setChannel("test");
		builder.setUsername(this.username);
		builder.setRefreshToken(" ");
		builder.setCode(" ");
		builder.setProVersion("14097");

		this.sendToInterface(builder.build());
	}

	public void onInterfaceDisconnect(Connection connection)
	{
		this.gameConnection = null;
		this.battleConnection = null;
	}

	public void onLogin(GCLoginRES res)
	{
		if (PlatformProtocolsConfig.GC_LOGIN_SUCCESS == res.getResult())
		{
			logger.debug(this.roleId + " login success");
			if (ClientConfig.getInstance().getStressServer() == ClientConfig.GAME_STRESS)
			{
				// 压测game
				// try
				// {
				// Thread.sleep(ThreadLocalRandom.current().nextInt(300));
				// }
				// catch (InterruptedException e)
				// {
				// e.printStackTrace();
				// }
				this.logout();
			}
			else if (ClientConfig.getInstance().getStressServer() == ClientConfig.BATTLE_STRESS)
			{
				this.sendHistoryREQ();
				// 压测battle
				if (StressTool2.getInstance().isAllLoginSuccess(this.threadIndex))
				{
					StressTool2.getInstance().hostCreateRoom(this.threadIndex);
				}
			}
			else if (ClientConfig.getInstance().getStressServer() == ClientConfig.CLUB_STRESS)
			{
				logger.info("club club club, {}", this.threadIndex);
				if (this.isManager())
				{
					logger.info("club manager begin");
					// 如果是经理，登陆成功开始创建俱乐部
					int data = 0;
					try
					{
						data = createClub();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					if (data > 1)
					{
						// 创建成功了！
						this.setClubId(data);
						Club club = new Club();
						club.setId(data);
						String code = "";
						try
						{
							code = InviteCodeEncoder.generateCode(data, this.getRoleId());
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						club.setInviteCode(code);
						club.setAgentId(this.threadIndex);
						club.setManagerId(this.getRoleId());
						Clubs.getInstance().addNewClub(club);
						doGetApplicantList(data); // 经理请求申请列表，无限通过要加入的人
					}
					else
					{
						logger.info("create club error : {}.", data);
					}
					logger.info("club manager end");
				}
				else
				{
					// 不是经理，那么申请加入俱乐部
					doJoinClubInfo();
				}
			}
		}
		else if (PlatformProtocolsConfig.GC_LOGIN_FAILED == res.getResult())
		{
			// this.loginGame(this.gameConnection.getConnectionId());
		}
	}

	/**
	 * 创建俱乐部
	 * 
	 * @return
	 * @throws Exception
	 */
	private int createClub()
		throws Exception
	{
		logger.info("club createClub begin");
		GmtHttpClient client = new GmtHttpClient();
		HashMap<String, Object> map = new HashMap<>();
		map.put("roleId", this.getRoleId());
		map.put("agentId", this.getRoleId());
		map.put("clubName", this.getUsername() + "'s Club");
		map.put("handler", "ClubGmtCreateNewClubHandler");
		map.put("server_id", 17170433); // 俱乐部的id
		String result = client.doServerGet(map);
		ObjectMapper mapper = new ObjectMapper();
		Map json = mapper.readValue(result, Map.class);

		logger.info("club createClub end");
		return (int)json.get("data");
	}

	/**
	 * gmt添加房卡
	 * 
	 * @param club
	 * @return
	 * @throws Exception
	 */
	private Map addClubRoomCard(Club club)
		throws Exception
	{
		GmtHttpClient client = new GmtHttpClient();
		HashMap<String, Object> map = new HashMap<>();

		int cards = 1000;
		String key = "mahjong@#$%&*2017!@#$%&*club@#$%&*";
		String oriStr = this.getRoleId() + "|" + club.getId() + "|" + club.getAgentId() + "|" + cards + "|" + key;

		map.put("roleId", this.getRoleId());
		map.put("clubId", this.getClubId());
		map.put("agentId", club.getAgentId());
		map.put("cards", cards);
		map.put("sign", MD5BY32(oriStr));

		map.put("handler", "ClubAgtAddRoomCardHandler");
		map.put("server_id", 17170433); // 俱乐部的id

		String result = client.doServerGet(map);
		ObjectMapper mapper = new ObjectMapper();
		Map json = mapper.readValue(result, Map.class);

		return json;
	}

	/**
	 * md5 32位加密
	 *
	 * @param key
	 * @return
	 */
	private static String MD5BY32(String key)
	{
		String result = "";
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(key.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuilder buf = new StringBuilder("");
			for (byte aB : b)
			{
				i = aB;
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			result = buf.toString();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 俱乐部经理获取申请列表
	 * 
	 * @param clubId
	 */
	public void doGetApplicantList(int clubId)
	{
		ClubProtoBuf.CCLClubApplicantListREQ.Builder builder = ClubProtoBuf.CCLClubApplicantListREQ.newBuilder();

		builder.setClubId(clubId);

		sendToClub(builder.build());
	}

	/**
	 * 获取自己的所有俱乐部
	 */
	public void doGetAllClub()
	{
		ClubProtoBuf.CCLAllClubREQ.Builder builder = ClubProtoBuf.CCLAllClubREQ.newBuilder();

		builder.setRoleId(this.getRoleId());

		sendToClub(builder.build());
	}

	/**
	 * 通过申请
	 * 
	 * @param proto
	 */
	public void doAgreeApply(ClubProtoBuf.ClubMemberInfoPROTO proto)
	{
		int roleId = proto.getRoleId();

		ClubProtoBuf.CCLClubApplicantREQ.Builder builder = ClubProtoBuf.CCLClubApplicantREQ.newBuilder();

		builder.setClubId(this.getClubId());
		builder.setOptype(com.kodgames.club.constant.ClubConstants.CLUB_APPLICANT_OPTYPE.ACCEPT);
		builder.setRoleId(roleId);
		builder.setTitle(com.kodgames.club.constant.ClubConstants.CLUB_MEMEBER_TITLE.MEMBER);

		sendToClub(builder.build());
	}

	/**
	 * 获取clubindex，id除clubNum余数，保证所有人都有俱乐部
	 * 
	 * @return
	 */
	private int getClubIndex()
	{
		return this.threadIndex % ClubConstants.ClubNum;
	}

	/**
	 * 根据clubindex获取俱乐部对象
	 * 
	 * @return
	 */
	private Club getMyClub()
	{
		logger.info("get my club enter");
		Club club;
		do
		{
			club = Clubs.getInstance().getClubFromIndex(getClubIndex());
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			logger.info("get my club, {}", club);
			// 等待俱乐部经理建好！
		} while (club == null);

		logger.info("get my club end");
		return club;
	}

	/**
	 * 查询俱乐部加入数据
	 */
	public void doJoinClubInfo()
	{
		Club club = getMyClub();

		logger.info("do join club info {} ", club);

		ClubProtoBuf.CCLJoinClubInfoREQ.Builder builder = ClubProtoBuf.CCLJoinClubInfoREQ.newBuilder();

		builder.setInvitationCode(club.getInviteCode());

		sendToClub(builder.build());
	}

	/**
	 * 加入一个已经查询过的俱乐部
	 */
	public void doJoinClub()
	{
		Club club = getMyClub();

		ClubProtoBuf.CCLJoinClubREQ.Builder builder = ClubProtoBuf.CCLJoinClubREQ.newBuilder();

		builder.setInvitationCode(club.getInviteCode());

		sendToClub(builder.build());
	}

	/**
	 * 获取俱乐部的信息
	 * 
	 * @param clubId
	 */
	public void doGetClubInfo(int clubId)
	{
		ClubProtoBuf.CCLClubInfoREQ.Builder builder = ClubProtoBuf.CCLClubInfoREQ.newBuilder();

		builder.setClubId(clubId);

		sendToClub(builder.build());
	}

	/**
	 * 使用gmt接口添加俱乐部房卡，然后根据角色属性来创建牌桌或者进入空牌桌
	 * 
	 * @param clubId
	 */
	public void doAddClubRoomCard(int clubId)
	{
		Club club = Clubs.getInstance().getClubFromClubId(clubId);
		if (club == null)
		{
			logger.error("club not found::::::::::::::{}", clubId);
			return;
		}

		try
		{
			Map result = addClubRoomCard(club);

			if (!(result.get("data") instanceof Integer))
			{
				// 不是数字说明加成功了！
				if (this.isClubRoomCreator())
				{
					doCreateClubRoom(clubId);
				}
				else
				{
					doGetAllClubTables();
				}
			}
			else
			{
				logger.error("club add cards error::::::{}", result.get("data"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 创建俱乐部牌桌
	 */
	public void doCreateClubRoom(int clubId)
	{
		CCLCreateRoomREQ.Builder builder = CCLCreateRoomREQ.newBuilder();

		builder.setClubId(clubId);
		builder.setCreatorId(roleId);
		builder.setRoomType(0);

		ArrayList<Integer> plays = new ArrayList();
		plays.add(65537);
		plays.add(196610);
		plays.add(327681);
		plays.add(458753);
		builder.addAllGameplays(plays);
		builder.setRoundCount(2);

		sendToClub(builder.build());
	}

	/** 进入俱乐部房间 */
	public void onCreateClubRoom(CLCCreateRoomRES res)
	{
		this.initBattleConnection(res.getBattleId());
		enterRoom(res.getRoomId());
	}

	public void onEnterClubRoom(ClubProtoBuf.CLCEnterRoomRES res)
	{
		this.initBattleConnection(res.getBattleId());
		enterRoom(res.getRoomId());
	}

	public void doQuitClub()
	{
		ClubProtoBuf.CCLQuitClubREQ.Builder builder = ClubProtoBuf.CCLQuitClubREQ.newBuilder();

		builder.setClubId(this.getClubId());

		sendToClub(builder.build());
	}

	/**
	 * 请求该玩家的所有空俱乐部牌桌
	 */
	public void doGetAllClubTables()
	{

		ClubProtoBuf.CCLClubTableREQ.Builder builder = ClubProtoBuf.CCLClubTableREQ.newBuilder();

		builder.setRoleId(this.getRoleId());
		builder.setClubId(0); // 请求空牌桌，反正就一个俱乐部

		sendToClub(builder.build());
	}

	public void doEnterClubRoom(int clubId, int roomId)
	{
		CCLEnterRoomREQ.Builder builder = CCLEnterRoomREQ.newBuilder();
		builder.setClubId(clubId);
		builder.setRoomId(roomId);

		sendToClub(builder.build());
	}

	private void logout()
	{
		CGLogoutREQ.Builder builder = CGLogoutREQ.newBuilder();

		this.sendToGame(builder.build());
	}

	public void onLogout(GCLogoutRES res)
	{
		if (PlatformProtocolsConfig.GC_LOGOUT_SUCCESS == res.getResult())
		{
			if (ClientConfig.getInstance().getStressServer() == ClientConfig.GAME_STRESS)
			{
				// 压测game
				try
				{
					Thread.sleep(ThreadLocalRandom.current().nextInt(300));
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				this.loginGame(0);
			}
			else if (ClientConfig.getInstance().getStressServer() == ClientConfig.AUTH_STRESS)
			{
				// 压测auth
				try
				{
					Thread.sleep(ThreadLocalRandom.current().nextInt(300));
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				CIAccountAuthREQ.Builder builder = CIAccountAuthREQ.newBuilder();
				builder.setChannel("test");
				builder.setUsername(this.username);
				builder.setRefreshToken(" ");
				builder.setCode(" ");
				this.sendToInterface(builder.build());
			}
		}
	}

	/*
	 * jiangzhen, 2016,9.8
	 */
	public void onCreateClubRoom(GCCreateRoomRES res)
	{
		if (PlatformProtocolsConfig.GC_CREATE_ROOM_SUCCESS == res.getResult())
		{
			this.roomId = res.getRoomId();

			this.initBattleConnection(res.getBattleId());

			// 创建一个房间实例
			Room room = new Room();
			room.setLocalRoomId(res.getRoomId());
			RoomService roomService = ServiceContainer.getInstance().getPublicService(RoomService.class);
			roomService.addRoomById(res.getRoomId(), room);

			room.createRoom(this);

			this.enterRoom(this.roomId);
		}
		else
		{
			logger.debug(this.roleId + " - 创建房间失败");
		}
	}

	public void onEnterRoom(BCEnterRoomRES res)
	{
		if (ProtocolsConfig.BC_ENTER_ROOM_SUCCESS == res.getResult())
		{
			logger.info(this.roleId + " enter room: " + this.roomId);

			if (ClientConfig.getInstance().getStressServer() == ClientConfig.GAME_STRESS)
			{
				// 压测game
				try
				{
					Thread.sleep(ClientConfig.getInstance().getCreateQuitInterval());
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				this.quitRoom();
			}
			else if (ClientConfig.getInstance().getStressServer() == ClientConfig.BATTLE_STRESS)
			{
				// 压测battle
				// 玩家是房主
				if (threadIndex % 4 == 0)
				{
					StressTool2.getInstance().playerQueryRoom(this.threadIndex);
				}
				this.status = MahjongConstant.PlayerStatus.READY;
				// this.status |= MahjongConstant.PlayerStatus.SELECT_DUN_LA_PAO;

				this.updateStatus();
			}
			else if (ClientConfig.getInstance().getStressServer() == ClientConfig.CLUB_STRESS)
			{
				// 压测的是club
				if (this.isClubRoomCreator())
					return; // 房主就等着
				this.quitRoom(); // 其他人退出
			}
		} else {
			if (ClientConfig.getInstance().getStressServer() == ClientConfig.CLUB_STRESS)
			{
				// 压测的是club
				if (this.isClubRoomCreator())
					return; // 房主就等着
				this.doGetAllClubTables(); // 进入失败就请求所有牌桌再进
			}
		}
	}

	/*
	 * jiangzhen 2016.9.18
	 */
	public void onRoomPlayerInfoSYN(BCRoomPlayerInfoSYN syn, int callback)
	{

	}

	public void onQueryBattleId(GCQueryBattleIdRES message)
	{
		if (PlatformProtocolsConfig.GC_QUERY_BATTLEID_SUCCESS == message.getResult())
		{
			this.initBattleConnection(message.getBattleId());

			this.enterRoom(this.roomId);
		}
		else if (PlatformProtocolsConfig.GC_QUERY_BATTLEID_FAILED_ROOM_NOT_EXIST == message.getResult())
		{
			// this.queryBattleId(this.roomId);
		}
	}

	public void onUpdatePlayerStatus(BCUpdateStatusRES res, int callback)
	{
		if (ProtocolsConfig.BC_UPDATE_PLAYERSTATUS_SUCCESS == res.getResult())
		{

		}
	}

	public void onBattlePlayerInfoSYN(BCBattlePlayerInfoSYN message)
	{
		if (this.threadIndex % 4 == 0)
		{
			// StressTool2.getInstance().startChat(this.threadIndex);
			// StressTool2.getInstance().startMarquee(this.threadIndex);
			// StressTool2.getInstance().startMail(this.threadIndex);
		}
		for (int i = 0; i < message.getPlayersCount(); ++i)
		{
			BattlePlayerInfoPROTO playerInfo = message.getPlayers(i);
			if (playerInfo.getRoleId() == this.roleId)
			{
				if (this.handCards.num() == 0)
				{
					this.handCards.add(playerInfo.getHandCards().toByteArray());
				}
				else
				{
					this.handCards.init(playerInfo.getHandCards());
				}
			}
		}
	}

	public void onPlayStepSYN(BCPlayStepSYN message)
	{
		for (int i = 0; i < message.getStepsCount(); ++i)
		{
			PlayStepPROTO playStep = message.getSteps(i);

			if (playStep.getRoleId() != this.roleId)
			{
				continue;
			}

			logger.debug("role={}, handCard={}", this.roleId, this.handCards);

			// 如果玩家胡牌了或者玩家输了或者黄庄了，开始一下局
			// if (playStep.getPlayType() == MahjongConstant.PlayType.OPERATE_HU)
			// {
			// this.updateStatus();
			// break;
			//// this.quitRoom();
			// }

			boolean isPass = false;

			// 选择过的概率是百分之三十
			if (ThreadLocalRandom.current().nextInt(100) < ClientConfig.getInstance().getPassProbability())
			{
				isPass = true;
			}
			else
			{
				isPass = false;
			}

			// 玩家选择过
			if (isPass)
			{
				// 如果玩家可以过
				if (playStep.getPlayType() == MahjongConstant.PlayType.OPERATE_CAN_PASS)
				{
					this.playCard(MahjongConstant.PlayType.OPERATE_PASS, playStep.getCards());
					break;
				}
			}
			// 玩家不选择过
			// 如果玩家抓牌
			if (playStep.getPlayType() == MahjongConstant.PlayType.OPERATE_DEAL)
			{
				ByteString card = playStep.getCards();
				this.handCards.add(playStep.getCards().toByteArray());
			}

			// 如果玩家可以出牌
			if (playStep.getPlayType() == MahjongConstant.PlayType.OPERATE_CAN_PLAY_A_CARD)
			{
				try
				{
					Thread.sleep(ClientConfig.getInstance().getDealPlayInterval());
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				if (this.handCards.num() > 0)
				{
					int index = this.handCards.num() - 1;
					Byte bt = this.handCards.remove(index);
					byte[] bytes = new byte[1];
					bytes[0] = bt.byteValue();
					ByteString byteString = ByteString.copyFrom(bytes);
					this.playCard(MahjongConstant.PlayType.OPERATE_PLAY_A_CARD, byteString);
					break;
				}
			}

			// 如果玩家可以吃牌
			if (playStep.getPlayType() == MahjongConstant.PlayType.OPERATE_CAN_CHI_A_CARD)
			{
				this.playCard(MahjongConstant.PlayType.OPERATE_CHI_A_CARD, playStep.getCards());

				break;
			}

			// 如果玩家可以碰牌
			if (playStep.getPlayType() == MahjongConstant.PlayType.OPERATE_CAN_PENG_A_CARD)
			{
				this.playCard(MahjongConstant.PlayType.OPERATE_PENG_A_CARD, playStep.getCards());
				if (this.handCards.num() >= 2)
				{
					this.handCards.remove(playStep.getCards().byteAt(0));
					this.handCards.remove(playStep.getCards().byteAt(0));
				}

				break;
			}

			// 如果玩家可以杠牌
			// if (playStep.getPlayType() == MahjongConstant.PlayType.OPERATE_CAN_GANG_A_CARD)
			// {
			// this.playCard(MahjongConstant.PlayType.OPERATE_GANG_A_CARD, playStep.getCards());
			// if (this.handCards.num() >= 3)
			// {
			// this.handCards.remove(playStep.getCards().byteAt(0));
			// this.handCards.remove(playStep.getCards().byteAt(0));
			// this.handCards.remove(playStep.getCards().byteAt(0));
			// }
			//
			// break;
			// }

			// 如果玩家可以补杠
			if (playStep.getPlayType() == MahjongConstant.PlayType.OPERATE_CAN_BU_GANG_A_CARD)
			{
				this.playCard(MahjongConstant.PlayType.OPERATE_BU_GANG_A_CARD, playStep.getCards());
				if (this.handCards.num() >= 1)
				{
					this.handCards.remove(playStep.getCards().byteAt(0));
				}
				break;
			}

			// 如果玩家可以暗杠
			if (playStep.getPlayType() == MahjongConstant.PlayType.OPERATE_CAN_AN_GANG)
			{
				this.playCard(MahjongConstant.PlayType.OPERATE_AN_GANG, playStep.getCards());
				if (this.handCards.num() >= 4)
				{
					this.handCards.remove(playStep.getCards().byteAt(0));
					this.handCards.remove(playStep.getCards().byteAt(0));
					this.handCards.remove(playStep.getCards().byteAt(0));
					this.handCards.remove(playStep.getCards().byteAt(0));
				}

				break;
			}

			// 如果玩家可以胡牌
			if (playStep.getPlayType() == MahjongConstant.PlayType.OPERATE_CAN_HU)
			{
				this.playCard(MahjongConstant.PlayType.OPERATE_HU, playStep.getCards());
				break;
			}
		}
	}

	public void onPlayCard(BCPlayCardRES message, int callback)
	{
		if (ProtocolsConfig.BC_PLAYCARD_SUCCESS == message.getResult())
		{

		}
	}

	public void onChat(BCChatRES message, int callback)
	{

	}

	public void onChatSYN(BCChatSYN message, int callback)
	{
	}

	public void onMarqueeRES(GCMarqueeRES message, int callback)
	{

	}

	public void onMailRES(GCMailRES message, int callback)
	{

	}

	public void onHistoryRES(GCHistoryRES message, int callback)
	{

	}

	public void onQuitRoom(BCQuitRoomRES message, int callback)
	{
		if (ClientConfig.getInstance().getStressServer() == ClientConfig.GAME_STRESS)
		{
			// this.createRoom(1, 1);
		}
		else if (ClientConfig.getInstance().getStressServer() == ClientConfig.BATTLE_STRESS)
		{

		}
		else if (ClientConfig.getInstance().getStressServer() == ClientConfig.CLUB_STRESS)
		{
			double p = Math.random();

			if (p < 0.7)
			{
				// 70%几率继续进入牌桌
				this.doGetAllClubTables();
			}
			else
			{
				// 30%的几率退出俱乐部
				this.doQuitClub();
			}
		}
	}

	public void onAccountAuth(ICAccountAuthRES message, int callback)
	{
		if (ClientConfig.getInstance().getStressServer() == ClientConfig.AUTH_STRESS)
		{
			// 压测auth
			// try
			// {
			// Thread.sleep(ThreadLocalRandom.current().nextInt(500));
			// }
			// catch (InterruptedException e)
			// {
			// e.printStackTrace();
			// }
			this.initGameConnection(message.getGameServerId());
			this.logout();
		}
		// 压测game和battle都是这一流程
		else
		{
			this.roleId = message.getRoleId();
			RoleService roleService = ServiceContainer.getInstance().getPublicService(RoleService.class);
			roleService.addPlayerById(this.roleId, this);
			this.interfaceConnection.setRemotePeerID(this.roleId);
			this.loginGame(message.getGameServerId());
			this.initClubConnection(message.getClubServerId()); // 初始化俱乐部的连接
		}
	}

	public void onMatchResult(BCMatchResultSYN message)
	{
		this.handCards.clear();
		// try
		// {
		// Thread.sleep(3000*60);
		// }
		// catch (InterruptedException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		this.updateStatus();
	}

	public void onDestroyRoomSYN(BCDestroyRoomSYN message)
	{
		// 压测battle
		// 房主创建房间
		if (this.threadIndex % 4 == 0)
		{
			this.createRoom(1, 1);
		}
	}

	public void onFinalMacthResultSYN(BCFinalMatchResultSYN message)
	{
		if (this.threadIndex % 4 == 0)
		{
			this.createRoom(1, 1);
		}
	}

	public int getClubId()
	{
		return clubId;
	}

	public void setClubId(int clubId)
	{
		this.clubId = clubId;
	}
}

class HandCard
{
	private List<Byte> handcards;

	public HandCard()
	{
		handcards = new LinkedList<Byte>();
	}

	public void init(ByteString byteString)
	{
		byteString.forEach(b -> {
			handcards.add(b);
		});
	}

	public Byte remove(int index)
	{
		return handcards.remove(index);
	}

	public void remove(byte b)
	{
		handcards.remove(handcards.indexOf(b));
	}

	public void remove(byte[] bytes)
	{
		for (int i = 0; i < bytes.length; ++i)
		{
			handcards.remove(bytes[i]);
		}
	}

	public void add(Byte b)
	{
		handcards.add(b);
	}

	public void add(byte[] b)
	{
		for (int i = 0; i < b.length; ++i)
		{
			Byte bt = new Byte(b[i]);
			this.handcards.add(bt);
		}
	}

	public int num()
	{
		return handcards.size();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for (Byte b : handcards)
		{
			sb.append(b + " ");
		}

		return sb.toString();
	}

	public void clear()
	{
		this.handcards.clear();
	}
}