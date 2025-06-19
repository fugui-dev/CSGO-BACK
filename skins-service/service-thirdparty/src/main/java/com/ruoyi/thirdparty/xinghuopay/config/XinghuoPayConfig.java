package com.ruoyi.thirdparty.xinghuopay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "xinghuopay")
public class XinghuoPayConfig {

    /**
     * 服务器URL
     */
    private String serverUrl;

    /**
     * 商户ID
     */
    private String pid;

    /**
     * 签名
     */
    private String sign;

    /**
     * 通知URL
     */
    private String notifyUrl;

    /**
     * 返回URL
     */
    private String returnUrl;
}
