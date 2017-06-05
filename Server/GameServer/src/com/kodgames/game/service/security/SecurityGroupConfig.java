package com.kodgames.game.service.security;

import com.kodgames.corgi.core.service.ServiceContainer;
import limax.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liufei on 2017/5/6.
 */
public class SecurityGroupConfig {
    private static Logger logger = LoggerFactory.getLogger(SecurityGroupConfig.class);
    private static SecurityGroupConfig instance = new SecurityGroupConfig();

    private static final String XML_IPGroupSecion = "IPGroup";
    private static final String XML_GroupConditionName = "GroupCondition";

    private static final String XML_CONDITIONTYPE_REGISTER = "REGISTER";
    private static final String XML_CONDITIONTYPE_COMBAT = "COMBAT";
    private static final String XML_CONDITIONTYPE_REGION = "REGION";
    private static final String XML_CONDITIONTYPE_AGENCY5 = "AGENCY5";
    private static final String XML_CONDITIONTYPE_AGENCY10 = "AGENCY10";

    private static final String XML_COMPARETYPE_LE = "LE";
    private static final String XML_COMPARETYPE_EQUAL = "EQUAL";
    private static final String XML_COMPARETYPE_LESS = "LESS";
    private static final String XML_COMPARETYPE_BETWEEN = "BETWEEN";
    private static final String XML_COMPARETYPE_GE = "GE";
    private static final String XML_COMPARETYPE_GREATER = "GREATER";
    private static final String XML_COMPARETYPE_NE = "NE";

    // 判定条件
    public enum ConditionType
    {
        REGISTER,   // 注册天数
        COMBAT,     // 战斗局数
        REGION,     // IP所属地区
        AGENCY5,    // 代理商ID取余5
        AGENCY10,   // 代理商ID取余5
    }

    // 比较方式
    public enum CompareType
    {
        LE,         // 小于等于
        EQUAL,      // 等于
        LESS,       // 小于
        BETWEEN,    // 范围 [min <= X < max)
        GE,         // 大于等于
        GREATER,    // 大于
        NE,         // 不等于
    }

    private SecurityGroupConfig()
    {

    }

    public static SecurityGroupConfig getInstance()
    {
        return instance;
    }

    /**
     * 加载配置文件
     * @param fileName 配置文件名
     */
    public void load(String fileName, SecurityService service)
    {
        logger.info("start load securitygroups xml");
        try
        {
            Element element = XMLUtils.getRootElement(fileName);
            loadImpl(element, service);
        }
        catch (Exception e)
        {
            logger.error("load security group config failed, exception = {}", e.getMessage());
        }
    }

    /**
     * 重新加载配置文件, 提供给GMT工具用于刷新配置
     * @param content 文件内容
     */
    public void reload(String content, SecurityService service)
    {
        logger.info("start reload securitygroups xml from String");
        InputStream is = new ByteArrayInputStream(content.getBytes());
        try
        {
            Element element = XMLUtils.getRootElement(is);
            loadImpl(element, service);
        }
        catch (Exception e)
        {
            logger.error("reload security group config failed, exception = {}", e.getMessage());
        }
    }

    private void loadImpl(Element element, SecurityService service)
    {
        if (service == null)
            service = ServiceContainer.getInstance().getPublicService(SecurityService.class);

        service.setOpen(element.getAttribute("isOpen").equalsIgnoreCase("true"));
        service.setDefaultGroup(element.getAttribute("defaultGroupName"));

        List<SecurityGroup> groups = new ArrayList<>();
        XMLUtils.getChildElements(element).forEach(subElem -> {
            if (subElem.getTagName().equals(XML_IPGroupSecion)) {
                SecurityGroup group = parseGroup(subElem);
                if (group != null) {
                    // 组名不允许重复
                    boolean allow = true;
                    for (SecurityGroup g : groups)
                    {
                        if (g.getName().equalsIgnoreCase(group.getName()))
                        {
                            logger.error("two group with same name {}", g.getName());
                            allow = false;
                            break;
                        }
                    }
                    if (allow)
                        groups.add(group);
                }
                else
                    logger.error("parse group failed for element {}", subElem.getTagName());
            }
            else
                logger.error("invalid xml element {} for securitygroup.xml", subElem.getTagName());
        });

        service.resetGroups(groups);
    }

