package com.ruoyi.thirdparty.unifypaycallbackprocess;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.mapper.TtOrderMapper;
import com.ruoyi.admin.mapper.TtPayConfigMapper;
import com.ruoyi.admin.mapper.TtRechargeProdMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.domain.common.constant.PayOrderStatus;
import com.ruoyi.domain.common.constant.PayType;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.TtPayConfig;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class UnifyPayPerOrderProcess {

    @Autowired
    TtRechargeProdMapper rechargeProdMapper;

    @Autowired
    private TtOrderMapper orderMapper;

    @Autowired
    private TtPayConfigMapper payConfigMapper;

    //校验支付规则（payTag：支付标识，对应payConfig表中的payTag字段）
    public void checkPayConfigRule (BigDecimal totalAmount, TtUser user, String payTag){
        TtPayConfig ttPayConfig = new TtPayConfig();
        ttPayConfig.setPayTag(payTag);
        List<TtPayConfig> list = payConfigMapper.selectTtPayConfigList(ttPayConfig);
        Assert.notEmpty(list, payTag + "支付通道未配置！");

        TtPayConfig config = list.get(0);

        //金额符合支付范围，用户总充值门槛达到最低充值
        Assert.isTrue(totalAmount.compareTo(config.getPayMinMoney()) >= 0, "该充值渠道不能低于" +config.getPayMinMoney());
        Assert.isTrue(totalAmount.compareTo(config.getPayMaxMoney()) <= 0, "该充值渠道不能高于" +config.getPayMaxMoney());

        // 用户最低充值金额判断
        BigDecimal totalRecharge = user.getTotalRecharge();
        if (totalRecharge == null) totalRecharge = BigDecimal.valueOf(0);
        Assert.isTrue(totalRecharge.compareTo(config.getUserTotalMinMoney()) >= 0, "用户总充值低于渠道充值门槛" +config.getUserTotalMinMoney());
    }


    //查询商品信息，校验商品
    public TtRechargeProd getPayGoodsInfo (CreateOrderParam param){
        // 查询商品信息
        TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeProdMapper)
                .eq(TtRechargeProd::getId, param.getGoodsId())
                .eq(TtRechargeProd::getStatus, 0)
                .one();
        if (ObjectUtil.isEmpty(goods)) {
            throw new ServiceException("不存在的商品！");
        }

        if (goods.getPrice().compareTo(param.getGoodsPrice()) != 0){
            throw new ServiceException("商品价格不一致！");
        }

        return goods;

    }

    //插入订单
    public void savePayOrder(String orderNo, String outTradeNo, TtUser user, CreateOrderParam param, PayType payType, BigDecimal totalAmount, String payUrl){
        //下单成功，插入订单
        // 创建订单
        TtOrder order = new TtOrder();
        order.setOrderId(orderNo); //系统订单号
        order.setOutTradeNo(outTradeNo); //三方订单号

        order.setUserId(user.getUserId());
        order.setType(payType.getCode());

        order.setGoodsId(param.getGoodsId());
        order.setGoodsPrice(param.getGoodsPrice());
        order.setGoodsNum(param.getGoodsNum());
        order.setTotalAmount(totalAmount);

        order.setSign("sign");
        order.setStatus(PayOrderStatus.NO_PAY.getCode());

        order.setPayUrl(payUrl);

        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setLogidUrl(user.getBdChannelUrl());

        int i = orderMapper.insert(order);

        if (i <= 0){
            throw new ServiceException("保存支付订单失败！");
        }

    }

}
