package com.kodgames.agent.service.diamond;

import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.service.PublicService;
import com.kodgames.message.proto.agent.AgentProtoBuf.TCDiamondJournalRES;
import com.kodgames.message.proto.agent.AgentProtoBuf.TCHaveDiamondRES;
import com.kodgames.message.protocol.PlatformProtocolsConfig;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodgames.core.net.http.HttpClient;
import com.kodgames.core.net.http.HttpRequestCallback;
import com.kodgames.core.net.http.HttpUri;


public class DiamondService extends PublicService
{
	private static final long serialVersionUID = -4114350409325028614L;
	private static final String WEB_AGENT_HOST = "";
	private static final int WEB_AGENT_PORT = 0;
	
	/**
	 * 查看代理商钻石数量
	 */
	private static final String CHECK_DIAMOND_URL = "http://"+ WEB_AGENT_HOST + ":" + WEB_AGENT_PORT + "/agtools/html/game_agent/query_agency_card.do";
	
	/**
	 * 查看代理商获得钻石记录(包含购买及赠送)
	 */
	private static final String GET_DIAMOND_RECORD_URL = "http://"+ WEB_AGENT_HOST + ":" + WEB_AGENT_PORT + "/agtools/html/game_agent/buy_card_record.do";

	/**
	 * 查询代理商赠送钻石记录
	 */
	private static final String GIVE_DIAMOND_RECORD_URL = "http://"+ WEB_AGENT_HOST + ":" + WEB_AGENT_PORT + "/agtools/html/game_agent/add_card_record.do";
	
	/**
	 * 赠送钻石给普通玩家
	 */
	private static final String GIVE_PLAYER_DIAMOND_URL = "http://"+ WEB_AGENT_HOST + ":" + WEB_AGENT_PORT + "/agtools/html/game_agent/add_card.do";
	
	/**
	 * 赠送钻石给俱乐部玩家
	 */
	private static final String GIVE_CLUB_PLAYER_DIAMOND_URL = "http://"+ WEB_AGENT_HOST + ":" + WEB_AGENT_PORT + "/agtools/html/game_agent/add_club_card.do";
	

	private static HttpClient client = null;
	
	private static Logger logger = LoggerFactory.getLogger(DiamondService.class);
	
	private static HttpClient getHttpClient() throws Exception{
		if(client == null) {
			client = new HttpClient();
		}
		return client;
	}
	
	public void getDiamond(int agencyId,String phone,Connection connection,int callback)
	{
		HttpClient client;
		HttpUri uri;
		TCHaveDiamondRES.Builder builder = TCHaveDiamondRES.newBuilder();
		try
		{
			client = getHttpClient();
			uri = new HttpUri(CHECK_DIAMOND_URL);
		}
		catch (Exception e)
		{
			logger.error("get httpclient failed or create uri failed");
			e.printStackTrace();
			builder.setResult(PlatformProtocolsConfig.TC_HAVE_DIAMOND_RES_FAILED);
			connection.write(callback, builder.build());
			return;
		}
		AbstractMap<String, Object> args = new HashMap<>();
		args.put("agencyId", agencyId);
		args.put("phone", phone);
		uri.setArgs(args);
		
		client.asyncGet(uri, new HttpRequestCallback()
		{
	
			@Override
			public void onResult(String json)
			{
				ObjectMapper mapper = new ObjectMapper();
				try
				{
					HashMap<String, Object> response = mapper.readValue(json, new TypeReference<HashMap<String, Object>>(){});
					int result = (int)response.get("result");
					int data = (int)response.get("data");
					
					if(result == 1) {
						if(data != -1) {		//成功返回
							builder.setResult(PlatformProtocolsConfig.TC_HAVE_DIAMOND_RES_SUCCESS);
							builder.setDiamondHave(data);
							connection.write(callback, builder.build());
						}else {
							connection.write(callback, builder.setResult(PlatformProtocolsConfig.TC_HAVE_DIAMOND_RES_AGENT_NOT_EXIST).build());
						}
					}else {
						connection.write(callback, builder.setResult(PlatformProtocolsConfig.TC_HAVE_DIAMOND_RES_FAILED).build());
					}
				}
				catch (IOException e)
				{
					logger.error("parse json error : {}",json);
					e.printStackTrace();
					connection.write(callback, builder.setResult(PlatformProtocolsConfig.TC_HAVE_DIAMOND_RES_FAILED).build());
				}
			}
		});
	}
	
	
	public void checkGetDiamondRecod(int agencyId,String phone,Connection connection,int callback)
	{
		HttpClient client;
		HttpUri uri;
		TCDiamondJournalRES.Builder builder = TCDiamondJournalRES.newBuilder();
		try
		{
			client = getHttpClient();
			uri = new HttpUri(GET_DIAMOND_RECORD_URL);
		}
		catch (Exception e)
		{
			logger.error("get httpclient failed or create uri failed");
			e.printStackTrace();
			builder.setResult(PlatformProtocolsConfig.TC_DIAMOND_JOURNAL_RES_FAILED);
			connection.write(callback, builder.build());
			return;
		}
		AbstractMap<String, Object> args = new HashMap<>();
		args.put("agencyId", agencyId);
		args.put("phone", phone);
		uri.setArgs(args);
		
		client.asyncGet(uri, new HttpRequestCallback()
		{
	
			@Override
			public void onResult(String json)
			{
				ObjectMapper mapper = new ObjectMapper();
				try
				{
					HashMap<String, Object> response = mapper.readValue(json, new TypeReference<HashMap<String, Object>>(){});
					int result = (int)response.get("result");
					Object data = response.get("data");
					
					if(result == 1){
						if((int)data == -1) {			//错误返回
							builder.setResult(PlatformProtocolsConfig.TC_DIAMOND_JOURNAL_RES_AGENT_NOT_EXIST);
							connection.write(callback, builder.build());
						}else {
							@SuppressWarnings("unchecked")
							List<HashMap<String, Object>> records = (List<HashMap<String, Object>>)data;
							for(HashMap<String, Object> record : records) {
								int type = (int)record.get("type");
								int amount = (int)record.get("amount");
								String time = (String)record.get("time");
								String typeStr = "";
								if(type == 0) {
									typeStr = "充值";
								}else if (type == 3) {
									typeStr = "购买获赠";
								}else if (type == 5) {
									typeStr = "获赠";
								}
								String recordStr = "【" + time + "】 您" + typeStr + "蓝钻 " + "【" + amount + "】 " + "个";
								builder.addJournal(recordStr);
							}
							connection.write(callback, builder.build());
						}
					}else {
						connection.write(callback, builder.setResult(PlatformProtocolsConfig.TC_DIAMOND_JOURNAL_RES_FAILED).build());
					}
				}
				catch (IOException e)
				{
					logger.error("parse json error : {}",json);
					e.printStackTrace();
					connection.write(callback, builder.setResult(PlatformProtocolsConfig.TC_DIAMOND_JOURNAL_RES_FAILED).build());
				}
			}
		});
	}
	
