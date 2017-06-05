package com.kodgames.game.service.gmtools;

import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.game.common.Constant;
import com.kodgames.game.service.mail.MailService;
import com.kodgames.gmtools.handler.GmtHandlerAnnotation;
import com.kodgames.gmtools.handler.IGmtoolsHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lz on 2016/8/27.
 * gmt发送邮件
 */
@GmtHandlerAnnotation(handler = "SetNoticeHandler")
public class SendMailHandler implements IGmtoolsHandler
{
	@Override public HashMap<String, Object> getResult(Map<String, Object> map)
	{
		HashMap<String, Object> result = new HashMap<>();

		//邮件类型 个人邮件/群体邮件
		int type = (int)map.get("type");
		//邮件文字内容
		String msg = (String)map.get("msg");
		int targetId = 0;
		if (type == Constant.MailConstant.TYPE_PERSONAL_MAIL)
		{
			targetId = (int)map.get("user_id");
		}
		int sender = (int)map.get("sender");
		MailService service = ServiceContainer.getInstance().getPublicService(MailService.class);
		service.sendMail(type, msg, targetId, sender);
		result.put("result", 1);
		return result;
	}
}
