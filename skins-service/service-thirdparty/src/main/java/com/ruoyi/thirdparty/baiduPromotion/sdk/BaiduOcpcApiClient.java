package com.ruoyi.thirdparty.baiduPromotion.sdk;

import com.alibaba.fastjson2.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BaiduOcpcApiClient {

    private static final String API_URL = "https://ocpc.baidu.com/ocpcapi/api/uploadConvertData";
    private String token;

    private List<JSONObject> conversionTypes;

    public BaiduOcpcApiClient(String token, List conversionTypes) {
        this.token = token;
        this.conversionTypes = conversionTypes;
    }

    /**
     * 上传转换数据
     *
     * @return API响应
     * @throws IOException 如果发生IO错误
     */
    public JSONObject uploadInfoApi() throws IOException {

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("token", token);
        jsonObj.put("conversionTypes", conversionTypes);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.setHeader("Content-Type", "application/json");
//            httpPost.setHeader("token", this.token);

            StringEntity entity = new StringEntity(jsonObj.toJSONString(), "UTF-8");
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return JSONObject.parseObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        }
    }

//    public static void main(String[] args) {
//
//
//        String token = "BghFzkT3Me5CkuKQ81ToHKQmzpTFYvgL@hbioKT2MpM0IclVej0iqEZTapRvEbZnH"; // 请替换为实际的访问令牌
//
//        JSONObject conversionItem = new JSONObject();
//        conversionItem.put("logidUrl", "https://j8csgo.cz.jsaqwlo.cn");
//        conversionItem.put("newType", 10);
//        ArrayList<JSONObject> conversionTypes = new ArrayList<>(1);
//        conversionTypes.add(conversionItem);
//
//        //请求api
//        BaiduOcpcApiClient client = new BaiduOcpcApiClient(token, conversionTypes);
//
//        try {
//            JSONObject response = client.uploadInfoApi();
//            System.out.println("API Response: " + response);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
