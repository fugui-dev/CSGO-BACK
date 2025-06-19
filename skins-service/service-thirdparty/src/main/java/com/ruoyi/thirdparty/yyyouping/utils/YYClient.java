package com.ruoyi.thirdparty.yyyouping.utils;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.thirdparty.yyyouping.utils.common.RSAUtils;
import com.ruoyi.thirdparty.yyyouping.utils.common.YYResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


@Slf4j
public class YYClient {

    private RSAUtils rsaUtils;

    private static String BaseUrl = "https://gw-openapi.youpin898.com";

    /**
     * 用户相关接口
     */
    private static String ApiGetAssetsInfo = "/open/v1/api/getAssetsInfo";
    private static String ApiCheckTradeUrl = "/open/v1/api/checkTradeUrl";
    private static String ApiDetailDataQueryAplly = "/open/v1/api/detailDataQueryAplly";
    private static String ApiDetailDataQueryResult = "/open/v1/api/detailDataQueryResult";

    /**
     * 市场查询接口
     */
    private static String ApiTemplateQuery = "/open/v1/api/templateQuery";
    private static String ApiGoodsQuery = "/open/v1/api/goodsQuery";
    private static String ApiBatchGetOnSaleCommodityInfo = "/open/v1/api/batchGetOnSaleCommodityInfo";
    private static String ApiQueryTemplateSaleByCategory = "/open/v1/api/queryTemplateSaleByCategory";
    private static String ApiQueryViewChart = "/open/v1/api/queryViewChart";

    /**
     * 购买接口
     */
    private static String ApiByTemplateCreateOrder = "/open/v1/api/byTemplateCreateOrder";
    private static String ApiByGoodsIdCreateOrder = "/open/v1/api/byGoodsIdCreateOrder";
    private static String ApiByTemplateAsyncCreateOrder = "/open/v1/api/byTemplateAsyncCreateOrder";
    private static String ApiByGoodsIdAsyncCreateOrder = "/open/v1/api/byGoodsIdAsyncCreateOrder";

    /**
     * 买家订单接口
     */
    private static String ApiOrderCancel = "/open/v1/api/orderCancel";
    private static String ApiOrderStatus = "/open/v1/api/orderStatus";
    private static String ApiOrderInfo = "/open/v1/api/orderInfo";

    /**
     * 出售接口
     */
    private static String ApiGetUserSteamInventoryData = "/open/v1/api/getUserSteamInventoryData";
    private static String ApiGetUserOnSaleCommodityData = "/open/v1/api/getUserOnSaleCommodityData";

    /**
     * 卖家订单接口
     */
    private static String ApiSellerQueryOrderList = "/open/v1/api/sellerQueryOrderList";
    private static String ApiSellerOrderStatus = "/open/v1/api/sellerOrderStatus";
    private static String ApiSellerOrderDetail = "/open/v1/api/sellerOrderDetail";


    public YYClient(String appKey,String publicKey,String privateKey){
        this.rsaUtils = new RSAUtils(appKey,publicKey,privateKey);
    }

    public String yyApiTemplateQuery(Map<String,Object> param) {
        Map<String, Object> p = null;
        try {
            p = createParam(param);
        }catch (Exception e){
            log.warn("YYClient 参数构造异常");
            return "";
        }

        return doPostToJson(BaseUrl+ApiTemplateQuery, JSON.toJSONString(p));
    }

    //ApiBatchGetOnSaleCommodityInfo
    public YYResult yyApiBatchGetOnSaleCommodityInfo(Map<String,Object> param) {

        try {
            Map<String, Object> p = createParam(param);

            String s = doPostToJson(BaseUrl + ApiBatchGetOnSaleCommodityInfo, JSON.toJSONString(p));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(s, YYResult.class);

        }catch (Exception e){
            log.warn("YYClient 参数构造异常");
            return YYResult.builder().code(005).msg("YYClient 参数构造异常").build();
        }
    }

    public String yyApiQueryTemplateSaleByCategory(Map<String,Object> param) {
        Map<String, Object> p = null;
        try {
            p = createParam(param);
        }catch (Exception e){
            log.warn("YYClient 参数构造异常");
            return "";
        }

        return doPostToJson(BaseUrl+ApiQueryTemplateSaleByCategory, JSON.toJSONString(p));
    }

    public String yyApiGetAssetsInfo(Map<String,Object> param) throws Exception {
        Map<String, Object> p = createParam(param);
        return doPostToJson(BaseUrl+ApiGetAssetsInfo, JSON.toJSONString(p));
    }

    public String yyApiGoodsQuery(Map<String,Object> param) {

        Map<String, Object> p = null;
        try {
            p = createParam(param);
        }catch (Exception e){
            log.warn("参数构建异常");
            return "";
        }

        return doPostToJson(BaseUrl+ApiGoodsQuery,JSON.toJSONString(p));
    }

    public String yyApiByGoodsIdCreateOrder(Map<String,Object> param) throws Exception {
        Map<String, Object> p = createParam(param);
        return doPostToJson(BaseUrl+ApiByGoodsIdCreateOrder,JSON.toJSONString(p));
    }

    public String yyApiOrderInfo(Map<String,Object> param) throws Exception {
        Map<String, Object> p = createParam(param);
        return doPostToJson(BaseUrl+ApiOrderInfo,JSON.toJSONString(p));
    }

    public String yyApiGetUserSteamInventoryData(Map<String,Object> param) throws Exception {
        Map<String, Object> p = createParam(param);
        return doPostToJson(BaseUrl+ApiGetUserSteamInventoryData,JSON.toJSONString(p));
    }

    private Map<String,Object> createParam(Map<String,Object> param) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        param.put("appKey",rsaUtils.getAppKey());
        param.put("timestamp",dateFormat.format(new Timestamp(System.currentTimeMillis())));
        param.put("sign",rsaUtils.getSign(param));
        return param;
    }

    // 基础post请求
    public static String doPostToJson(String urlPath, String Json) {

        String result = "";
        BufferedReader reader = null;
        HttpURLConnection conn = null;
        try {
            trustAllHosts();
            URL url = new URL(urlPath);
            if (url.getProtocol().toLowerCase().equals("https")) {
                HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                httpsConn.setHostnameVerifier(DO_NOT_VERIFY);
                conn = httpsConn;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // conn.setRequestProperty("accept","*/*");
            conn.setRequestProperty("accept", "application/json");
            if (Json != null) {
                byte[] writebytes = Json.getBytes();
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(Json.getBytes());
                outwritestream.flush();
                outwritestream.close();
            }

            if (conn.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                result = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String arg0, SSLSession arg1) {
            return true;
        }
    };

    public static void trustAllHosts() {
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

}
