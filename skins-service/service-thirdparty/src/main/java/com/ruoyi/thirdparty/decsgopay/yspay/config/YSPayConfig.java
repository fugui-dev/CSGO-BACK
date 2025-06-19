package com.ruoyi.thirdparty.decsgopay.yspay.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class YSPayConfig {

//    @Value("${cspayconfig.apiUrl}")
    private String apiUrl = "https://epay.zc67.cn";

//    @Value("${cspayconfig.pid}")
    private String pid = "2181";

//    @Value("${cspayconfig.notifyUrl}")
    private String notifyUrl = "http://api.txxlj.com/prod-api/api/ysPay/notify"; //线上地址要加prod-api
//    private String notifyUrl = "http://zzw3cr.natappfree.cc/api/ysPay/notify"; //线上地址要加prod-api

//    @Value("${cspayconfig.returnUrl}")
    private String returnUrl = "https://fbcs2.txxlj.com";

//    @Value("${cspayconfig.md5key}")
    private String md5key = "mv7zm7h4L7QrM7xAPFR7F7V4h7nmQ3G3";


}
