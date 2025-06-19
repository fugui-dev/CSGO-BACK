package com.ruoyi.thirdparty.xinghuopay.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

public class XinghuoPayUtil {

    private String key;

    public XinghuoPayUtil(String key) {
        this.key = key;
    }

    /**
     * 生成MD5签名
     *
     * @param params 参数列表
     * @return 生成的签名
     */
    public String getSign(Map<String, String> params) {
        // 排序参数
        Map<String, String> sortedParams = new TreeMap<>(params);
        sortedParams.remove("sign");
        sortedParams.remove("sign_type");

        // 构建签名字符串
        StringBuilder signStr = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                signStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }

        if (signStr.length() > 0) {
            signStr.setLength(signStr.length() - 1);  // 移除最后一个&
        }

        // 添加商户密钥
        signStr.append(this.key);

        // 计算MD5签名
        return md5(signStr.toString()).toLowerCase();
    }

    /**
     * 计算MD5哈希值
     *
     * @param input 输入字符串
     * @return MD5哈希值
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            return bytesToHex(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
