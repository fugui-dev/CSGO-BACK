package com.ruoyi.thirdparty.yyyouping.config;

import com.ruoyi.thirdparty.yyyouping.utils.YYClient;
import com.ruoyi.thirdparty.yyyouping.utils.common.RSAUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "yy-youping")
public class YYConfig {

    private String appKey;
    private String publicKey;
    private String privateKey;

    public static String BaseUrl = "https://gw-openapi.youpin898.com";
    /**
     * 用户相关接口
     */
    public static final String ApiGetAssetsInfo = "/open/v1/api/getAssetsInfo";
    public static final String ApiCheckTradeUrl = "/open/v1/api/checkTradeUrl";
    public static final String ApiDetailDataQueryAplly = "/open/v1/api/detailDataQueryAplly";
    public static final String ApiDetailDataQueryResult = "/open/v1/api/detailDataQueryResult";

    /**
     * 市场查询接口
     */
    public static final String ApiTemplateQuery = "/open/v1/api/templateQuery";
    public static final String ApiGoodsQuery = "/open/v1/api/goodsQuery";
    public static final String ApiBatchGetOnSaleCommodityInfo = "/open/v1/api/batchGetOnSaleCommodityInfo";
    public static final String ApiQueryTemplateSaleByCategory = "/open/v1/api/queryTemplateSaleByCategory";
    public static final String ApiQueryViewChart = "/open/v1/api/queryViewChart";

    /**
     * 购买接口
     */
    public static final String ApiByTemplateCreateOrder = "/open/v1/api/byTemplateCreateOrder";
    public static final String ApiByGoodsIdCreateOrder = "/open/v1/api/byGoodsIdCreateOrder";
    public static final String ApiByTemplateAsyncCreateOrder = "/open/v1/api/byTemplateAsyncCreateOrder";
    public static final String ApiByGoodsIdAsyncCreateOrder = "/open/v1/api/byGoodsIdAsyncCreateOrder";

    /**
     * 买家订单接口
     */
    public static final String ApiOrderCancel = "/open/v1/api/orderCancel";
    public static final String ApiOrderStatus = "/open/v1/api/orderStatus";
    public static final String ApiOrderInfo = "/open/v1/api/orderInfo";

    /**
     * 出售接口
     */
    public static final String ApiGetUserSteamInventoryData = "/open/v1/api/getUserSteamInventoryData";
    public static final String ApiGetUserOnSaleCommodityData = "/open/v1/api/getUserOnSaleCommodityData";

    /**
     * 卖家订单接口
     */
    public static final String ApiSellerQueryOrderList = "/open/v1/api/sellerQueryOrderList";
    public static final String ApiSellerOrderStatus = "/open/v1/api/sellerOrderStatus";
    public static final String ApiSellerOrderDetail = "/open/v1/api/sellerOrderDetail";

    @Bean
    public RSAUtils yyClient(){
        return new RSAUtils(appKey,publicKey,privateKey);
    }

}
