package com.ruoyi.thirdparty.note.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "yun-xin")
public class YunXinProperties {

    private String appKey;
    private String appSecret;
    private String templateId;
    private String serverUrl;
}
