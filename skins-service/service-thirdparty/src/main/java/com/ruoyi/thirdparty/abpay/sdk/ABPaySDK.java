package com.ruoyi.thirdparty.abpay.sdk;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.thirdparty.abpay.config.ABPayConfig;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class ABPaySDK {
    private static final String USER_AGENT = "Mozilla/5.0";

    private ABPayConfig payConfig;

    public ABPaySDK(ABPayConfig payConfig){
        this.payConfig = payConfig;
    }


    //查询订单
    public boolean queryOrder(String orderNo) {
        try {
            String url = payConfig.getApiUrl() + "/bus/noauth/openapi/amount/query";
            JSONObject params = new JSONObject();
            params.put("busRecordId", orderNo);
            params.put("appKey", payConfig.getAppKey());
            params.put("busTransType", 4);
            params.put("type", 1);
            params.put("nonce", "123456");
            params.put("timestamp", System.currentTimeMillis());

            // 生成签名
            String sign = generateSignature(params, payConfig.getGeneralQueryKey());
            params.put("signature", sign);

            disableSSLVerification(); //禁用https
            String response = sendPostRequest(url, params.toJSONString());

            log.info("查询订单【{}】响应：【{}】" , orderNo, response);
            JSONObject jsonObject = JSONObject.parseObject(response);
            if (!"200".equals(jsonObject.get("code"))){
                return false;
            }

            //响应成功，并且status=2时支付成功
            if (JSONObject.parseObject(jsonObject.get("data").toString()).get("status").equals(2)){
                return true;
            }

        }catch (Exception e){
            log.error("查询AB订单【{}】出现异常==>", orderNo, e);
            return false;

        }
        return false;
    }


    //预订单
    //响应内容{"success":true,"code":"200","msg":"success","retry":false,"time":"1719313850231","data":"https://ch5.iq8477.xyz/bus/openapi/amount/in/auto/pre?sign=5b3ffe2a73f5455b95b838765c34aa44","busRecordId":"799b5d8fb4094a37b5a3af0c1280b705","errorStack":null,"args":null}
    public JSONObject preOrder(String orderNo, BigDecimal totalAmount, TtUser user){
        try {
            String url = payConfig.getApiUrl() + "/bus/noauth/openapi/amount/in";
            JSONObject params = new JSONObject();
            params.put("appKey", payConfig.getAppKey());
            params.put("nonce", "123456");
            params.put("busTransType", 1);
            params.put("notifyUrl", payConfig.getNotifyUrl());
            params.put("timestamp", System.currentTimeMillis());
            params.put("busRecordId", orderNo);
            params.put("amount", totalAmount);
            params.put("busCreateTime", System.currentTimeMillis());
            params.put("userName",  user.getPhoneNumber());
            params.put("realName",  user.getRealName());

            // 生成签名
            String sign = generateSignature(params, payConfig.getPrivateKey());
            params.put("signature", sign);

            disableSSLVerification(); //禁用https
            String response = sendPostRequest(url, params.toJSONString());

            log.info("下单订单号：【{}】" , orderNo);
            log.info("订单【{}】响应：【{}】" , orderNo, response);

            JSONObject jsonObject = JSONObject.parseObject(response);

            if (!"200".equals(jsonObject.get("code")) || !(Boolean) jsonObject.get("success")){
                return null;
            }

            return jsonObject;

        } catch (Exception e) {
            log.error("下单异常==>", e);
            return null;

        }
    }

    private static void disableSSLVerification() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }


    public static String generateSignature(Map<String, Object> params, String key) throws Exception {
        // 过滤空值并排序
        String paramString = params.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() +  entry.getValue())
                .collect(Collectors.joining());

        // MD5运算
        paramString = paramString + key;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(paramString.getBytes(StandardCharsets.UTF_8));
        StringBuilder md5Str = new StringBuilder();
        for (byte b : array) {
            md5Str.append(String.format("%02x", b));
        }

        // 转换为小写
        return md5Str.toString().toLowerCase();
    }

    private static String sendPostRequest(String requestUrl, String jsonRequest) throws Exception {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept-Language", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        byte[] postDataBytes = jsonRequest.getBytes(StandardCharsets.UTF_8);

        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write(postDataBytes);
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            return response.toString();
        }
    }
}
