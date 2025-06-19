package com.ruoyi.thirdparty.wechat.domain;

import java.math.BigDecimal;

public class PayOrderParam {
    private Integer coinItemId;//商户商品id
    private Integer coinItemNum; //商户商品数量
    private BigDecimal coinItemAmount; //商户商品价格

    public Integer getCoinItemId() {
        return coinItemId;
    }

    public void setCoinItemId(Integer coinItemId) {
        this.coinItemId = coinItemId;
    }

    public Integer getCoinItemNum() {
        return coinItemNum;
    }

    public void setCoinItemNum(Integer coinItemNum) {
        this.coinItemNum = coinItemNum;
    }

    public BigDecimal getCoinItemAmount() {
        return coinItemAmount;
    }

    public void setCoinItemAmount(BigDecimal coinItemAmount) {
        this.coinItemAmount = coinItemAmount;
    }
}
