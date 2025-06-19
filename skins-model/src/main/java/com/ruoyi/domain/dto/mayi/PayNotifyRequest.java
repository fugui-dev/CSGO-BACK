package com.ruoyi.domain.dto.mayi;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 回调参数
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PayNotifyRequest {

    //商户编号
    private String memberid;

    //订单号
    private String orderid;

    //订单金额
    private String amount;

    //实付金额
    private String true_amount;

    //交易流水号
    private String transaction_id;

    //交易时间
    private String datetime;

    //交易状态
    private String returncode;

    //扩展返回
    private String attach;

    //签名
    private String sign;

}
