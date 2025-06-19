package com.ruoyi.domain.dto.yima;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 回调参数
@AllArgsConstructor
@NoArgsConstructor
@Data
public class YimaPayNotifyRequest {

    //商户编号
    private String appId;

    //订单号
    private String out_trade_no;

    private String trade_no;

    private String platform_trade_no;

    private String channel;

    //订单金额
    private String amount;

    //实付金额
    private String true_amount;

    private String pay_time;

    private String description;

    private String attach;

    //签名
    private String sign;

    private String trade_state;

    private String time_expire;

    private String create_time;
}
