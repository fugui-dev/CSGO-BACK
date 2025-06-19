package com.ruoyi.thirdparty.yimapay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
 * @description
 * @date 2025/6/12
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "yima")
public class YimaConfig {
    private String apiKey;
    private String gateway;
    private String appId;
    private String notifyBaseUrl;
    private String callBackUrl;

    // 下单接口
    public static final String ApiPayAddOrder = "/transactions/order.html";

    // 查询订单接口
    public static final String ApiQueryTrans = "/orderid.html";

}
