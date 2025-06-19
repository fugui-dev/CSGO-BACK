package com.ruoyi.thirdparty.zyZFB.utils;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.security.MessageDigest;
import java.util.*;

public class signUtil {

    public static String getSign(Map<String, String> map, String key) {

        // 排序
        List<Map.Entry<String, String>> entryList = new ArrayList<>(map.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        String str = "";
        for (int i = 0; i < entryList.size(); i++) {
            if (i == entryList.size()-1) {
                str = str + entryList.get(i).getKey() + "=" + entryList.get(i).getValue();
                continue;
            }
            str = str + entryList.get(i).getKey() + "=" + entryList.get(i).getValue() + "&";
        }
        str = str + key;

        // MD5加密
        String sign = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            // 转换为MD5码
            byte[] digest = md5.digest(str.getBytes("utf-8"));
            sign = ByteUtils.toHexString(digest);
        } catch (Exception e) {
            System.out.println("md5加密 warn");
        }

        return sign;

    }

}
