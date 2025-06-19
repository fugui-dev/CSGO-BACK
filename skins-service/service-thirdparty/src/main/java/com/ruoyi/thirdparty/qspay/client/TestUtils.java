package com.ruoyi.thirdparty.qspay.client;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.thirdparty.qspay.config.QSPayConfig;

import java.util.HashMap;
import java.util.UUID;

public class TestUtils {

    public static void main(String[] args) throws Exception {
        QSPayClient payClient = new QSPayClient(new QSPayConfig());

        HashMap<String, String> map = new HashMap<>();
        map.put("out_trade_no", UUID.randomUUID().toString());
        map.put("notify_url", "http://www.baidu.com");
        map.put("type", "alipay");
        map.put("name", "虚拟商品购买");
        map.put("money", "0.01");
        map.put("clientip", "192.168.1.100");
        map.put("sign_type", "MD5");


        String result = payClient.sendPaymentRequest(map);
        JSONObject jsonObject = JSONObject.parseObject(result);
        System.out.println(jsonObject);
    }

}
