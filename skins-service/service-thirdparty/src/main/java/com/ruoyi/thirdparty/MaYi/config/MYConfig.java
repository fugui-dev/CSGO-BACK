package com.ruoyi.thirdparty.MaYi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mayi")
public class MYConfig {

    private String apiKey;
    private String gateway;
    private String memberid;
    private String notifyBaseUrl;
    private String callBackUrl;
    // （银行）通道编码
    private String payBankCode;

    // 下单接口
    public static final String ApiPayAddOrder = "/Pay_AddOrder";

    // 查询订单接口
    public static final String ApiQueryTrans = "/Pay_Trade_query.html";

}
