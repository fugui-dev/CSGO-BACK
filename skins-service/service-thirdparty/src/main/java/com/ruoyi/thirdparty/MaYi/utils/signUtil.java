package com.ruoyi.thirdparty.MaYi.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

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
            // if (i == entryList.size()-1) {
            //     str = str + entryList.get(i).getKey() + "=" + entryList.get(i).getValue();
            //     continue;
            // }
            str = str + entryList.get(i).getKey() + "=" + entryList.get(i).getValue() + "&";
        }
        str = str + "key=" +key;

        // MD5加密
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        return md5.digestHex(str).toUpperCase();

    }

    public static String getSignOfYima(Map<String, String> params, List<String> ignoreKeys, String secretKey) {
        Set<String> keySet = params.keySet();
        List<String> collect = keySet.stream().filter(key -> ignoreKeys == null || !ignoreKeys.contains(key))
                .sorted()
                .collect(Collectors.toList());

        // 拼接签名字段
        StringBuilder builder = new StringBuilder();
        for (String signKey : collect) {
            builder.append(signKey).append("=").append(params.get(signKey)).append("&");
        }

        builder.append("key=").append(secretKey);
        String signStr = builder.toString();
        // 平台生成MD5签名的编码使用的是UTF-8注意我们加密的编码。当结果存在中文字符时不同的编码最终生成的结果是不一样的
        String ret = DigestUtils.md5DigestAsHex(signStr.getBytes(StandardCharsets.UTF_8)).toUpperCase();
        return ret;
    }


}
