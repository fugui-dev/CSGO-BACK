package com.ruoyi.thirdparty.abpay.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.service.TtOrderService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.domain.common.constant.PayType;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.thirdparty.abpay.config.ABPayConfig;
import com.ruoyi.thirdparty.abpay.sdk.ABPaySDK;
import com.ruoyi.thirdparty.unifypaycallbackprocess.UnifyCallbackProcess;
import com.ruoyi.thirdparty.unifypaycallbackprocess.UnifyPayPerOrderProcess;
import com.ruoyi.thirdparty.zhaocaipay.vo.UnifyPayPreOrderVO;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ABPayService {

    @Autowired
    ABPayConfig payConfig;

    @Autowired
    UnifyPayPerOrderProcess unifyPayPerOrderProcess;

    @Autowired
    UnifyCallbackProcess unifyCallbackProcess;

    @Autowired
    TtOrderService orderService;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    //预订单
    public R<UnifyPayPreOrderVO> preOrder(CreateOrderParam param, TtUser user, HttpServletRequest request) {
        //校验商品
        TtRechargeProd goodsInfo = unifyPayPerOrderProcess.getPayGoodsInfo(param);

        // 总价值
        BigDecimal totalAmount = param.getGoodsPrice().multiply(new BigDecimal(param.getGoodsNum()));

        //校验金额是否符合支付规则
        unifyPayPerOrderProcess.checkPayConfigRule(totalAmount, user, "abPay");

        //下单
        ABPaySDK paySDK = new ABPaySDK(payConfig);
        String orderNo = IdUtils.fastSimpleUUID(); //开箱系统订单号

        //响应内容{"success":true,"code":"200","msg":"success","retry":false,"time":"1719313850231","data":"https://ch5.iq8477.xyz/bus/openapi/amount/in/auto/pre?sign=5b3ffe2a73f5455b95b838765c34aa44","busRecordId":"799b5d8fb4094a37b5a3af0c1280b705","errorStack":null,"args":null}
        JSONObject dataObj = paySDK.preOrder(orderNo, totalAmount, user);

        //判断结果，响应前端
        if (dataObj != null){

            String url = dataObj.get("data").toString();
//            String orderNo = dataObj.get("busRecordId").toString(); //外部订单号（AB支付下单没有返回外部订单号）
            String outTradeNo = "";


            //存储订单
            unifyPayPerOrderProcess.savePayOrder(orderNo, outTradeNo, user, param, PayType.AB_PAY, totalAmount, url);


            return R.ok(new UnifyPayPreOrderVO(url, orderNo, outTradeNo));

        }

        return R.fail("支付申请失败！");
    }


    //回调处理
    public String callback(HttpServletRequest request) throws Exception {
        //取出参数
        String rawJson = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining());
        JSONObject paramMap = JSONObject.parseObject(rawJson);
        log.info("收到AB支付回调==>【{}】", paramMap);

        //响应code 200
        Assert.isTrue(("200".equals(paramMap.get("code"))));

        //1验签（这个AB支付的验签太SB，直接查订单吧）
        ABPaySDK paySDK = new ABPaySDK(payConfig);

        //code不参与验签,获取签名
//        paramMap.remove("code");
//        JSONObject callbackData = JSONObject.parseObject(JSONObject.toJSONString(paramMap));
//        String signature = dataJSON.get("signature").toString();
//        String checkSign = ABPaySDK.generateSignature(callbackData, payConfig.getPrivateKey());

//        JSONObject dataJSON = JSONObject.parseObject(JSONObject.toJSONString(paramMap.get("data")));


        //2向支付方查询订单号，如果查询可以不验签
        String orderNo = paramMap.get("busRecordId").toString();
        boolean payFlag = paySDK.queryOrder(orderNo);
        Assert.isTrue(payFlag, "未查询到支付成功的信息！");

        //3调用统一处理流程
        String notifyProcess = unifyCallbackProcess.notifyProcess(orderNo);

        //因为ab支付下单时没有返回第三方单号，这里需要异步处理一下
        if ("success".equals(notifyProcess)){
            threadPoolTaskExecutor.execute(()->{
                String outTradeNo = JSONObject.parseObject(paramMap.get("data").toString()).get("recordId").toString();
                appendABPayOutOrderNo(orderNo, outTradeNo);
            });
        }

        return notifyProcess;

    }

    //补充ab支付第三方订单号
    private void appendABPayOutOrderNo(String orderNo, String outTradeNo) {
        TtOrder order = orderService.getOne(Wrappers.lambdaQuery(TtOrder.class)
                .eq(TtOrder::getOrderId, orderNo), false);

        if (order == null) {
            log.info("补充支付方订单号时未找到订单！");
            return;
        }

        //这里多创建一个，避免多个任务同时在更新订单
        TtOrder updateOrder = new TtOrder();
        updateOrder.setId(order.getId());
        updateOrder.setOutTradeNo(outTradeNo);
        boolean update = orderService.updateById(updateOrder);
        log.info("订单【{}】补充第三方支付订单号【{}】成功？【{}】", orderNo, outTradeNo, update);

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
