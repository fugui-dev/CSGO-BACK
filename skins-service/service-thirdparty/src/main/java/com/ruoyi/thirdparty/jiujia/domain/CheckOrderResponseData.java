package com.ruoyi.thirdparty.jiujia.domain;

import lombok.Data;

@Data
public class CheckOrderResponseData {
    private String total_amount;
    private String out_trade_no;
    private String trade_no;
    private String pay_time;
    private String status;
}
