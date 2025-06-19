package com.ruoyi.thirdparty.alipay.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(value = AliProperties.class)
public class AliCilent {

    @Bean
    @Qualifier("RealNameAuthentication")
    public AlipayClient RealNameAuthentication(AliProperties aliProperties) {
        List<MerchantConfig> merchants = aliProperties.getMerchants();
        if (merchants.isEmpty()) throw new IllegalArgumentException("No merchant configuration provided");
        MerchantConfig merchantConfig = merchants.get(1);
        return new DefaultAlipayClient(
                aliProperties.getServerUrl(),
                merchantConfig.getAppId(),
                merchantConfig.getPrivateKey(),
                aliProperties.getFormat(),
                aliProperties.getCharset(),
                merchantConfig.getAlipayPublicKey(),
                aliProperties.getSignType()
        );
    }
}
