package com.ruoyi.thirdparty.qspay.client;

import com.ruoyi.thirdparty.msPay.sdk.util.BaseHttpSSLSocketFactory;
import com.ruoyi.thirdparty.msPay.sdk.util.HttpConnectUtils;
import com.ruoyi.thirdparty.msPay.sdk.util.KeyUtils;
import com.ruoyi.thirdparty.qspay.config.QSPayConfig;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class QSPayClient {
    private QSPayConfig config;

    public QSPayClient(QSPayConfig config) {
        this.config = config;
    }

    public String sendPaymentRequest(Map<String, String> params) throws Exception {
        String apiUrl = config.getApiUrl() + "/mapi.php";
        return sendRequest(apiUrl, params);
    }

    public String queryOrder(Map<String, String> params) throws Exception {
        String apiUrl = config.getApiUrl() + "/api.php?act=order";
        return sendRequest(apiUrl, params);
    }

    public String refundOrder(Map<String, String> params) throws Exception {
        String apiUrl = config.getApiUrl() + "/api.php?act=refund";
        return sendRequest(apiUrl, params);
    }

    private String sendRequest(String apiUrl, Map<String, String> params) throws Exception {
        params.put("pid", config.getMerchantId());
        params.put("key", config.getApiKey());

        String sign = generateSign(params);
        params.put("sign", sign);

        String paramString = params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));

        byte[] postDataBytes = paramString.getBytes(StandardCharsets.UTF_8);

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

        if ("https".equalsIgnoreCase(url.getProtocol())) {
            HttpsURLConnection connection = (HttpsURLConnection) conn;
//            if (context != null) {
//                connection.setSSLSocketFactory(context);
//            } else {
                connection.setSSLSocketFactory(new com.ruoyi.thirdparty.msPay.sdk.util.BaseHttpSSLSocketFactory());
//            }
            //解决由于服务器证书问题导致HTTPS无法访问的情况
            connection.setHostnameVerifier(new BaseHttpSSLSocketFactory.TrustAnyHostnameVerifier());
//            return connection;
        }

        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            return in.lines().collect(Collectors.joining());
        }

//        StringBuilder resultStrBuilder = new StringBuilder();
//        int ret = HttpConnectUtils.sendRequest(apiUrl,
//                "application/x-www-form-urlencoded",
//                paramString,
//                30000,
//                60000,
//                "POST",
//                resultStrBuilder,
//                null);
//        log.info(" \n请求地址为：" + apiUrl +
//                "\n 请求结果为：" + ret +
//                "\n 请求参数为：" + paramString +
//                "\n 返回内容为：" + resultStrBuilder + "\n");
//
//        return resultStrBuilder.toString();

    }

    public String generateSign(Map<String, String> params) throws Exception {
        String paramString = params.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("sign") && !entry.getKey().equals("sign_type") && !entry.getValue().isEmpty())
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));

        String stringToSign = paramString + config.getApiKey();
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(stringToSign.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
