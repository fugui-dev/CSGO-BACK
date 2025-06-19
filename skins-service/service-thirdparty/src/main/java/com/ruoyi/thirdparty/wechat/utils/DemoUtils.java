package com.ruoyi.thirdparty.wechat.utils;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class DemoUtils {
    public static String getRandom620(Integer length) {
        String result = "";
        Random rand = new Random();
        int n = 20;
        if (null != length && length > 0) {
            n = length;
        }
        int randInt = 0;
        for (int i = 0; i < n; i++) {
            randInt = rand.nextInt(10);

            result += randInt;
        }
        return result;
    }

    public static String sortMapByValues(Map<String, Object> map){
        String[] sortedKeys = map.keySet().toArray(new String[]{});
        Arrays.sort(sortedKeys);// 排序请求参数
        StringBuilder s2 = new StringBuilder();
        for (String key : sortedKeys) {
            //if str(v).strip() is not None and k != 'paytype' and k != 'remark' and k != 'bankcode' and k != 'sign' and k != 'userkey' and k != 'apiurl':
            if(!Objects.equals(key, "paytype") && !Objects.equals(key, "remark") && !Objects.equals(key, "bankcode") && !Objects.equals(key, "sign")){
                s2.append(key).append("=").append(map.get(key)).append("&");
            }
        }
        s2.deleteCharAt(s2.length() - 1);
        return String.valueOf(s2);
    }

    /**
     * RestTemplate发送POST请求之formData形式
     * @return
     */
    public static Map<String, String> postFormData(MultiValueMap<String, Object> map, String url){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        //头部类型
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //构造实体对象
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, headers);
        //发起请求,服务地址，请求参数，返回消息体的数据类型
        ResponseEntity<String> response = restTemplate.postForEntity(url, param, String.class);
        //body
        String body = response.getBody();
        if(body ==null){
            String location = Objects.requireNonNull(response.getHeaders().getLocation()).toASCIIString();
            Map<String,String> m = new HashMap<>();
            m.put("url",location);
            return m;
        }else{
            Map result = JSON.parseObject(body, Map.class);
            System.out.println("2"+result);
            return result;
        }
    }

    /**
     * MD5加密之方法一
     * @explain 借助apache工具类DigestUtils实现
     * @param str
     *            待加密字符串
     * @return 16进制加密字符串
     */
    public static String encryptToMD5(String str) {
        return DigestUtils.md5Hex(str);
    }



    // 把json格式的字符串写到文件
    public static boolean writeFile(String filePath, String sets) {
        FileWriter fw;
        try {
            fw = new FileWriter(filePath);
            PrintWriter out = new PrintWriter(fw);
            out.write(sets);
            out.println();
            fw.close();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
