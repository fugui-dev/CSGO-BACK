package com.ruoyi.thirdparty.jiujia.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderBody {

    private BigDecimal totalAmount;
    private String orderId;

    public OrderBody(BigDecimal totalAmount, String orderId) {
        this.totalAmount = totalAmount;
        this.orderId = orderId;
    }

}
