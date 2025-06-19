package com.ruoyi.thirdparty.msPay.sdk.util.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {


    @JSONField(name = "code")
    private String code;

    @JSONField(name = "msg")
    private String msg;

    @JSONField(name = "merchant_id")
    private String merchantId;

    @JSONField(name = "trade_status")
    private String tradeStatus;

    @JSONField(name = "code_url")
    private String codeUrl;

    @JSONField(name = "order_no")
    private String orderNo;

    @JSONField(name = "out_trade_no")
    private String outTradeNo;


}
