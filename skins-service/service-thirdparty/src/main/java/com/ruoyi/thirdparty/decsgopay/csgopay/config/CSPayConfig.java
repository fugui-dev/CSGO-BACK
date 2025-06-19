package com.ruoyi.thirdparty.decsgopay.csgopay.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CSPayConfig {

//    @Value("${cspayconfig.apiUrl}")
    private String apiUrl = "https://pay.jfbjl.cn";

//    @Value("${cspayconfig.pid}")
    private String pid = "1004";

//    @Value("${cspayconfig.notifyUrl}")
    private String notifyUrl = "https://cf.991skins.com/prod-api/api/csPay/notify"; //线上地址要加prod-api

    //    @Value("${cspayconfig.returnUrl}")
    private String returnUrl = "https://cf.991skins.com";

//    @Value("${cspayconfig.md5key}")
    private String md5key = "y3iyzJPt3yrOccy3poOOyVGO7YePVCRI";


}
