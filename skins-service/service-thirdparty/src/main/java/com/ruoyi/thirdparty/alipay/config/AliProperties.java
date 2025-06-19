package com.ruoyi.thirdparty.alipay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "alipay")
public class AliProperties {

    private String serverUrl;

    private String format;

    private String charset;

    private String signType;

    private List<MerchantConfig> merchants;

    private String notifyUrl;

    private String realNameAuthenticationReturnUrl;

}