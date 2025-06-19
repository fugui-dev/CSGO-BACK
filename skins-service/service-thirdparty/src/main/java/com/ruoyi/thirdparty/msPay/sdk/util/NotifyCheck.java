package com.ruoyi.thirdparty.msPay.sdk.util;

import com.ruoyi.common.utils.StringUtils;

public class NotifyCheck {

    //回调通知报文，验证签名
    public Boolean isCheck(String contentStr, String signStr){
        try{
            return AlipaySignature.rsaCheckContent(contentStr, signStr, KeyUtils.SC_EASYPAY_PUBLIC_KEY, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    //回调通知报文，验证签名
    public Boolean isCheck(String msg){
//        String msg = "charset=utf-8&biz_content={\"ref_no\":\"11620190722110938208789\",\"notify_type\":\"WAIT_TRIGGER\",\"notify_id\":\"2019072205448161\",\"notify_time\":\"2019-07-22 12:37:24\",\"trade_no\":\"2019072205448161\",\"pay_no\":\"2019072205448161\",\"out_trade_no\":\"2019072200057\",\"payment_type\":\"1\",\"subject\":\"测试商品AAAAA\",\"body\":null,\"total_fee\":\"0.01\",\"amount\":\"0.01\",\"trade_status\":\"TRADE_FINISHED\",\"seller_email\":null,\"seller_id\":\"100000000081484\",\"buyer_id\":\"o8QAHwXd0BLZdYZzDQY0lDoHKnNQ\",\"buyer_email\":\"\",\"gmt_create\":\"2019-07-22 11:09:37\",\"gmt_payment\":\"2019-07-22 11:09:48\",\"is_success\":\"T\",\"is_total_fee_adjust\":\"0\",\"discount\":\"0\",\"gmt_logistics_modify\":\"2019-07-22 11:09:48\",\"price\":\"0.01\",\"quantity\":\"1\",\"seller_actions\":\"SEND_GOODS\"}&partner=100000000081484&sign=ZYgyNrwhKCEbHk9nJ0+XyswMjyBrDjrd9O2/8rV3/w1NTyoxua7UbYSapPB+gGtsK1nxzbBpU2yrKzRaEfPVsgMTP9vDs5rSgb3wALtrPJBqwSsobhQ/FidRFzc5WFt7vJBfdXaAAjuyly22z7QaQ/LEoEYHFa+sh5364/be+tA=&sign_type=RSA";

        String returnString = StringUtils.substringBetween(msg,"biz_content=", "&partner=");
        String returnSign = StringUtils.substringBetween(msg,"sign=","&sign_type");
        try{
            return AlipaySignature.rsaCheckContent(returnString, returnSign, KeyUtils.SC_EASYPAY_PUBLIC_KEY, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }



    public static void main(String[] args) {
            String msg = "charset=utf-8&biz_content={\"ref_no\":\"11620190722110938208789\",\"notify_type\":\"WAIT_TRIGGER\",\"notify_id\":\"2019072205448161\",\"notify_time\":\"2019-07-22 12:37:24\",\"trade_no\":\"2019072205448161\",\"pay_no\":\"2019072205448161\",\"out_trade_no\":\"2019072200057\",\"payment_type\":\"1\",\"subject\":\"测试商品AAAAA\",\"body\":null,\"total_fee\":\"0.01\",\"amount\":\"0.01\",\"trade_status\":\"TRADE_FINISHED\",\"seller_email\":null,\"seller_id\":\"100000000081484\",\"buyer_id\":\"o8QAHwXd0BLZdYZzDQY0lDoHKnNQ\",\"buyer_email\":\"\",\"gmt_create\":\"2019-07-22 11:09:37\",\"gmt_payment\":\"2019-07-22 11:09:48\",\"is_success\":\"T\",\"is_total_fee_adjust\":\"0\",\"discount\":\"0\",\"gmt_logistics_modify\":\"2019-07-22 11:09:48\",\"price\":\"0.01\",\"quantity\":\"1\",\"seller_actions\":\"SEND_GOODS\"}&partner=100000000081484&sign=ZYgyNrwhKCEbHk9nJ0+XyswMjyBrDjrd9O2/8rV3/w1NTyoxua7UbYSapPB+gGtsK1nxzbBpU2yrKzRaEfPVsgMTP9vDs5rSgb3wALtrPJBqwSsobhQ/FidRFzc5WFt7vJBfdXaAAjuyly22z7QaQ/LEoEYHFa+sh5364/be+tA=&sign_type=RSA";

        Boolean check = new NotifyCheck().isCheck(msg);

    }

}
