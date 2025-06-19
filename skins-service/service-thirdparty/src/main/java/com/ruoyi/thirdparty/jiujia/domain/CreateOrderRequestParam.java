package com.ruoyi.thirdparty.jiujia.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestParam {

    @JsonProperty("app_key")
    private String appKey;

    @JsonProperty("api_domain")
    private String apiDomain;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("pay_type")
    private Integer payType;

    @JsonProperty("goods_id")
    private Integer goodsId;

    @JsonProperty("goods_price")
    private BigDecimal goodsPrice;

    @JsonProperty("goods_num")
    private Integer goodsNum;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("member_id")
    private String memberId;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("sign")
    private String sign;

    @JsonProperty("user_ip")
    private String userIp;

}
