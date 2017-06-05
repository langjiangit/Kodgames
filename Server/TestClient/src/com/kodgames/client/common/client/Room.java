package com.kodgames.client.common.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.client.service.room.RoomService;

public class Room
{
	private static Logger logger = LoggerFactory.getLogger(Room.class);

	private Player[] players = new Player[RoomService.ROOM_SIZE];

	private int serverRoomId = -1;
	private int localRoomId = -1;
	private int currPlayerNum = 0;

	public Room()
	{
		this.localRoomId = RoomService.localRoomIdSeed.getAndIncrement();
		this.currPlayerNum = 0;
	}
	
	public void setLocalRoomId(int localRoomId){
		this.localRoomId = localRoomId;
	}
	
	public int getLocalRoomId(){
		return this.localRoomId;
	}

	/*
	 * jiangzhen,2016.9.8
	 * 玩家创建房间时调用
	 */
	public void createRoom(Player player)
	{
		logger.info("Room createRoom()");
	}

	/*
	 * jiangzhen, 2016.9.8
	 * 玩家进入房间时调用
	 */
	public void enterRoom(Player player)
	{
		logger.info("Room enterRoom()");
		if (this.currPlayerNum >= RoomService.ROOM_SIZE){
			logger.error("curr player num equals room size");
			return;
		}
		this.currPlayerNum++;
		this.players[this.currPlayerNum-1] = player;
	}

	public void stopBattle()
	{
		
	}
}
