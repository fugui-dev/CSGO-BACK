package com.ruoyi.thirdparty.msPay.sdk.util;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.thirdparty.msPay.sdk.util.dto.ResponseDTO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//二维码，公众号测试demo
//包含退款和订单查询
public class OrderMain {

//    public static void main(String[] args) {
//        OrderMain orderMain = new OrderMain();
//        String responseStr = orderMain.pushOrder(100L, orderNo);
//        ResponseDTO rspDTO = JSONObject.parseObject(responseStr, ResponseDTO.class);
//        if ("00".equals(rspDTO.getCode())){
//
//
//        }else {
//
//        }
//    }


    //标记生产还是测试环境
    public static boolean isTest = false;

    //根据接口文档生成对应的json请求字符串
    private String biz_content = "";

    //接口文档中的方法名
    private String service = "trade.auth.preauth";

    //商户号
    private String merchant_id = KeyUtils.TEST_DEFAULT_MERCHANT_ID;

    //接入机构号
    private String partner = KeyUtils.TEST_DEFAULT_PARTNER;

    //请求地址
    private static String url = KeyUtils.DEFAULT_URL;

    //key密钥
    private static String key = KeyUtils.TEST_MERCHANT_PRIVATE_KEY;

    //加密密钥
    private static String DES_ENCODE_KEY = KeyUtils.TEST_DES_ENCODE_KEY;

    //二维码下单
    public void qrcodePayPush(String payType, Long money, String orderNo){
        JSONObject sParaTemp = qrcodeAndJsPayPush(payType, money, orderNo);
//        sParaTemp.put("pay_acc_type", "00");

        biz_content = sParaTemp.toString();

        service  = "easypay.qrcode.pay.push";
    }


    public JSONObject qrcodeAndJsPayPush(String payType, Long money, String orderNo) {
        JSONObject sParaTemp = new JSONObject();
        sParaTemp.put("merchant_id", merchant_id);
//        sParaTemp.put("seller_email", "18679106330@gmail.com");
        sParaTemp.put("amount", money);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowStr = dateFormat.format(new Date());
        sParaTemp.put("business_time", nowStr);
        sParaTemp.put("notify_url", KeyUtils.SC_DEFAULT_NOTIFY);
//        sParaTemp.put("order_desc", "Echannell");
//        sParaTemp.put("subject", "Echannell");
        sParaTemp.put("pay_type", payType);

//        sParaTemp.put("out_trade_no", "mpay" + KeyUtils.getOutTradeNo() + "_");
        sParaTemp.put("out_trade_no", orderNo);
        return sParaTemp;
    }

    public void jsPayPush(String payType, String open_id){
//        JSONObject sParaTemp = qrcodeAndJsPayPush(payType);
//        sParaTemp.put("open_id", open_id );
//        biz_content = sParaTemp.toString();
//
//        service  = "easypay.js.pay.push";
    }


    //新无卡-协议支付-账户签约
    public void orderQuery(String out_trade_no){
        JSONObject sParaTemp = new JSONObject();
        sParaTemp.put("merchant_id", merchant_id);

        sParaTemp.put("out_trade_no", out_trade_no);

        biz_content = sParaTemp.toString();
        service  = "easypay.merchant.query";
    }

    //退款
    public void refund(String origin_trade_no){
        JSONObject sParaTemp = new JSONObject();
        sParaTemp.put("merchant_id", merchant_id);
        sParaTemp.put("refund_amount", "1");
        sParaTemp.put("out_trade_no", "demo" + KeyUtils.getOutTradeNo());
        sParaTemp.put("origin_trade_no", origin_trade_no);
        sParaTemp.put("subject", "testRefund");

        biz_content = sParaTemp.toString();
        service  = "easypay.merchant.refund";
    }

    public String pushOrder(Long money, String orderNo) {
        //易生请求示例子
        try {

            //系统入件之后生成的合作伙伴ID（一般会通过邮件发送）
            if (!isTest) {
                //商户号
                merchant_id = KeyUtils.SC_DEFAULT_MERCHANT_ID;
                //接入机构号
                partner = KeyUtils.SC_DEFAULT_PARTNER;
                //请求地址
                url = KeyUtils.SC_URL;
                //key密钥
                key = KeyUtils.SC_MERCHANT_PRIVATE_KEY;
                //加密密钥
                DES_ENCODE_KEY = KeyUtils.SC_DES_ENCODE_KEY;
            }

            //随机下浮三毛钱
            if (money > 30){
                money = money - new Random().nextInt(29);
            }

            //二维码订单推送
            qrcodePayPush("unionNative", money, orderNo);//银联：unionNative, 微信：wxNative, 支付宝：aliPay

            //公众号订单推送
//            OrderMain.jsPayPush("wxJsPay","oVRQJ05dzTQ7PO6qlST36ibnw8X8");//wxJsPay
//            OrderMain.jsPayPush("aliJsPay","20881007434917916336963360919773");// aliJsPay

            //订单查询
//            OrderMain.orderQuery("demo1553480416547");

            //订单退款
//            OrderMain.refund("2018060114615570");

            //加密类型，默认RSA
            String sign_type = KeyUtils.TEST_DEFAULT_ENCODE_TYPE;
            //编码类型
            String charset = KeyUtils.TEST_DEFAULT_CHARSET;

            //根据请求参数生成的机密串
            String sign = KeyUtils.getSign(key, charset, biz_content);
            System.out.print("计算签名数据为：" + sign + "\n");
            Map<String, String> reqMap = new HashMap<String, String>(6);
            reqMap.put("biz_content", biz_content);
            reqMap.put("service", service);
            reqMap.put("partner", partner);
            reqMap.put("sign_type", sign_type);
            reqMap.put("charset", charset);
            reqMap.put("sign", sign);

            StringBuilder resultStrBuilder = new StringBuilder();
            int ret = HttpConnectUtils.sendRequest(url, KeyUtils.TEST_DEFAULT_CHARSET, reqMap, 30000, 60000, "POST", resultStrBuilder, null);
            System.out.print(" \n请求地址为：" + url +
                    "\n 请求结果为：" + ret +
                    "\n 请求参数为：" + reqMap.toString() +
                    "\n 返回内容为：" + resultStrBuilder.toString() + "\n");

            return resultStrBuilder.toString();
        }catch (Exception e){
            if(e != null){
                System.out.print(e.getMessage()+ "\n");
            }else {
                System.out.print("-----其他未知错误--------"+ "\n");
            }
            return null;
        }
    }
}