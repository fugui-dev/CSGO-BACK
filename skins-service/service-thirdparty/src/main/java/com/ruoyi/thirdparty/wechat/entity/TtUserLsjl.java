package com.ruoyi.thirdparty.wechat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 流水记录对象 tt_user_lsjl
 * 
 * @author junhai
 * @date 2023-08-19
 */
@Data
public class TtUserLsjl
{
    private static final long serialVersionUID = 1L;

    /** 流水id */
    private Long id;

    /** 用户id */
    @Excel(name = "用户id")
    private Long ttUserId;

    /** 上上级用户id **/
    private Long ttSsUserId;

    /** 业务类型：充值和消费 */
    @Excel(name = "业务类型：充值和消费")
    private String type;

    /** v币变动前 */
    @Excel(name = "v币变动前")
    private BigDecimal vCoinBefore;

    /** v币变动后 */
    @Excel(name = "v币变动后")
    private BigDecimal vCoinAfter;

    /** v币变动金额 */
    @Excel(name = "v币变动金额")
    private BigDecimal vCoinChange;

    /** 钻石变动前的金额 */
    @Excel(name = "钻石变动前的金额")
    private BigDecimal dCoinBefore;

    /** 钻石变动金额 */
    @Excel(name = "钻石变动金额")
    private BigDecimal dCoinChange;

    /** 钻石变动后的金额 */
    @Excel(name = "钻石变动后的金额")
    private BigDecimal dCoinAfter;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setTtUserId(Long ttUserId) 
    {
        this.ttUserId = ttUserId;
    }

    public Long getTtUserId() 
    {
        return ttUserId;
    }
    public void setType(String type) 
    {
        this.type = type;
    }

    public String getType() 
    {
        return type;
    }
    public void setvCoinBefore(BigDecimal vCoinBefore) 
    {
        this.vCoinBefore = vCoinBefore;
    }

    public BigDecimal getvCoinBefore() 
    {
        return vCoinBefore;
    }
    public void setvCoinAfter(BigDecimal vCoinAfter) 
    {
        this.vCoinAfter = vCoinAfter;
    }

    public BigDecimal getvCoinAfter() 
    {
        return vCoinAfter;
    }
    public void setvCoinChange(BigDecimal vCoinChange) 
    {
        this.vCoinChange = vCoinChange;
    }

    public BigDecimal getvCoinChange() 
    {
        return vCoinChange;
    }
    public void setdCoinBefore(BigDecimal dCoinBefore) 
    {
        this.dCoinBefore = dCoinBefore;
    }

    public BigDecimal getdCoinBefore() 
    {
        return dCoinBefore;
    }
    public void setdCoinChange(BigDecimal dCoinChange) 
    {
        this.dCoinChange = dCoinChange;
    }

    public BigDecimal getdCoinChange() 
    {
        return dCoinChange;
    }
    public void setdCoinAfter(BigDecimal dCoinAfter) 
    {
        this.dCoinAfter = dCoinAfter;
    }

    public BigDecimal getdCoinAfter() 
    {
        return dCoinAfter;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("ttUserId", getTtUserId())
            .append("type", getType())
            .append("vCoinBefore", getvCoinBefore())
            .append("vCoinAfter", getvCoinAfter())
            .append("vCoinChange", getvCoinChange())
            .append("dCoinBefore", getdCoinBefore())
            .append("dCoinChange", getdCoinChange())
            .append("dCoinAfter", getdCoinAfter())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
