package com.ruoyi.thirdparty.zhaocaipay.service;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.domain.common.constant.PayType;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.thirdparty.unifypaycallbackprocess.UnifyCallbackProcess;
import com.ruoyi.thirdparty.unifypaycallbackprocess.UnifyPayPerOrderProcess;
import com.ruoyi.thirdparty.zhaocaipay.config.ZhaoCaiPayConfig;
import com.ruoyi.thirdparty.zhaocaipay.sdk.ZhaoCaiPaySDK;
import com.ruoyi.thirdparty.zhaocaipay.vo.UnifyPayPreOrderVO;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ZhaoCaiPayService {

    @Autowired
    ZhaoCaiPayConfig payConfig;

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
        //校验商品
        TtRechargeProd goodsInfo = unifyPayPerOrderProcess.getPayGoodsInfo(param);

        // 总价值
        BigDecimal totalAmount = param.getGoodsPrice().multiply(new BigDecimal(param.getGoodsNum()));

        //校验金额是否符合支付规则
        unifyPayPerOrderProcess.checkPayConfigRule(totalAmount, user, "zcPay");

        //下单
        ZhaoCaiPaySDK paySDK = new ZhaoCaiPaySDK(payConfig);
        String orderNo = IdUtils.fastSimpleUUID(); //开箱系统订单号

        //返回格式：{"qrcode":"https:\/\/9ec2518d.psknrktn3.xyz\/pay\/payOrderTwo?order_no=1924408788439412H6","url":"https:\/\/9ec2518d.psknrktn3.xyz\/pay\/payOrderTwo?order_no=1924408788439412H6","order_no":"1924408788439412H6"},"url":"https:\/\/9ec2518d.psknrktn3.xyz\/pay\/payOrderTwo?order_no=1924408788439412H6","key":"4otTe7euMoyImphsN9%253D-MqA6l","money":"3000.00","bank_user":"肖xx","receipt_name":"291864110@qq.com","bank_name":"0","bank_desc":null}
        JSONObject dataObj = paySDK.preOrder(totalAmount, user.getUserId().toString(), orderNo);

        //判断结果，响应前端
        if (dataObj != null){

            String url = dataObj.get("url").toString();
            String outTradeNo = dataObj.get("order_no").toString(); //外部订单号


            //存储订单
            unifyPayPerOrderProcess.savePayOrder(orderNo, outTradeNo, user, param, PayType.ZC_PAY, totalAmount, url);


            return R.ok(new UnifyPayPreOrderVO(url, orderNo, outTradeNo));

        }

        return R.fail("支付申请失败！");
    }


    //回调处理
    public String callback(HttpServletRequest request) throws Exception {
        //取出参数
        Map<String, String> paramMap = getParameterMap(request);
        log.info("收到招财支付回调==>【{}】", paramMap);

        //1验签
        ZhaoCaiPaySDK paySDK = new ZhaoCaiPaySDK(payConfig);
        String sign = paramMap.get("sign");
        String checkSign = ZhaoCaiPaySDK.generateSignature(paramMap, payConfig.getApiKey());
//        Assert.isTrue(checkSign.equals(sign));
        log.info("招财验签结果==>【{}】", checkSign.equals(sign));
        Assert.isTrue("CODE_SUCCESS".equals(paramMap.get("callbacks")));

        //2向支付方查询订单号，如果查询可以不验签
        String orderNo = paramMap.get("out_trade_no");
        boolean payFlag = paySDK.queryOrder(orderNo);
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
