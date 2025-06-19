package com.ruoyi.domain.dto.mayi;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiPayAddOrderResponse {

    //状态
    private String status;

    //消息
    private String msg;

    //金额
    private String pay_amount;

    //订单号
    private String pay_orderid;

    //支付地址
    private String payUrl;

}
