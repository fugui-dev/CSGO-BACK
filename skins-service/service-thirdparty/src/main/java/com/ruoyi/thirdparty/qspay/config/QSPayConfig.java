package com.ruoyi.thirdparty.qspay.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class QSPayConfig {

    @Value("${qsconfig.apiUrl}")
    private String apiUrl = "https://abc.tianlicloud.com/";

    @Value("${qsconfig.merchantId}")
    private String merchantId = "1015";

    @Value("${qsconfig.apiKey}")
    private String apiKey = "DWX9uDKeGkZdE9QKyedyGWX3l7ET7u9t";

    @Value("${qsconfig.notifyUrl}")
    private String notifyUrl = "http://api.f99skins.com/prod-api/api/qs/notify";

}
