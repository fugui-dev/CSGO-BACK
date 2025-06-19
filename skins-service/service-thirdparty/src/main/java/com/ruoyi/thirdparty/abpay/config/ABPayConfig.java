package com.ruoyi.thirdparty.abpay.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ABPayConfig {

    @Value("${abconfig.apiUrl}")
    private String apiUrl = "https://h5gw01.slpayuat.com";

    @Value("${abconfig.appKey}")
    private String appKey = "8d5f5e0174104d9785d40ec845f19faa";

    @Value("${abconfig.notifyUrl}")
    private String notifyUrl = "http://api.f99skins.com/prod-api/api/abPay/notify";

    @Value("${abconfig.privateKey}")
    private String privateKey = "4o6rEp1N8EEgzoUr5AhJSJJLVca18eDquy91QvE43U1E";

    @Value("${abconfig.generalQueryKey}")
    private String generalQueryKey = "4Ss5hZjQ6wjE5XTb1NLg1sENAkeZJUtZVdc7UesvzuS8";

}
