package com.ruoyi.thirdparty.yyyouping.service;

import com.ruoyi.thirdparty.yyyouping.config.YYConfig;
import com.ruoyi.thirdparty.yyyouping.utils.common.YYResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Map;

public interface yyyoupingService {

    YYResult yyApiBatchGetOnSaleCommodityInfo(Map<String, Object> param);
    YYResult yyApiTemplateQuery(Map<String, Object> param);
    YYResult yyApiQueryTemplateSaleByCategory(Map<String, Object> param);
    YYResult yyApiGetAssetsInfo();
    YYResult yyApiGoodsQuery(Map<String, Object> param);
    YYResult yyApiByGoodsIdCreateOrder(Map<String, Object> param);
    YYResult yyApiOrderInfo(Map<String, Object> param);
    YYResult yyApiGetUserSteamInventoryData(Map<String, Object> param);

}
