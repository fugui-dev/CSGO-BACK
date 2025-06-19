package com.ruoyi.thirdparty.yyyouping.service.Impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.thirdparty.yyyouping.config.YYConfig;
import com.ruoyi.thirdparty.yyyouping.service.yyyoupingService;
import com.ruoyi.thirdparty.yyyouping.utils.common.RSAUtils;
import com.ruoyi.thirdparty.yyyouping.utils.common.YYResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.ruoyi.thirdparty.yyyouping.config.YYConfig.*;


@Slf4j
@Service
public class yyyoupingServiceImpl implements yyyoupingService {

    @Autowired
    private YYConfig yyConfig;

    @Autowired
    private RSAUtils rsaUtils;

    // 添加基本请求参数
    private Map<String, Object> createParam(Map<String, Object> param) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            param.put("appKey", yyConfig.getAppKey());
            param.put("timestamp", dateFormat.format(new Timestamp(System.currentTimeMillis())));
            param.put("sign", rsaUtils.getSign(param));
        }catch (Exception e){
            log.warn("参数构造异常");
        }
        return param;
    }


    public YYResult yyApiBatchGetOnSaleCommodityInfo(Map<String, Object> param) {

        Map<String, Object> p = createParam(param);
        HttpRequest post = HttpUtil.createPost(BaseUrl + ApiBatchGetOnSaleCommodityInfo);
        post.body(JSONUtil.toJsonStr(p));
        HttpResponse res = post.execute();
        String body = res.body();

        return JSONUtil.toBean(body, YYResult.class);

    }

    // 通过此接口可获取悠悠有品所有商品对应的模板ID的下载链接。
    public YYResult yyApiTemplateQuery(Map<String, Object> param) {

        try {

            Map<String, Object> p = createParam(param);

            HttpRequest post = HttpUtil.createPost(BaseUrl + ApiTemplateQuery);
            post.body(JSON.toJSONString(p), "json");
            String body = post.execute().body();

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(body, YYResult.class);

        } catch (Exception e) {
            log.warn("yyApiTemplateQuery");
            return YYResult.builder().code(005).msg("yyApiTemplateQuery").build();
        }
    }


    public YYResult yyApiQueryTemplateSaleByCategory(Map<String, Object> param) {

        Map<String, Object> p = createParam(param);
        String res = HttpUtil.post(BaseUrl + ApiQueryTemplateSaleByCategory, p);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(res, YYResult.class);
        }catch (Exception e){
            log.warn("响应结果解析异常");
            return YYResult.builder().code(005).msg("响应结果解析异常").build();
        }
    }

    public YYResult yyApiGetAssetsInfo() {

        try {

            Map<String, Object> p = createParam(new HashMap<String,Object>());

            HttpRequest post = HttpUtil.createPost(BaseUrl + ApiGetAssetsInfo);
            post.body(JSON.toJSONString(p),"json");
            String body = post.execute().body();

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(body, YYResult.class);

        } catch (Exception e) {
            log.warn("yyApiGetAssetsInfo");
            return YYResult.builder().code(005).msg("yyApiGetAssetsInfo").build();
        }
    }

    public YYResult yyApiGoodsQuery(Map<String, Object> param) {

        try {

            Map<String, Object> p = createParam(param);
            String res = HttpUtil.post(BaseUrl + ApiGoodsQuery, p);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(res, YYResult.class);

        } catch (Exception e) {
            log.warn("参数构造异常");
            return YYResult.builder().code(005).msg("参数构造异常").build();
        }

    }

    public YYResult yyApiByGoodsIdCreateOrder(Map<String, Object> param) {
        try {

            Map<String, Object> p = createParam(param);
            String res = HttpUtil.post(BaseUrl + ApiByGoodsIdCreateOrder, p);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(res, YYResult.class);

        } catch (Exception e) {
            log.warn("参数构造异常");
            return YYResult.builder().code(005).msg("参数构造异常").build();
        }
    }

    public YYResult yyApiOrderInfo(Map<String, Object> param) {
        try {

            Map<String, Object> p = createParam(param);
            String res = HttpUtil.post(BaseUrl + ApiOrderInfo, p);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(res, YYResult.class);

        } catch (Exception e) {
            log.warn("参数构造异常");
            return YYResult.builder().code(005).msg("参数构造异常").build();
        }
    }

    public YYResult yyApiGetUserSteamInventoryData(Map<String, Object> param) {
        try {

            Map<String, Object> p = createParam(param);
            String res = HttpUtil.post(BaseUrl + ApiGetUserSteamInventoryData, p);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(res, YYResult.class);

        } catch (Exception e) {
            log.warn("参数构造异常");
            return YYResult.builder().code(005).msg("参数构造异常").build();
        }
    }


}
