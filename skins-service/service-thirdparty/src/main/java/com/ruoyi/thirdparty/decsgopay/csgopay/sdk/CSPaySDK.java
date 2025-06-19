package com.ruoyi.thirdparty.decsgopay.csgopay.sdk;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.thirdparty.decsgopay.csgopay.config.CSPayConfig;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Slf4j
public class CSPaySDK {

    private CSPayConfig payConfig;

    public CSPaySDK(CSPayConfig payConfig){
        this.payConfig = payConfig;
    }


    /**
     * 下单接口
     */
    public JSONObject preOrder(String payType, String orderNo, String goodsName, String money) {
        try {
            trustAllHosts();

            // 请求地址
            String requestUrl = payConfig.getApiUrl() + "/mapi.php";
            
            // 表单数据
            Map<String, String> parameters = new HashMap<>();
            parameters.put("pid", payConfig.getPid());
            parameters.put("type", payType);
            parameters.put("out_trade_no", orderNo);
            parameters.put("notify_url", payConfig.getNotifyUrl());
            parameters.put("return_url", payConfig.getReturnUrl());
            parameters.put("name", goodsName);
            parameters.put("money", money);
            parameters.put("clientip", "192.168.1.100");
            parameters.put("param", "");

            // 签名
            String sign = MD5Util.generateMD5Signature(parameters, payConfig.getMd5key());
            parameters.put("sign", sign);
            parameters.put("sign_type", "MD5");

            // 将参数转换为URL编码的字符串
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                sj.add(entry.getKey() + "=" + entry.getValue());
            }
            byte[] postDataBytes = sj.toString().getBytes(StandardCharsets.UTF_8);

            // 创建URL对象
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);

            // 发送POST请求
            try (OutputStream os = conn.getOutputStream()) {
                os.write(postDataBytes);
            }

            // 获取响应代码
            int responseCode = conn.getResponseCode();
           log.info("Response Code: " + responseCode);

            // 读取响应内容
            StringBuilder strBuilder = new StringBuilder();
            try (InputStream is = conn.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    strBuilder.append(line);
                }
            }

            String responseStr = strBuilder.toString();
            log.info("订单【{}】下单响应：==>【{}】", orderNo, responseStr);
            JSONObject jsonObject = JSONObject.parseObject(responseStr);
            boolean success = jsonObject.get("code").equals(1);

            //如果code不为1直接返回null
            return success ? jsonObject : null;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

            }

            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

            }
        }

        };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    /**
     * 查询订单
     * @param orderNo
     * @return
     */
    public boolean queryOrder(String orderNo, String outTradeNo) {

        String getUrl = payConfig.getApiUrl() + "/api.php?act=order" + "&pid=" + payConfig.getPid() + "&key=" + payConfig.getMd5key() + "&trade_no=" + outTradeNo + "&order_no=" + orderNo;
        String rspStr = HttpUtils.sendGet(getUrl);
        log.info("查询订单【{},{}】状态响应==>【{}】", orderNo, outTradeNo, rspStr);

        JSONObject jsonObject = JSONObject.parseObject(rspStr);
        boolean code = jsonObject.get("code").toString().equals("1");
        if (!code) return false;

        boolean status = jsonObject.get("status").toString().equals("1");
        if (status) return true;

        return false;
    }
}