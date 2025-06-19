package com.ruoyi.thirdparty.realname;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@Slf4j
public class RealNameSdk {

    private String url;

    private String appCode;


    public RealNameSdk(RealName2Config config){
        this.url = config.getUrl();
        this.appCode = config.getAppCode();
    }

    public boolean sendApi(String idCard, String name){

        String url = this.url;
        String appCode = this.appCode;

        Map<String, String> params = new HashMap<>(2);
        params.put("idcard", idCard);
        params.put("name", name);

        String result = null;
        try {

            log.info("发起实名api请求==>【{}】", params);
            result = get(appCode, url, params);
            log.info("实名api响应==>【{}】", result);
            if ((Integer) JSONObject.parseObject(JSONObject.parseObject(result).get("data").toString()).get("result") == 0){
                return true;
            }
        } catch (Exception e) {
            log.error("请求实名api出现异常==>", e);

        }

        return false;
    }

    /**
     * 用到的HTTP工具包：okhttp 3.13.1
     * <dependency>
     * <groupId>com.squareup.okhttp3</groupId>
     * <artifactId>okhttp</artifactId>
     * <version>3.13.1</version>
     * </dependency>
     */
    public static String get(String appCode, String url, Map<String, String> params) throws IOException {
        url = url + buildRequestUrl(params);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).addHeader("Authorization", "APPCODE " + appCode).build();
        Response response = client.newCall(request).execute();
        System.out.println("返回状态码" + response.code() + ",message:" + response.message());
        String result = response.body().string();
        return result;
    }

    public static String buildRequestUrl(Map<String, String> params) {
        StringBuilder url = new StringBuilder("?");
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            url.append(key).append("=").append(params.get(key)).append("&");
        }
        return url.toString().substring(0, url.length() - 1);
    }
}