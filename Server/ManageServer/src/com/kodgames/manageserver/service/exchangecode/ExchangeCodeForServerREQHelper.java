//package com.kodgames.manageserver.service.exchangecode;
//
//import java.util.Date;
//
//import com.kodgames.manageserver.common.ExchangeCodeConfig;
//import com.kodgames.manageserver.service.exchangecode.util.DesDecryptor;
//import com.kodgames.manageserver.service.exchangecode.util.FormatorUtil;
//import com.kodgames.manageserver.service.exchangecode.util._62_10_Transformer;
//import com.kodgames.message.protocol.ProtocolsConfig;
//
//public class ExchangeCodeForServerREQHelper
//{
//
//	/**
//	 * 校验当前时间是否为这个兑换码的有效期内
//	 */
//	public static boolean isTimeValid(long now, ExchangeCodeConfig codeConfig)
//	{
//		if (now >= codeConfig.getAbsoluteTimeStart() && now <= codeConfig.getAbsoluteTimeEnd())
//		{
//			return true;
//		}
//
//		return false;
//	}
//
//	/**
//	 * 校验兑换码是否合法
//	 */
//	public static boolean isRealExchangeCode(String sign, String plainText, ExchangeCodeConfig configBean,
//		String encryptContext)
//	{
//		if (sign == null || sign.length() != 4)
//		{
//			return false;
//		}
//		else
//		{
//			if (!FormatorUtil.isLegal8alphaDigit(plainText))
//			{
//				return false;
//			}
//			if (!plainText.substring(0, 4).equals(sign))
//			{
//				return false;
//			}
//
//			DesDecryptor decryptor = new DesDecryptor();
//			String activityCode = decryptor.createActiveCode(plainText);
//			if (activityCode.length() != ExchangeCodeForServerREQService.CODE_LENGTH)
//			{
//				return false;
//			}
//
//			if (!activityCode.substring(4).equals(encryptContext))
//			{
//				return false;
//			}
//
//			String decimi62 = plainText.substring(4);
//			int hitNumber = Integer.parseInt(_62_10_Transformer.convertBase62ToDecimal(decimi62));
//			int lower = 0;
//			int higher = configBean.getGenCount();
//			if (hitNumber < lower || hitNumber >= higher)
//			{
//				return false;
//			}
//			return true;
//		}
//	}
//
//	/**
//	 * 使用兑换码，并返回使用结果（成功或是失败的错误码）
//	 */
//	public static int useExchangeCode(String codeStr, String codePrefix, long playerId, long now, ExchangeCodeConfig codeConfig)
//	{
//		IExchangeCodeUsedBeanDao usedDao = DBCS.getExector(IExchangeCodeUsedBeanDao.class);
//		Integer playerUserSameBatchCount = usedDao.selectBatchSamePlayerUsedCount(codePrefix, playerId);
//		// 该玩家已使用本批次兑换码的数量
//		if (playerUserSameBatchCount != null && playerUserSameBatchCount >= codeConfig.getCodeMaxUseNum())
//		{
//			return ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_ALREADY_USED_SAME_PREFIX;
//		}
//
//		// 已被同一玩家使用过
//		if (usedDao.selectExchangeCodeUsedBean(codeStr, playerId) != null)
//		{
//			return ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_ALREADY_USED;
//		}
//
//		IExchangeCodeBeanDao codeDao = DBCS.getExector(IExchangeCodeBeanDao.class);
//		ExchangeCodeBean codeBean = codeDao.selectExchangeCodeBean(codeStr);
//		if (codeBean == null)
//		{
//			codeBean = new ExchangeCodeBean();
//			codeBean.setCode(codeStr);
//			codeDao.insertExchangeCodeBean(codeBean);
//		}
//
//		// 已经达到该激活码的使用次数上限 || 直接把数据库中该兑换码使用次数加1，如果增加失败则表示该兑换码此刻已经达到使用次数上限
//		if (codeBean.getPlayer_use_count() >= codeConfig.getCodeMaxPlyerUseNum() || codeDao.addExchangeCodeBeanPlayerUseCount(codeStr, codeConfig.getCodeMaxPlyerUseNum()) < 1)
//		{
//			return ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_ALREADY_USED;
//		}
//
//		ExchangeCodeUsedBean usedBean = new ExchangeCodeUsedBean();
//		usedBean.setCode(codeStr);
//		usedBean.setCode_prefix(codePrefix);
//		usedBean.setPlayer_id(playerId);
//		usedBean.setUse_time(new Date(now));
//
//		// 增加该玩家使用此兑换码记录
//		usedDao.insertExchangeCodeUsedBean(usedBean);
//
//		return ProtocolsConfig.EXCHANGE_CODE_USE_SUCCESS;
//	}
//}
