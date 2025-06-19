package com.ruoyi.thirdparty.jiujia.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
public class VisaVerificationBody {

    private String totalFee;
    private String resultCode;
    private String tradeNo;
    private String outTradeNo;

    private String sign;
}
