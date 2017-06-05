/**
 * 2016.12.17
 * 
 * by jiangzhen
 */
package com.kodgames.client.common.client.card;

import java.util.List;

/**
 * 手牌管理器接口,作用如下：
 *    提供手牌操作的方法,比如添加一张牌，删除一张牌
 * @author jiangzhen
 *
 */
public interface HandCardMgr
{
	/**
	 *  添加一张卡片到手牌中
	 * @param card 要添加的牌
	 */
	public void addOneCard(Card card);
	
	/**
	 *  添加一些牌到手牌中
	 *  @param cardList 要添加的牌
	 */
	public void addCardList(List<Card> cardList);
	
	/**
	 * 删除指定数量的卡
	 * 
	 * @param card 要删除的卡
	 * @param num  要删出的数量
	 * @return true 成功 false 失败
	 */
	public boolean removeCards(Card card, int num);
	
	/**
	 * 删除一些牌
	 * @param cardList
	 */
	public void removeCardList(List<Card> cardList);
	
	/**
	 * 返回手牌数量
	 * @return 手牌数量
	 */
	public int cardNum();
	
	/**
	 * 返回指定下标的牌
	 * @param index
	 * @return
	 */
	public Card getCard(int index);
	
	/**
	 * 清除所有手牌
	 * @return
	 */
	public void clear();
}
