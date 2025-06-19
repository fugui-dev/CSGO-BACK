package com.ruoyi.thirdparty.msPay.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class MsPayConfig {

//    @Value("${zcconfig.apiUrl}")
    private String apiUrl = "http://9ec2518d.zhaocai202403.xyz";

//    @Value("${zcconfig.appId}")
    private String appId = "1067092";

//    @Value("${zcconfig.apiKey}")
    private String apiKey = "DWX9uDKeGkZdE9QKyedyGWX3l7ET7u9t";

//    @Value("${zcconfig.notifyUrl}")
    private String notifyUrl = "http://api.f99skins.com/prod-api/api/qs/notify";

//    @Value("${zcconfig.successUrl}")
    private String successUrl = "http://api.f99skins.com/prod-api/api/qs/notify";

//    @Value("${zcconfig.errorUrl}")
    private String errorUrl = "http://api.f99skins.com/prod-api/api/qs/notify";

}
