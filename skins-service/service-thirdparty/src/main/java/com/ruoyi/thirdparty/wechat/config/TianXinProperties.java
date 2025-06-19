package com.ruoyi.thirdparty.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tianxin")
public class TianXinProperties {

    private String version; // 版本号
    private String customerid;  // 商户编号
    private String paytype; // 支付编号
    private String notifyurl;   // 异步通知URL
    private String returnurl;   // 同步通知URL
    private String access_type; // 接入方式
    private String userkey; // 商户Key
    private String apiurl;  // 请求地址
}
