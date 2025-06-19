package com.ruoyi.thirdparty.wechat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 金币变动记录对象 tt_coin_record
 * 
 * @author ruoyi
 * @date 2023-07-06
 */
public class TtCoinRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**  */
    private Long id;

    /** 所属用户 */
    @Excel(name = "所属用户")
    private Long uid;

    /** 用户名 */
    @Excel(name = "用户名")
    private String uname;

    /** 变动类型 */
    @Excel(name = "变动类型",dictType = "change_type")
    private String type;


    /** 变动类型 */
    @Excel(name = "操作场景",dictType = "operation_type")
    private String operType;

    /** 奖励类型 */
    @Excel(name = "奖励类型",dictType = "reward_type")
    private String rewardType;

    /** 变动金额 */
    @Excel(name = "变动金额")
    private BigDecimal money;

    /** 返佣时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    /** 状态 */
    @Excel(name = "状态")
    private Long status;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setUid(Long uid) 
    {
        this.uid = uid;
    }

    public Long getUid() 
    {
        return uid;
    }
    public void setUname(String uname) 
    {
        this.uname = uname;
    }

    public String getUname() 
    {
        return uname;
    }
    public void setType(String type) 
    {
        this.type = type;
    }

    public String getType() 
    {
        return type;
    }
    public void setRewardType(String rewardType) 
    {
        this.rewardType = rewardType;
    }

    public String getRewardType() 
    {
        return rewardType;
    }
    public void setMoney(BigDecimal money) 
    {
        this.money = money;
    }

    public BigDecimal getMoney() 
    {
        return money;
    }
    public void setTime(Date time)
    {
        this.time = time;
    }

    public Date getTime()
    {
        return time;
    }
    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
    {
        return status;
    }

    public String getOperType() {
        return operType;
    }

    public void setOperType(String operType) {
        this.operType = operType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("uid", getUid())
            .append("uname", getUname())
            .append("type", getType())
                .append("oper_type", getOperType())
            .append("rewardType", getRewardType())
            .append("money", getMoney())
            .append("time", getTime())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
