package com.ruoyi.thirdparty.jiujia.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jiu-jia-pay")
public class JiuJiaProperties {

    private String aliPayUrl;
    private String memberId;
    private String appKey;
    private String apiSecret;
    private String apiDomain;
    private String callbackUrl;

}
