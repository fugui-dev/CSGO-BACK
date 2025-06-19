package com.ruoyi.thirdparty.jiujia.service;

import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.thirdparty.jiujia.domain.CallbackBody;
import com.ruoyi.domain.other.CreateOrderParam;

public interface JiuJiaPayService {

    /**
     * 创建订单
     * @param createOrderBody 创建支付订单参数数据
     * @param ttUser 用户信息
     */
    String createPay(CreateOrderParam createOrderBody, TtUser ttUser);

    /**
     * 支付回调
     * @param callbackBody 回调参数
     */
    String callback(CallbackBody callbackBody);
}
