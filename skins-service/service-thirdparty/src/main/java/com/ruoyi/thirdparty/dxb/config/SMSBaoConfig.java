package com.ruoyi.thirdparty.dxb.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SMSBaoConfig {
    @Value("${dxb.enable}")
    private Boolean enable;

    @Value("${dxb.username}")
    private String username;

    @Value("${dxb.password}")
    private String password;

}