    private SecurityGroup parseGroup(Element element)
    {
        SecurityGroup group = new SecurityGroup();
        group.setName(element.getAttribute("name"));
        group.setActive(element.getAttribute("isActive").equalsIgnoreCase("true"));

        List<SecurityCondition> conditions = new ArrayList<>();
        XMLUtils.getChildElements(element).forEach(subElem -> {
            if (subElem.getTagName().equals(XML_GroupConditionName)) {
                SecurityCondition condition = parseCondition(subElem);
                if (condition != null) {
                    // 同组内不允许出现相同类型的Condition
                    boolean allow = true;
                    for (SecurityCondition c : conditions)
                    {
                        if (c.getType() == condition.getType())
                        {
                            logger.error("not allow multiple condition with same type in group {}", c.getType().toString());
                            allow = false;
                            break;
                        }
                    }
                    if (allow)
                        conditions.add(condition);
                }
                else
                    logger.error("parse condition failed for element {}", subElem.getTagName());
            }
            else
                logger.error("invalid xml element {} for IPGroup", subElem.getTagName());
        });

        group.setConditions(conditions);
        return group;
    }

    private SecurityCondition parseCondition(Element element)
    {
        SecurityCondition condition = new SecurityCondition();
        String type = element.getAttribute("type");
        String ctype = element.getAttribute("compareType");
        String min = element.getAttribute("intMinValue");
        String max = element.getAttribute("intMaxValue");
        String str = element.getAttribute("strValue");

        SecurityGroupConfig.ConditionType conditionType;
        if (type.equalsIgnoreCase(XML_CONDITIONTYPE_REGISTER))
            conditionType = ConditionType.REGISTER;
        else if (type.equalsIgnoreCase(XML_CONDITIONTYPE_COMBAT))
            conditionType = ConditionType.COMBAT;
        else if (type.equalsIgnoreCase(XML_CONDITIONTYPE_REGION))
            conditionType = ConditionType.REGION;
        else if (type.equalsIgnoreCase(XML_CONDITIONTYPE_AGENCY5))
            conditionType = ConditionType.AGENCY5;
        else if (type.equalsIgnoreCase(XML_CONDITIONTYPE_AGENCY10))
            conditionType = ConditionType.AGENCY10;
        else
        {
            logger.error("invalid condition type {} for element {}", type, element.getTagName());
            return null;
        }

        SecurityGroupConfig.CompareType compareType;
        if (ctype.equalsIgnoreCase(XML_COMPARETYPE_LE))
            compareType = CompareType.LE;
        else if (ctype.equalsIgnoreCase(XML_COMPARETYPE_EQUAL))
            compareType = CompareType.EQUAL;
        else if (ctype.equalsIgnoreCase(XML_COMPARETYPE_LESS))
            compareType = CompareType.LESS;
        else if (ctype.equalsIgnoreCase(XML_COMPARETYPE_BETWEEN))
            compareType = CompareType.BETWEEN;
        else if (ctype.equalsIgnoreCase(XML_COMPARETYPE_GE))
            compareType = CompareType.GE;
        else if (ctype.equalsIgnoreCase(XML_COMPARETYPE_GREATER))
            compareType = CompareType.GREATER;
        else if (ctype.equalsIgnoreCase(XML_COMPARETYPE_NE))
            compareType = CompareType.NE;
        else {
            logger.error("invalid compare type {} for element {}", ctype, element.getTagName());
            return null;
        }

        int minVal;
        int maxVal;
        try {
            minVal = Integer.parseInt(min);
            maxVal = Integer.parseInt(max);
        } catch (Exception e)
        {
            logger.error("parse failed for exception: {}", e.getMessage());
            return null;
        }

        condition.setParams(conditionType, compareType, minVal, maxVal, str);
        return condition;
    }
}
