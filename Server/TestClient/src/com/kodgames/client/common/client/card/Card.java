package com.kodgames.client.common.client.card;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;

/**
 * 2016.12.17
 * @author jiangzhen
 *
 * 表示一张牌
 */
public class Card
{
	private byte value;
	
	public Card(byte value)
	{
		this.value = value;
	}
	
	public void setValue(byte value)
	{
		this.value = value;
	}
	
	public byte getValue()
	{
		return this.value;
	}
	
	@Override
	public String toString()
	{
		return ""+value;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Card))
		{
			return false;
		}
		Card card = (Card)o;
		if (this.value != card.value)
		{
			return false;
		}
		return true;
	}
	
	public static List<Card> bytesToCardList(byte[] bytes)
	{
		List<Card> cardList = new ArrayList<Card>();
		for (byte b : bytes)
		{
			cardList.add(new Card(b));
		}
		
		return cardList;
	}
	
	public static List<Card> ByteStringToCardList(ByteString bs)
	{
		return bytesToCardList(bs.toByteArray());
	}
	
	/**
	 * 转为ByteString
	 * @return
	 */
	public ByteString toByteString()
	{
		byte[] bytes = new byte[1];
		bytes[0] = value;		
		return ByteString.copyFrom(bytes);
	}
}
