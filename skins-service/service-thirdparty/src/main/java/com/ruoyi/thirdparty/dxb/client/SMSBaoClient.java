package com.ruoyi.thirdparty.dxb.client;

import com.ruoyi.thirdparty.dxb.config.SMSBaoConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SMSBaoClient {
    private SMSBaoConfig config;

    public SMSBaoClient(SMSBaoConfig config) {
        this.config = config;
    }

    public String sendSMSVerCode(String phone, String code){
        try {
            sendSMS(phone, "【永恒科技】您的验证码是{"+ code + "}，5分钟内有效。如非本人操作，请忽略本短信。");
        }catch (Exception e){
            return "";
        }
        return code;
    }

    public String sendSMS(String phone, String message) throws Exception {
        String apiUrl = "http://api.smsbao.com/sms";
        String encodedMessage = java.net.URLEncoder.encode(message, StandardCharsets.UTF_8.toString());

        String hashedPassword = md5(config.getPassword());

        String urlParameters = "u=" + config.getUsername() + "&p=" + hashedPassword + "&m=" + phone + "&c=" + encodedMessage;

        URL url = new URL(apiUrl + "?" + urlParameters);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            return response.toString();
        }
    }

    private String md5(String plainText) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
