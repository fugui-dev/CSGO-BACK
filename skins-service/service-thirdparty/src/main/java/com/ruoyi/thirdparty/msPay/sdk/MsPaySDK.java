package com.ruoyi.thirdparty.msPay.sdk;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.thirdparty.msPay.config.MsPayConfig;
import com.ruoyi.thirdparty.msPay.sdk.util.OrderMain;
import com.ruoyi.thirdparty.msPay.sdk.util.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MsPaySDK {
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String MERCHANT_KEY = "l78ScetDKbOpcOfIONhgn6WMGFo5Q6ad";

    private static final String BASE_URL = "http://9ec2518d.zhaocai202403.xyz";

    private MsPayConfig payConfig;

    public MsPaySDK(MsPayConfig payConfig){
        this.payConfig = payConfig;
    }

//    public static void main(String[] args) throws Exception {

//        preOrder();

//        queryOrder("a430355ecda548a4933314a680ac7313");

//    }

    //查询订单
    public boolean queryOrder(String orderNo){
        try {
            String url = payConfig.getApiUrl() + "/index/getorder";
            Map<String, String> params = new HashMap<>();
            params.put("appid", payConfig.getAppId());
            params.put("out_trade_no", orderNo);

            // 生成签名
            String sign = generateSignature(params, payConfig.getApiKey());
            params.put("sign", sign);

            String response = sendPostRequest(url, params);
            log.info("查询订单是否支付响应==>【{}】", response);

            JSONObject jsonObject = JSONObject.parseObject(response);
            if (200 != (Integer) jsonObject.get("code")){
                return false;
            }
            if (JSONObject.parseObject(jsonObject.get("data").toString()).get("status").equals(4)){
                return true;
            }
        }catch (Exception e){

            return false;
        }

            return false;
    }



    //预订单
    public ResponseDTO preOrder(BigDecimal amount, String outUid, String orderNo){
        try {
            OrderMain orderMain = new OrderMain();
            String responseStr = orderMain.pushOrder(BigDecimal.valueOf(100).multiply(amount).longValue(), orderNo);
            log.info("下单订单号：【{}】" , orderNo);
            log.info("订单【{}】响应：【{}】" , orderNo, responseStr);

//            easypay_qrcode_pay_push_response
            JSONObject jsonObject = JSONObject.parseObject(responseStr);
            String response = jsonObject.get("easypay_qrcode_pay_push_response").toString();

            ResponseDTO rspDTO = JSONObject.parseObject(response, ResponseDTO.class);
            if ("00".equals(rspDTO.getCode())){
                return rspDTO;

            }else {
                return null;

            }


        } catch (Exception e) {
            log.error("下单异常==>", e);
            return null;
        }
    }


    public static String generateSignature(Map<String, String> params, String key) throws Exception {
        // 过滤空值并排序
        String paramString = params.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        StringBuilder sb = new StringBuilder();
        sb.append(paramString);

        // 拼接商户密钥
        sb.append("&key=").append(key);

        // MD5运算
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
        StringBuilder md5Str = new StringBuilder();
        for (byte b : array) {
            md5Str.append(String.format("%02x", b));
        }

        // 转换为大写
        return md5Str.toString().toUpperCase();
    }

    private static String sendPostRequest(String requestUrl, Map<String, String> params) throws Exception {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept-Language", "UTF-8");
        connection.setDoOutput(true);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(param.getKey());
            postData.append('=');
            postData.append(param.getValue());
        }

        byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

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
