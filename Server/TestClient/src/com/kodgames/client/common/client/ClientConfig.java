package com.kodgames.client.common.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

import limax.util.XMLUtils;

//分为本地配置和服务配置
public class ClientConfig
{
	public static final int BATTLE_STRESS = 0;
	public static final int GAME_STRESS = 1;
	public static final int AUTH_STRESS = 2;
	public static final int CLUB_STRESS = 3;
	
	private static ClientConfig cc = null;
	private String interfaceIp;
	private int interfacePort;
	private int httpPort;
	
	private int dealPlayInterval;
	
	private int chatInterval;
	
	/*
	 * 跑马灯时间间隔
	 */
	private int marqueeInterval;
	
	/*
	 * 发邮件时间间隔
	 */
	private int mailInterval;
	
	/*
	 * 玩家创建房间和退出房间的间隔时间
	 */
	private int  createQuitInterval;
	
	/*
	 * 玩家选择过的概率,百分之
	 */
	private int passProbability;
	
	private int matchInterval;
	
	private List<Integer> gamePlaysList = new ArrayList<Integer>();
	
	//需要压测的服务器
	private int stressServer = -1;
	
	/*
	 * 缓存每个玩家的第一次auth应答内容
	 */
	private ConcurrentHashMap<Player, JsonNode> authResultMap = null;

	private ClientConfig(){
		this.interfaceIp = "10.23.1.170";
		this.interfacePort = 3671;
		this.httpPort = 13101;
		this.authResultMap = new ConcurrentHashMap<Player, JsonNode>();
		this.stressServer = ClientConfig.CLUB_STRESS;
	}
	
	public void init(String filename)
	{
		Element elem = null;
		
		try
		{
			elem = XMLUtils.getRootElement(filename);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		NodeList nl = elem.getChildNodes();
		
		for (int i = 0; i < nl.getLength(); ++i)
		{
			Node node = nl.item(i);
			
			if (node.getNodeName().equals("GamePlays"))
			{
				parseGamePlays(node);
			}
			else if (node.getNodeName().equals("constant"))
			{
				parseConstant(node);
			}
		}
		
		int x = 10;
		x =10;
	}
	
	private void parseConstant(Node node)
	{
		NamedNodeMap nnm = node.getAttributes();
	
		this.interfaceIp = nnm.getNamedItem("interfaceIp").getNodeValue();
		this.interfacePort = Integer.valueOf(nnm.getNamedItem("interfacePort").getNodeValue());
		this.httpPort = Integer.valueOf(nnm.getNamedItem("httpPort").getNodeValue());
		this.dealPlayInterval = Integer.valueOf(nnm.getNamedItem("dealPlayInterval").getNodeValue());
		this.chatInterval = Integer.valueOf(nnm.getNamedItem("chatInterval").getNodeValue());
		this.marqueeInterval = Integer.valueOf(nnm.getNamedItem("marqueeInterval").getNodeValue());
		this.mailInterval = Integer.valueOf(nnm.getNamedItem("mailInterval").getNodeValue());
		this.createQuitInterval = Integer.valueOf(nnm.getNamedItem("createQuit").getNodeValue());
		this.passProbability = Integer.valueOf(nnm.getNamedItem("passProbability").getNodeValue());
		this.matchInterval = Integer.valueOf(nnm.getNamedItem("matchInterval").getNodeValue());
	}
	
	private String getNodeAttributeValue(Node node, String attrName)
	{
		NamedNodeMap nnm = node.getAttributes();
		Node item = nnm.getNamedItem(attrName);
		
		return item.getNodeValue();
	}
	
	private void parseGamePlays(Node node)
	{
		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); ++i)
		{
			Node n = nl.item(i);
			if (n.getNodeName().equals("type"))
			{
				String type = this.getNodeAttributeValue(n, "name");
				this.parseStringToGamePlay(type);
			}
		}
	}
	
	private void parseStringToGamePlay(String type)
	{
//		Class guangDongRuleClass = Rules_GuangDong.class;
//		Field field;
//		Integer value = 0;
//
//		try
//		{
//			field = guangDongRuleClass.getField(type);
//			value = (Integer)field.get(guangDongRuleClass);
//		}
//		catch (NoSuchFieldException | SecurityException e)
//		{
//			e.printStackTrace();
//		}
//		catch (IllegalArgumentException e)
//		{
//			e.printStackTrace();
//		}
//		catch (IllegalAccessException e)
//		{
//			e.printStackTrace();
//		}
//		this.gamePlaysList.add(value);
	}
	
	public int getOneMatchInterval()
	{
		
		return 0;
	}
	
	public int getDealPlayInterval()
	{
		return dealPlayInterval;
	}

	public void setDealPlayInterval(int dealPlayInterval)
	{
		this.dealPlayInterval = dealPlayInterval;
	}

	public int getChatInterval()
	{
		return chatInterval;
	}

	public void setChatInterval(int chatInterval)
	{
		this.chatInterval = chatInterval;
	}

	public int getMarqueeInterval()
	{
		return marqueeInterval;
	}

	public void setMarqueeInterval(int marqueeInterval)
	{
		this.marqueeInterval = marqueeInterval;
	}

	public int getMailInterval()
	{
		return mailInterval;
	}

	public void setMailInterval(int mailInterval)
	{
		this.mailInterval = mailInterval;
	}

	public int getCreateQuitInterval()
	{
		return createQuitInterval;
	}

	public void setCreateQuitInterval(int createQuitInterval)
	{
		this.createQuitInterval = createQuitInterval;
	}

	public int getPassProbability()
	{
		return passProbability;
	}

	public void setPassProbability(int passProbability)
	{
		this.passProbability = passProbability;
	}

	public int getMatchInterval()
	{
		return matchInterval;
	}

	public void setMatchInterval(int matchInterval)
	{
		this.matchInterval = matchInterval;
	}

	public List<Integer> getGamePlaysList()
	{
		return gamePlaysList;
	}

	public void setGamePlaysList(List<Integer> gamePlaysList)
	{
		this.gamePlaysList = gamePlaysList;
	}

	public void setStressServer(int stressServer)
	{
		this.stressServer = stressServer;
	}
	
	public int getStressServer()
	{
		return this.stressServer;
	}
	
	public String getInterfaceIp(){
		return this.interfaceIp;
	}
	
	public int getInterfacePort(){
		return this.interfacePort;
	}
	
	public void addAuthResult(JsonNode authResult, Player player){
		this.authResultMap.put(player, authResult);
	}
	
	public JsonNode getAuthResult(Player player){
		return this.authResultMap.get(player);
	}
	
	public void removeAuthResult(Player player){
		this.authResultMap.remove(player);
	}
	
	public static ClientConfig getInstance(){
		if (cc == null){
			synchronized (ClientConfig.class) {
				if (cc == null){
					cc = new ClientConfig();
				}
			}
		}
		
		return cc;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}
}
