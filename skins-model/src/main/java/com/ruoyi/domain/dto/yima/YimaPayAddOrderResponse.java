package com.ruoyi.domain.dto.yima;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class YimaPayAddOrderResponse {

    //状态
    private String resultCode;

    //消息
    private String message;

    //金额
    private Data Data;


    @lombok.Data
    public static class Data{
        private String out_trade_no;
        private String trade_no;
        private String body;
        private String channel;
    }
}
