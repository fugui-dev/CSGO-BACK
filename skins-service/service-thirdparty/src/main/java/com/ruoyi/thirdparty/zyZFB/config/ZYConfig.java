package com.ruoyi.thirdparty.zyZFB.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "zy-zfb")
public class ZYConfig {

    private String merKey;
    private String gateway;
    private String tranType;
    private String merchantId;
    private String notifyBaseUrl;

    // 下单接口
    public static final String ApiAddTrans = "/ctp/view/server/aotori/addTrans";

    // 查询订单接口
    public static final String ApiQueryTrans = "/ctp/view/server/aotori/queryTrans";

    // 代付接口
    public static final String ApiPropayTrans = "/ctp/view/server/aotori/propayTrans";

    // 查询余额接口
    public static final String ApiQueryBalancePHP = "/ctp/view/server/aotori/querybalance.php";

}
