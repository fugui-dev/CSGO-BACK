package com.ruoyi.thirdparty.decsgopay.yspay.service;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.domain.common.constant.PayType;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.thirdparty.decsgopay.yspay.config.YSPayConfig;
import com.ruoyi.thirdparty.decsgopay.yspay.sdk.YSPaySDK;
import com.ruoyi.thirdparty.unifypaycallbackprocess.UnifyCallbackProcess;
import com.ruoyi.thirdparty.unifypaycallbackprocess.UnifyPayPerOrderProcess;
import com.ruoyi.thirdparty.zhaocaipay.vo.UnifyPayPreOrderVO;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class YSPayService {

    @Autowired
    YSPayConfig payConfig;

    @Autowired
    UnifyPayPerOrderProcess unifyPayPerOrderProcess;

    @Autowired
    UnifyCallbackProcess unifyCallbackProcess;



    public String testCallbackProcess(String testOrderNo){
        log.info("测试回调流程==>【{}】", testOrderNo);
        return unifyCallbackProcess.notifyProcess(testOrderNo);
    }


    //预订单
    public R<UnifyPayPreOrderVO> preOrder(CreateOrderParam param, TtUser user, HttpServletRequest request) {
        Asserts.notBlank(param.getPayType(), "支付方式不能为空！");

        //校验商品
        TtRechargeProd goodsInfo = unifyPayPerOrderProcess.getPayGoodsInfo(param);

        // 总价值
        BigDecimal totalAmount = param.getGoodsPrice().multiply(new BigDecimal(param.getGoodsNum()));

        //校验金额是否符合支付规则
        unifyPayPerOrderProcess.checkPayConfigRule(totalAmount, user, "ysPay");

        //下单
        YSPaySDK paySDK = new YSPaySDK(payConfig);
        String orderNo = IdUtils.fastSimpleUUID(); //开箱系统订单号

        //{"code":1,"trade_no":"2024092615384070492","qrcode":"https:\/\/qr.alipay.com\/bax05156cxzhtuk8dfk625c5"}
        JSONObject dataObj = paySDK.preOrder(param.getPayType(), orderNo, goodsInfo.getName(), String.valueOf(totalAmount));

        //判断结果，响应前端
        if (dataObj != null){

            //payurl和qrcode同时只能存在一个，这里需要做判空
            String payUrl = dataObj.get("payurl") != null ? dataObj.get("payurl").toString() : "";
            String url = dataObj.get("qrcode") != null ? dataObj.get("qrcode").toString() : "";
            if (StringUtils.isNotBlank(payUrl)){
                url = payUrl;
            }

            String outTradeNo = dataObj.get("trade_no").toString(); //外部订单号


            //存储订单
            unifyPayPerOrderProcess.savePayOrder(orderNo, outTradeNo, user, param, PayType.YS_PAY, totalAmount, url);


            return R.ok(new UnifyPayPreOrderVO(url, orderNo, outTradeNo));

        }

        return R.fail("支付申请失败！");
    }


    //回调处理
    public String callback(HttpServletRequest request) throws Exception {
        //取出参数
        Map<String, String> paramMap = getParameterMap(request);
        log.info("收到YSPAY支付回调==>【{}】", paramMap);

        //1验签
        YSPaySDK paySDK = new YSPaySDK(payConfig);
//        String sign = paramMap.get("sign");
//        String checkSign = ZhaoCaiPaySDK.generateSignature(paramMap, payConfig.getMd5key());
//        Assert.isTrue(checkSign.equals(sign));
//        log.info("CSPAY验签结果==>【{}】", checkSign.equals(sign));
        Assert.isTrue("TRADE_SUCCESS".equals(paramMap.get("trade_status")));

        //2向支付方查询订单号，如果查询可以不验签
        String orderNo = paramMap.get("out_trade_no"); //商户订单
        String outOrderNo = paramMap.get("trade_no"); //支付方订单
        boolean payFlag = paySDK.queryOrder(orderNo, outOrderNo);
        Assert.isTrue(payFlag, "未查询到支付成功的信息！");

        //3调用统一处理流程
        return unifyCallbackProcess.notifyProcess(orderNo);

    }

    public static Map<String, String> getParameterMap(HttpServletRequest request) {
        Map<String, String> parameterMap = new HashMap<>();

        // 获取请求参数的名称
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();

            // 将参数值数组转换为字符串，如果有多个值，用逗号分隔
            String paramValue = String.join(",", paramValues);
            parameterMap.put(paramName, paramValue);
        }

        return parameterMap;
    }
}
