package com.kodgames.game.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Liufei on 2017/5/6.
 */
public class SecurityCondition {
    private Logger logger = LoggerFactory.getLogger(SecurityCondition.class);

    private SecurityGroupConfig.ConditionType conditionType;        // 条件类型
    private SecurityGroupConfig.CompareType compareType;            // 参数比较类型
    private int minValue;           // 参数值
    private int maxValue;
    private String strValue;

    public void setParams(SecurityGroupConfig.ConditionType conditionType, SecurityGroupConfig.CompareType compareType, int minValue, int maxValue, String strValue)
    {
        this.conditionType = conditionType;
        this.compareType = compareType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.strValue = strValue;
    }

    public SecurityGroupConfig.ConditionType getType()
    {
        return conditionType;
    }

    /**
     * 检查给定的信息是否满足该组的所有条件
     * @param registDays 注册天数
     * @param combatCounts 最近7天的局数
     * @param agencyId 最后购买房卡的代理商id
     * @param regionName ip所在地区
     */
    public boolean matchCondition(int registDays, int combatCounts, int agencyId, String regionName)
    {
        switch (conditionType)
        {
            case REGISTER:      // 注册天数
                return compareIntValue(registDays);

            case COMBAT:        // 比赛局数
                return compareIntValue(combatCounts);

            case AGENCY5:       // 代理商id取余5
                return compareIntValue(agencyId % 5);

            case AGENCY10:      // 代理商id取余10
                return compareIntValue(agencyId % 10);

            case REGION:        // ip所属地区
                return compareStrValue(regionName);
        }

        logger.warn("unknown condition type {}", conditionType.toString());
        return false;
    }

    // 比较整数类型的参数
    private boolean compareIntValue(int value)
    {
        // 整数参数支持所有的比较类型
        switch (compareType)
        {
            case GE:
                return value >= this.minValue;

            case LE:
                return value <= this.minValue;

            case NE:
                return value != this.minValue;

            case LESS:
                return value < this.minValue;

            case EQUAL:
                return value == this.minValue;

            case GREATER:
                return value > this.minValue;

            case BETWEEN:
                return value >= this.minValue && value < this.maxValue;
        }

        logger.warn("unknown compare type {}", compareType.toString());
        return false;
    }

    // 比较字符串类型的参数
    private boolean compareStrValue(String value)
    {
        if (value == null)
            return false;

        // 字符串参数只支持 == 和 !=
        switch (compareType)
        {
            case EQUAL:
                return  value.equals(this.strValue);

            case NE:
                return !value.equals(this.strValue);
        }

        logger.warn("unsupported compare type {}", compareType.toString());
        return false;
    }
}
