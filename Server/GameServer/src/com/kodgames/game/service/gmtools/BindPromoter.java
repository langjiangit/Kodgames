package com.kodgames.game.service.gmtools;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.corgi.core.constant.DateTimeConstants;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.game.service.activity.WeiXinShareActivityService;
import com.kodgames.game.start.WxPromoterConfig;
import com.kodgames.gmtools.handler.GmtHandlerAnnotation;
import com.kodgames.gmtools.handler.IGmtoolsHandler;

import xbean.InviteeInfo;
import xbean.PromoterInfo;
import xbean.RoleInfo;

/**
 * Created by 001 on 2017/4/12.
 * 通知绑定
 */
@GmtHandlerAnnotation(handler = "BindPromoter")
public class BindPromoter implements IGmtoolsHandler
{
    private static Logger logger = LoggerFactory.getLogger(BindPromoter.class);

    @Override
    public HashMap<String, Object> getResult(Map<String, Object> args)
    {
        String promoter = (String)args.get("promoter");
        String invitee = (String)args.get("invitee");
        if (promoter == null || invitee == null)
        {
            logger.debug("param should not be null {} - {}", promoter, invitee);
            return responseResult(1);
        }

        WeiXinShareActivityService service = ServiceContainer.getInstance().getPublicService(WeiXinShareActivityService.class);
        if (!service.isActive())
        {
            logger.debug("not in weixin share activity time");
            return responseResult(5);
        }
        
        // 玩家不能自己绑定自己
        if (promoter.equals(invitee))
        {
        	logger.debug("promoter and invitee are same, return");
        	return responseResult(6);
        }

        // 检查promoter是否满足推广员条件
        PromoterInfo promoterInfo = table.Promoter_info.update(promoter);
        if (promoterInfo == null)
        {
            promoterInfo = table.Promoter_info.insert(promoter);
        }
        
        Integer promoterRoleId = table.Unionid_2_roleid.select(promoter);
        if (promoterRoleId == null)
        {
            logger.debug("hasn't find item in unionid_2_roleid table for promoter {}", promoter);
            return responseResult(2);
        }

        RoleInfo promoterRole = table.Role_info.select(promoterRoleId);
        if (promoterRole == null)
        {
            logger.warn("can't find role in role_info, unionid={}, roleId={}", promoter, promoterRoleId);
            return responseResult(2);
        }
        
        long now = System.currentTimeMillis();
        if (now - promoterRole.getRoleCreateTime() < WxPromoterConfig.getInstance().getPromoterNeedTime() * DateTimeConstants.DAY)
        {
            logger.warn("role registed time lower than required time, can't be inviter");
            return responseResult(3);
        }

        // 检查invitee是否已绑定过
        InviteeInfo inviteeInfo = table.Invitee_info.update(invitee);
        if (inviteeInfo != null)
        {
            String alreadyPromoter = inviteeInfo.getPromoterUnionId();
            logger.debug("role already bind to promoter {}", alreadyPromoter);
            return responseResult(4);
        }
        
        int inviteeRoleId = table.Unionid_2_roleid.select(invitee);
        RoleInfo inviteeRoleInfo = table.Role_info.select(inviteeRoleId);
        long startTime = WxPromoterConfig.getInstance().getStartTime();
        if (inviteeRoleInfo.getRoleCreateTime() <= startTime)
        {
        	logger.debug("invitee isn't new player!, roleId={}, roleInfo={}", inviteeRoleId, inviteeRoleInfo);
        	return responseResult(7);
        }

        // 生成绑定关系
        inviteeInfo = table.Invitee_info.insert(invitee);
        inviteeInfo.setFinished(0);
        inviteeInfo.setJoinTime(System.currentTimeMillis());
        inviteeInfo.setPromoterUnionId(promoter);

        promoterInfo.getInviteeUnionidList().add(invitee);

        return responseResult(0);
    }

    private HashMap<String, Object> responseResult(int data)
    {
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", 1);
        result.put("data", data);
        return result;
    }
}
