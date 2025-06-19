package com.ruoyi.thirdparty.decsgopay.yspay.sdk;

import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

public class MD5Util {

    // 生成MD5签名
    public static String generateMD5Signature(Map<String, String> params, String key) throws Exception {
        // 1. 过滤并按参数名ASCII码排序
        TreeMap<String, String> sortedParams = new TreeMap<>(String::compareTo);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramKey = entry.getKey();
            String paramValue = entry.getValue();
            if (paramValue != null && !paramValue.isEmpty() && !"sign".equals(paramKey) && !"sign_type".equals(paramKey)) {
                sortedParams.put(paramKey, paramValue);
            }
        }

        // 2. 拼接成URL键值对格式
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // 去掉最后的 "&"
        }

        // 3. 拼接商户密钥并生成MD5签名
        String stringToSign = sb.toString() + key;
        return md5(stringToSign).toLowerCase(); // MD5加密后转小写
    }

    // MD5加密
    private static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes("UTF-8"));
        
        // 将字节数组转为16进制
        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
