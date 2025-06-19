package com.ruoyi.thirdparty.jiujia.util;

import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.thirdparty.jiujia.config.JiuJiaProperties;
import com.ruoyi.thirdparty.jiujia.domain.OrderBody;
import com.ruoyi.thirdparty.jiujia.domain.VisaVerificationBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JiuJiaUtils {

    public static String createSign(JiuJiaProperties jiuJiaProperties, OrderBody orderBody) {
        String param = "member_id=" + jiuJiaProperties.getMemberId() + "&app_key=" + jiuJiaProperties.getAppKey() +
                "&api_domain=" + jiuJiaProperties.getApiDomain() + "&total_amount=" + orderBody.getTotalAmount() +
                "&callback_url=" + jiuJiaProperties.getCallbackUrl() + "&order_id=" + orderBody.getOrderId() +
                "&key=" + jiuJiaProperties.getApiSecret();
        return Md5Utils.hash(param);
    }

    public static boolean visaVerification(JiuJiaProperties jiuJiaProperties, VisaVerificationBody visaVerificationBody) {
        String param = "member_id=" + jiuJiaProperties.getMemberId() + "&total_fee=" + visaVerificationBody.getTotalFee() +
                "&result_code=" + visaVerificationBody.getResultCode() + "&trade_no=" + visaVerificationBody.getTradeNo() +
                "&out_trade_no=" + visaVerificationBody.getOutTradeNo() + "&key=" + jiuJiaProperties.getApiSecret();
        String hash = Md5Utils.hash(param);
        return visaVerificationBody.getSign().equals(hash);
    }

    public static String createSubject() {
        List<String> list = new ArrayList<>();
        list.add("水晶箱子");
        list.add("木箱子");
        list.add("手工箱子");
        list.add("坚毅的箱子");
        list.add("金箱子");
        list.add("木钥匙");
        list.add("水晶钥匙");
        list.add("铁箱子");
        list.add("手工钥匙");
        list.add("金钥匙");
        list.add("纸箱子");
        list.add("万能钥匙");
        list.add("生锈的钥匙");
        int randomIndex = new Random().nextInt(list.size());
        return list.get(randomIndex);
    }
}
