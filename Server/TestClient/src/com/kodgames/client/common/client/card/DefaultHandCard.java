package com.kodgames.client.common.client.card;

import java.util.ArrayList;
import java.util.List;

public class DefaultHandCard implements HandCardMgr
{
	private List<Card> handCard;
	
	public DefaultHandCard()
	{
		handCard = new ArrayList<Card>();
	}

	@Override
	public void addOneCard(Card card)
	{
		this.handCard.add(card);
	}

	@Override
	public void addCardList(List<Card> cardList)
	{
		this.handCard.addAll(cardList);
	}
	
	@Override
	public boolean removeCards(Card card, int num)
	{
		int i = 0;
		for (i = 0; i < num; ++i)
		{
			this.handCard.remove(card);
		}
		
		return i==num;
	}

	@Override
	public void removeCardList(List<Card> cardList)
	{
		// TODO jiangzhen 有待改善
		for (Card card : cardList)
		{
			this.handCard.remove(card);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuffer res = new StringBuffer();
		res.append("[ ");
		for (Card card : this.handCard)
		{
			res.append(card.toString()+" ");
		}
		res.append("]");
		
		return res.toString();
	}
	
	@Override
	public int cardNum()
	{
		return this.handCard.size();
	}
	
	@Override
	public Card getCard(int index)
	{
		return this.handCard.get(index);
	}
	
	@Override
	public void clear()
	{
		this.handCard.clear();
	}
}