	public void checkGiveDiamondRecod(int agencyId,String phone,Connection connection,int callback)
	{
		HttpClient client;
		HttpUri uri;
		TCDiamondJournalRES.Builder builder = TCDiamondJournalRES.newBuilder();
		try
		{
			client = getHttpClient();
			uri = new HttpUri(GIVE_DIAMOND_RECORD_URL);
		}
		catch (Exception e)
		{
			logger.error("get httpclient failed or create uri failed");
			e.printStackTrace();
			builder.setResult(PlatformProtocolsConfig.TC_DIAMOND_JOURNAL_RES_FAILED);
			connection.write(callback, builder.build());
			return;
		}
		AbstractMap<String, Object> args = new HashMap<>();
		args.put("agencyId", agencyId);
		args.put("phone", phone);
		uri.setArgs(args);
		
		client.asyncGet(uri, new HttpRequestCallback()
		{
	
			@Override
			public void onResult(String json)
			{
				ObjectMapper mapper = new ObjectMapper();
				try
				{
					HashMap<String, Object> response = mapper.readValue(json, new TypeReference<HashMap<String, Object>>(){});
					int result = (int)response.get("result");
					Object data = response.get("data");
					
					if(result == 1){
						if((int)data == -1) {			//错误返回
							builder.setResult(PlatformProtocolsConfig.TC_DIAMOND_JOURNAL_RES_AGENT_NOT_EXIST);
							connection.write(callback, builder.build());
						}else {
							@SuppressWarnings("unchecked")
							List<HashMap<String, Object>> records = (List<HashMap<String, Object>>)data;
							for(HashMap<String, Object> record : records) {
								int playerId = (int)record.get("playerID");
								int amount = (int)record.get("amount");
								String time = (String)record.get("time");
								String recordStr = "【" + time + "】 赠送给玩家 【" + playerId + "】 蓝钻 【" + amount + "】 " + "个";
								builder.addJournal(recordStr);
							}
							connection.write(callback, builder.build());
						}
					}else {
						connection.write(callback, builder.setResult(PlatformProtocolsConfig.TC_DIAMOND_JOURNAL_RES_FAILED).build());
					}
				}
				catch (IOException e)
				{
					logger.error("parse json error : {}",json);
					e.printStackTrace();
					connection.write(callback, builder.setResult(PlatformProtocolsConfig.TC_DIAMOND_JOURNAL_RES_FAILED).build());
				}
			}
		});
	}
}
