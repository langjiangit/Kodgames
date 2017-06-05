//package com.kodgames.manageserver.service.exchangecode;
//
//import com.kodgames.corgi.core.service.PlayerService;
//import com.kodgames.corgi.core.service.ServiceContainer;
//import com.kodgames.manageserver.common.ExchangeCodeConfig;
//import com.kodgames.manageserver.service.exchangecode.util.DesDecryptor;
//import com.kodgames.manageserver.service.exchangecode.util.FormatorUtil;
//import com.kodgames.manageserver.service.servers.ControlService;
//import com.kodgames.message.proto.servers.ServerProtoBuf.ExchangeCodeForServerREQ;
//import com.kodgames.message.proto.servers.ServerProtoBuf.ExchangeCodeForServerRES;
//import com.kodgames.message.protocol.ProtocolsConfig;
//
//public class ExchangeCodeForServerREQService extends PlayerService
//{
//	private static final long serialVersionUID = 4398689573215740630L;
//	public static final int CODE_LENGTH = 17;
//
//	/**
//	 * 处理manager发回来的兑换码处理结果，并发给客户端
//	 */
//	public ExchangeCodeForServerRES useExchangeCodeResult(ExchangeCodeForServerREQ message)
//	{
//		ExchangeCodeForServerRES.Builder builder = ExchangeCodeForServerRES.newBuilder();
//
//		String codeStr = message.getExchangeCode();
//		long playerId = message.getRoleId();
//		int areaId = message.getAreaId();
//		int channelId = message.getChannelID();
//		int result = ProtocolsConfig.EXCHANGE_CODE_USE_SUCCESS;
//
//		do
//		{
//			// 判断是否为空
//			if (codeStr == null || codeStr.length() == 0)
//			{
//				result = ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_NO_EXCHANGE_CODE;
//				break;
//			}
//
//			// 长度是否满足
//			if (codeStr.length() != CODE_LENGTH)
//			{
//				result = ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_ERROR_FORMAT;
//				break;
//			}
//
//			// 转为大写
//			codeStr = codeStr.toUpperCase();
//			// 兑换码前缀
//			String codePrefix = codeStr.substring(0, 4);
//			ControlService service = ServiceContainer.getInstance().getPublicService(ControlService.class);
//			// 根据前缀获取对应配置文件
//			ExchangeCodeConfig codeConfig = service.getExchangeCodeConfig(codePrefix);
//
//			if (codeConfig == null)
//			{
//				result = ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_ERROR_CODE;
//				break;
//			}
//
//			// 当前时间
//			long now = System.currentTimeMillis();
//
//			// 校验时间控制
//			if (!ExchangeCodeForServerREQHelper.isTimeValid(now, codeConfig))
//			{
//				result = ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_INVALID_TIME;
//				break;
//			}
//
//			// 渠道控制
//			if (!codeConfig.getChannelLimit().contains(channelId) && !codeConfig.isChannelUnlimit())
//			{
//				result = ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_INVALID_CHANNEL;
//				break;
//			}
//
//			// 区控制
//			if (!codeConfig.getAreaLimit().contains(areaId) && !codeConfig.isAreaUnlimit())
//			{
//				result = ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_INVALID_AREA;
//				break;
//			}
//
//			// 判断新的兑换码是否在配置范围内,并排除兑换码中的Ii0o
//			if (!FormatorUtil.isLegal17DigitAndLetter(codeStr))
//			{
//				result = ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_ERROR_CODE;
//				break;
//			}
//
//			String encryptContext = codeStr.substring(4);
//			DesDecryptor decryptor = new DesDecryptor();
//			String plainText = decryptor.deCreateExchangeCode(encryptContext, codePrefix);
//
//			// 校验兑换码是否合法
//			if (!ExchangeCodeForServerREQHelper.isRealExchangeCode(codePrefix, plainText, codeConfig, encryptContext))
//			{
//				result = ProtocolsConfig.EXCHANGE_CODE_USE_FAILED_ERROR_CODE;
//				break;
//			}
//
//			// 使用兑换码
//			result = ExchangeCodeForServerREQHelper.useExchangeCode(codeStr, codePrefix, playerId, now, codeConfig);
//
//			if (result != ProtocolsConfig.EXCHANGE_CODE_USE_SUCCESS)
//			{
//				break;
//			}
//
//			// 奖励
//			builder.setRewardId(codeConfig.getRewardId());
//			String desc = codeConfig.getDescription();
//			if (desc != null && !desc.trim().equals(""))
//			{
//				builder.setDescription(desc);
//			}
//		} while (false);
//
//		builder.setRoleId(playerId);
//		builder.setResult(result);
//		builder.setClientCallback(message.getClientCallback());
//		return builder.build();
//	}
//}
