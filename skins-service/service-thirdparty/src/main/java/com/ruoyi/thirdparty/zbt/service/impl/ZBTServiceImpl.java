package com.ruoyi.thirdparty.zbt.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.thirdparty.zbt.config.ZBTProperties;
import com.ruoyi.thirdparty.zbt.param.*;
import com.ruoyi.thirdparty.zbt.result.ResultZbt;
import com.ruoyi.thirdparty.zbt.result.buy.OpenBuyResultDTO;
import com.ruoyi.thirdparty.zbt.result.order.OrderBuyDetailDTO;
import com.ruoyi.thirdparty.zbt.result.product.AvailableMarketList;
import com.ruoyi.thirdparty.zbt.result.product.OrnPriceInfo;
import com.ruoyi.thirdparty.zbt.result.product.ProductFilters;
import com.ruoyi.thirdparty.zbt.result.product.SearchData;
import com.ruoyi.thirdparty.zbt.result.user.BusinessData;
import com.ruoyi.thirdparty.zbt.result.user.GenericCheckSteamOutPut;
import com.ruoyi.thirdparty.zbt.result.user.UserAccountModel;
import com.ruoyi.thirdparty.zbt.service.ZBTService;
import com.ruoyi.thirdparty.zbt.util.ZBTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@EnableConfigurationProperties(value = ZBTProperties.class)
public class ZBTServiceImpl implements ZBTService {

    private final ZBTProperties zbtProperties;

    public ZBTServiceImpl(ZBTProperties zbtProperties) {
        this.zbtProperties = zbtProperties;
    }

    @Override
    public ResultZbt<UserAccountModel> balance() {
        String result = getMethod(zbtProperties.getUserBalance(), null);
        if (StringUtils.isBlank(result)) return null;
        return JSONObject.parseObject(result, new TypeReference<ResultZbt<UserAccountModel>>() {
        });
    }

    @Override
    public ResultZbt<GenericCheckSteamOutPut> userSteamInfo(UserSteamInfoParams params) {
        try {
            params.setTradeUrl(URLEncoder.encode(params.getTradeUrl(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        Map<String, String> paramMap = ZBTUtils.objectToMap(params);
        String result = getMethod(zbtProperties.getUserSteamInfo(), paramMap);
        if (StringUtils.isBlank(result)) return null;
        return JSONObject.parseObject(result, new TypeReference<ResultZbt<GenericCheckSteamOutPut>>() {
        });
    }

    @Override
    public ResultZbt<CreateCheckSteam> createSteamCheck(CreateCheckSteam params) {

        String result = postMethod(zbtProperties.getUserCreate(), JSONObject.toJSONString(params));

        log.info("zbt创建用户响应==>【{}】", result);

        if (StringUtils.isBlank(result)) {
            return ResultZbt.faild();
        }
        ResultZbt<CreateCheckSteam> resultZbt = JSONObject.parseObject(result, ResultZbt.class);
        return resultZbt;
    }

    @Override
    public ResultZbt<BusinessData> developmentInfo() {
        String result = getMethod(zbtProperties.getDevelopmentInfo(), null);
        if (StringUtils.isBlank(result)) return null;
        return JSONObject.parseObject(result, new TypeReference<ResultZbt<BusinessData>>() {
        });
    }

    @Override
    public ResultZbt<SearchData> search(SearchParams params) {
        Map<String, String> paramMap = ZBTUtils.objectToMap(params);
        String result = getMethod(zbtProperties.getProductSearch(), paramMap);
        if (StringUtils.isBlank(result)) return null;
        return JSONObject.parseObject(result, new TypeReference<ResultZbt<SearchData>>() {
        });
    }

    @Override
    public ResultZbt<ProductFilters> productFilters() {
        String result = getMethod(zbtProperties.getProductFilters(), null);
        if (StringUtils.isBlank(result)) return null;
        return JSONObject.parseObject(result, new TypeReference<ResultZbt<ProductFilters>>() {
        });
    }

    @Override
    public ResultZbt<AvailableMarketList> productList(ProductListParams params) {
        Map<String, String> paramMap = ZBTUtils.objectToMap(params);
        String result = getMethod(zbtProperties.getProductList(), paramMap);
        if (StringUtils.isBlank(result)) {
            return null;
        }
        return JSONObject.parseObject(result, new TypeReference<ResultZbt<AvailableMarketList>>() {
        });
    }

    @Override
    public ResultZbt<OpenBuyResultDTO> tradeBuy(NormalBuyParamV2DTO normalBuyParamV2DTO) {
        String result = postMethod(zbtProperties.getTradeBuy(), JSON.toJSONString(normalBuyParamV2DTO));
        if (StringUtils.isBlank(result)) {
            return null;
        }
        return JSONObject.parseObject(result, new TypeReference<ResultZbt<OpenBuyResultDTO>>() {
        });
    }

    @Override
    public ResultZbt<OpenBuyResultDTO> tradeQuickBuy(QuickBuyParamV2DTO quickBuyParamV2DTO) {
        String result = postMethod(zbtProperties.getTradeQuickBuy(), JSON.toJSONString(quickBuyParamV2DTO));
        if (StringUtils.isBlank(result)) return null;
        return JSONObject.parseObject(result, new TypeReference<ResultZbt<OpenBuyResultDTO>>() {
        });
    }

    @Override
    public ResultZbt<OrderBuyDetailDTO> orderBuyDetail(OrderBuyDetailParams params) {
        Map<String, String> paramMap = ZBTUtils.objectToMap(params);
        String result = getMethod(zbtProperties.getOrderBuyDetail(), paramMap);
        if (StringUtils.isBlank(result)) return null;
        return JSONObject.parseObject(result, new TypeReference<ResultZbt<OrderBuyDetailDTO>>() {
        });
    }

    @Override
    public ResultZbt<List<OrnPriceInfo>> queryPriceBatch(SearchPriceBatchParams searchPriceBatchParams) {
//        Map<String, String> paramMap = ZBTUtils.objectToMap(searchPriceBatchParams);
        String result = postMethod(zbtProperties.getProductInfo(), JSONObject.toJSONString(searchPriceBatchParams));
        if (StringUtils.isBlank(result)) return null;

        JSONObject jsonObject = JSONObject.parseObject(result);

        Boolean success = (Boolean) jsonObject.get("success");
        if (!success){
            return ResultZbt.faild();
        }

        JSONArray jsonArray = jsonObject.getJSONArray("data");
        List<OrnPriceInfo> list = jsonArray.toJavaList(OrnPriceInfo.class);

        return new ResultZbt(list, null, null, null, true);
    }

    private String postMethod(String url, String JSONString) {
        Map<String, String> param = new HashMap<>();
        param.put("app-key", zbtProperties.getAppKey());
        param.put("language", zbtProperties.getLanguage());
        String substring = GeneratedStr(param);
        url = zbtProperties.getBaseUrl() + url + "?" + substring;
        return HttpUtils.sendPostJSONString(url, JSONString);
    }

    private String getMethod(String url, Map<String, String> param) {

        if (param == null) {
            param = new HashMap<>();
        }
        param.put("appId", zbtProperties.getAppId());
        param.put("app-key", zbtProperties.getAppKey());
        param.put("language", zbtProperties.getLanguage());
        String substring = GeneratedStr(param);
        return HttpUtils.sendGet(zbtProperties.getBaseUrl() + url, substring);
    }

    private String GeneratedStr(Map<String, String> param){
        StringBuilder params = new StringBuilder();
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            params.append(key);
            params.append("=");
            params.append(value);
            params.append("&");
        }
        params.trimToSize();
        return params.substring(0, params.length() - 1);
    }
}
