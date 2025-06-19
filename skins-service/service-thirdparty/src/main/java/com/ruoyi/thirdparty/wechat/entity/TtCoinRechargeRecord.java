package com.ruoyi.thirdparty.wechat.entity;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * 充值记录对象 tt_coin_recharge_record
 * 
 * @author ruoyi
 * @date 2023-07-06
 */
public class TtCoinRechargeRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 金币 */
    @Excel(name = "金币")
    private BigDecimal coin;

    /** 订单状态：0未付款，1已付款，2已取消 */
    @Excel(name = "订单状态",dictType = "alipay_status")
    private String payStatus;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long uid;

    /** 用户名 */
    @Excel(name = "用户名")
    private String uname;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 支付宝订单号 */
    @Excel(name = "支付宝订单号")
    private String callbackNo;

    /** 回调信息 */
    @Excel(name = "回调信息")
    private String callbackMsg;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setCoin(BigDecimal coin) 
    {
        this.coin = coin;
    }

    public BigDecimal getCoin() 
    {
        return coin;
    }
    public void setPayStatus(String payStatus)
    {
        this.payStatus = payStatus;
    }

    public String getPayStatus()
    {
        return payStatus;
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
    public void setOrderNo(String orderNo) 
    {
        this.orderNo = orderNo;
    }

    public String getOrderNo() 
    {
        return orderNo;
    }
    public void setCallbackNo(String callbackNo) 
    {
        this.callbackNo = callbackNo;
    }

    public String getCallbackNo() 
    {
        return callbackNo;
    }
    public void setCallbackMsg(String callbackMsg) 
    {
        this.callbackMsg = callbackMsg;
    }

    public String getCallbackMsg() 
    {
        return callbackMsg;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("coin", getCoin())
            .append("payStatus", getPayStatus())
            .append("uid", getUid())
            .append("uname", getUname())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("orderNo", getOrderNo())
            .append("callbackNo", getCallbackNo())
            .append("callbackMsg", getCallbackMsg())
            .toString();
    }
}
