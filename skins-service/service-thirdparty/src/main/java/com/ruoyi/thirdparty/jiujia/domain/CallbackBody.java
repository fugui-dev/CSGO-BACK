package com.ruoyi.thirdparty.jiujia.domain;

import lombok.Data;

@Data
public class CallbackBody {

    private String member_id;
    private String total_fee;
    private String result_code;
    private String trade_no;
    private String out_trade_no;
    private String time_end;
    private String pay_type;
    private String ext_sign;
    private String sign;

}
