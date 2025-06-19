package com.ruoyi.thirdparty.wechat.entity;

import java.math.BigDecimal;

public class TtCoinRechargeParam {

    /** 金币 */
    private BigDecimal coin;

    private BigDecimal minCoin;

    private BigDecimal maxCoin;

    /** 订单状态：0未付款，1已付款，2已取消 */
    private String payStatus;

    /** 用户ID */
    private Long uid;
    private Long rid;

    /** 用户名 */
    private String uname;

    /** 订单号 */
    private String orderNo;


    /** 订单号 */
    private String  beginTime;
    private String  endTime;

    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
    }

    public BigDecimal getCoin() {
        return coin;
    }

    public void setCoin(BigDecimal coin) {
        this.coin = coin;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getMinCoin() {
        return minCoin;
    }

    public void setMinCoin(BigDecimal minCoin) {
        this.minCoin = minCoin;
    }

    public BigDecimal getMaxCoin() {
        return maxCoin;
    }

    public void setMaxCoin(BigDecimal maxCoin) {
        this.maxCoin = maxCoin;
    }
}
