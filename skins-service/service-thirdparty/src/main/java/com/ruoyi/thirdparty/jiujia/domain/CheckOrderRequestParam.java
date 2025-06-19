package com.ruoyi.thirdparty.jiujia.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
public class CheckOrderRequestParam {

    @JsonProperty("app_key")
    private String appKey;

    @JsonProperty("member_id")
    private String memberId;

    @JsonProperty("out_trade_no")
    private String outTradeNo;
}
