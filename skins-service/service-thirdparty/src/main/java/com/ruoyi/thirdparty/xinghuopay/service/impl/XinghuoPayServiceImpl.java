package com.ruoyi.thirdparty.xinghuopay.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.domain.common.constant.PayOrderStatus;
import com.ruoyi.domain.common.constant.PayType;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.TtPromotionLevel;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.domain.other.TtRechargeRecord;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.common.service.RechargeSuccessfulNoticeService;
import com.ruoyi.thirdparty.xinghuopay.config.XinghuoPayConfig;
import com.ruoyi.thirdparty.xinghuopay.domain.XinghuoPayResponse;
import com.ruoyi.thirdparty.xinghuopay.service.XinghuoPayService;
import com.ruoyi.thirdparty.xinghuopay.util.XinghuoPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Service
public class XinghuoPayServiceImpl implements XinghuoPayService {

    @Autowired
    private TtRechargeProdMapper rechargeProdMapper;

    @Autowired
    private XinghuoPayConfig xinghuoPayConfig;

    @Autowired
    private TtOrderMapper orderMapper;

    @Autowired
    private TtUserMapper userMapper;

    @Autowired
    private TtRechargeProdMapper rechargeListMapper;

    @Autowired
    private TtUserService userService;

    @Autowired
    private TtUserBlendErcashMapper userBlendErcashMapper;

    @Autowired
    private RechargeSuccessfulNoticeService rechargeSuccessfulNoticeService;

    @Autowired
    private TtRechargeRecordMapper ttRechargeRecordMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private TtPromotionLevelMapper ttPromotionLevelMapper;

    @Autowired
    private TtFirstRechargeMapper ttFirstRechargeMapper;

    @Override
    public R pay(CreateOrderParam param, TtUser ttUser, String ip) {
        // 1.查询商品信息
        TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeProdMapper)
                .eq(TtRechargeProd::getId, param.getGoodsId())
                .eq(TtRechargeProd::getStatus, 0)
                .one();
        if (ObjectUtil.isEmpty(goods)) {
            return R.fail("不存在的商品");
        }
        if (goods.getPrice().compareTo(param.getGoodsPrice()) != 0) {
            return R.fail("商品价格不一致");
        }

        // 计算总价值
        BigDecimal totalAmount = param.getGoodsPrice().multiply(new BigDecimal(param.getGoodsNum()));

        // 2.构建支付参数
        Map<String, String> map = new HashMap<>();
        map.put("pid", xinghuoPayConfig.getPid());
        map.put("type", "alipay");
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        String orderId = String.valueOf(snowflake.nextId());
        map.put("out_trade_no", orderId);
        map.put("notify_url", xinghuoPayConfig.getNotifyUrl());
        map.put("return_url", xinghuoPayConfig.getReturnUrl());
        map.put("name", goods.getName());
        map.put("money", String.valueOf(totalAmount));
        map.put("clientip", ip);
        String key = xinghuoPayConfig.getSign();
        XinghuoPayUtil signUtil = new XinghuoPayUtil(key);
        String sign = signUtil.getSign(map);
        map.put("sign", sign);
        map.put("sign_type", "MD5");

        // 3.发送请求
        HttpRequest post = HttpUtil.createPost(xinghuoPayConfig.getServerUrl());
        post.header("Content-Type","application/x-www-form-urlencoded");
        post.formStr(map);
        HttpResponse res = post.execute();

        // 4.解析响应
        XinghuoPayResponse resBody = JSONUtil.toBean(res.body(), XinghuoPayResponse.class);
        if (!resBody.getCode().equals("1")) {
            return R.fail(resBody.getMsg());
        }

        // 5.创建订单
        TtOrder order = new TtOrder();
        order.setOrderId(orderId);
        order.setOutTradeNo(resBody.getTradeNo());
        order.setUserId(ttUser.getUserId());
        order.setType(PayType.XIN_HUO_ZFB.getCode());
        order.setGoodsId(param.getGoodsId());
        order.setGoodsPrice(param.getGoodsPrice());
        order.setGoodsNum(param.getGoodsNum());
        order.setTotalAmount(totalAmount);
        order.setSign(sign);
        order.setStatus(PayOrderStatus.NO_PAY.getCode());
        order.setCreateTime(new Date());
        orderMapper.insert(order);

        return R.ok(resBody);
    }

    @Override
    public String notify(Map<String, String> params) {
        log.info("支付成功通知");
        String result = "failure";

        // 校验
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            log.error("交易失败");
            return result;
        }

        // 1.查询订单信息
        String outTradeNo = params.get("trade_no");
        TtOrder order = new LambdaQueryChainWrapper<>(orderMapper)
                .eq(TtOrder::getOutTradeNo, outTradeNo)
                .eq(TtOrder::getStatus, PayOrderStatus.NO_PAY.getCode())
                .one();
        // 防止重复通知
        if (!PayOrderStatus.NO_PAY.getCode().equals(order.getStatus())) {
            return result;
        }

        // 2.查询用户信息
        TtUser user = new LambdaQueryChainWrapper<>(userMapper)
                .eq(TtUser::getUserId, order.getUserId())
                .eq(TtUser::getDelFlag, 0)
                .one();

        // 3.查询商品信息
        TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeListMapper)
                .eq(TtRechargeProd::getId, order.getGoodsId())
                .eq(TtRechargeProd::getStatus, 0)
                .one();

        // 4.账户结算
        if (ObjectUtil.isNull(goods.getProductA())) goods.setProductA(BigDecimal.ZERO);
        if (ObjectUtil.isNull(goods.getProductC())) goods.setProductC(BigDecimal.ZERO);
        payNotifyAccounting(order, user, goods, order.getGoodsNum());

        // 首充赠送
        firstChargeGiftAmount(user, goods, order.getGoodsNum());

        // 推广等级充值赠送
        promotionLevelChargeGiftAmount(user, goods, order.getGoodsNum());

        // 5.更新订单
        boolean update = new LambdaUpdateChainWrapper<>(orderMapper)
                .eq(TtOrder::getId, order.getId())
                .set(TtOrder::getStatus, PayOrderStatus.PAY_COMPLE.getCode())
                .set(TtOrder::getUpdateTime, new Date())
                .update();
        if (update) {
            result = "success";
        }

        // 6.发送充值通知
        rechargeSuccessfulNoticeService.sendRechargeSuccessNotice(order.getUserId().toString(), order.getGoodsPrice());

        return result;
    }

    // 账户结算
    public void payNotifyAccounting(TtOrder order, TtUser user, TtRechargeProd goods, Integer goodsNumber) {

        // 加钱
        // BigDecimal totalAmount = order.getTotalAmount();
        BigDecimal totalAmount = goods.getProductA().multiply(new BigDecimal(goodsNumber));
        BigDecimal totalCredits = goods.getProductC().multiply(new BigDecimal(goodsNumber));
        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate
                .eq(TtUser::getUserId, user.getUserId())
                .setSql("account_amount = account_amount + " + totalAmount.toString()
                        + ",account_credits = account_credits + " + totalCredits.toString());
        userService.update(userUpdate);

        user = userService.getById(user.getUserId());
        // 综合消费日志
        TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                .userId(user.getUserId())

                .amount(totalAmount.compareTo(BigDecimal.ZERO) > 0 ? totalAmount : null)
                .finalAmount(totalAmount.compareTo(BigDecimal.ZERO) > 0 ? user.getAccountAmount().add(totalAmount) : null)

                .credits(totalCredits.compareTo(BigDecimal.ZERO) > 0 ? totalCredits : null)
                .finalCredits(totalCredits.compareTo(BigDecimal.ZERO) > 0 ? user.getAccountCredits().add(totalCredits) : null)

                .total(totalAmount.add(totalCredits))  // 收支合计

                .type(TtAccountRecordType.INPUT.getCode())
                .source(TtAccountRecordSource.RECHARGE.getCode())
                .remark(TtAccountRecordSource.RECHARGE.getMsg())

                .createTime(new Timestamp(System.currentTimeMillis()))
                .updateTime(new Timestamp(System.currentTimeMillis()))
                .build();

        userBlendErcashMapper.insert(blendErcash);

        // 充值记录
        TtRechargeRecord ttRechargeRecord = TtRechargeRecord.builder().build();
        ttRechargeRecord.setUserId(order.getUserId());
        ttRechargeRecord.setParentId(user.getParentId());
        ttRechargeRecord.setArrivalAmount(totalAmount);
        ttRechargeRecord.setAmountActuallyPaid(totalAmount);
        ttRechargeRecord.setFinallyPrice(user.getAccountAmount());
        ttRechargeRecord.setOrderId(order.getOrderId());
        ttRechargeRecord.setOutTradeNo(order.getOutTradeNo());
        ttRechargeRecord.setStatus("0");
        ttRechargeRecord.setChannelType("1");
        ttRechargeRecord.setCreateTime(DateUtils.getNowDate());
        ttRechargeRecordMapper.insert(ttRechargeRecord);
    }

    /**
     * 首充赠送
     */
    private void firstChargeGiftAmount(TtUser ttUser, TtRechargeProd goods, Integer goodsNumber) {
        // 1.判断是否为首充
        LambdaQueryWrapper<TtUserBlendErcash> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(TtUserBlendErcash::getSource, 1)
                .eq(TtUserBlendErcash::getUserId, ttUser.getUserId());
        List<TtUserBlendErcash> ttUserBlendErcashes = userBlendErcashMapper.selectList(wrapper);
        if (ttUserBlendErcashes.size() > 0) {
            return;
        }

        // 2.加钱
        // 充值金额
        BigDecimal totalAmount = goods.getProductA().multiply(new BigDecimal(goodsNumber));
        // 查询赠送比例
        BigDecimal firstChargeAmountRatio = ttFirstRechargeMapper.selectRatioByMinAmount(totalAmount);
        if (Objects.isNull(firstChargeAmountRatio)) {
            return;
        }
        // 计算赠送金额
        BigDecimal giftAmount = totalAmount.multiply(firstChargeAmountRatio);
        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate
                .eq(TtUser::getUserId, ttUser.getUserId())
                .setSql("account_amount = account_amount + " + giftAmount.toString());
        userService.update(userUpdate);

        // 3.记录
        ttUser = userService.getById(ttUser.getUserId());
        TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                .userId(ttUser.getUserId())
                .amount(giftAmount.compareTo(BigDecimal.ZERO) > 0 ? giftAmount : null)
                .finalAmount(giftAmount.compareTo(BigDecimal.ZERO) > 0 ? ttUser.getAccountAmount().add(giftAmount) : null)
                .total(giftAmount)
                .type(TtAccountRecordType.INPUT.getCode())
                .source(TtAccountRecordSource.FIRST_CHARGE.getCode())
                .remark(TtAccountRecordSource.FIRST_CHARGE.getMsg())
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        userBlendErcashMapper.insert(blendErcash);
    }

    /**
     * 推广等级充值赠送
     */
    private void promotionLevelChargeGiftAmount(TtUser ttUser, TtRechargeProd goods, Integer goodsNumber) {
        // 1.查询用户推广等级对应的赠送比例
        TtPromotionLevel ttPromotionLevel = ttPromotionLevelMapper.selectById(ttUser.getPromotionLevel());

        // 2.加钱
        BigDecimal amountRatio = ttPromotionLevel.getCommissions();
        BigDecimal totalAmount = goods.getProductA().multiply(new BigDecimal(goodsNumber));
        BigDecimal giftAmount = totalAmount.multiply(amountRatio);
        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate
                .eq(TtUser::getUserId, ttUser.getUserId())
                .setSql("account_amount = account_amount + " + giftAmount.toString());
        userService.update(userUpdate);

        // 3.记录
        ttUser = userService.getById(ttUser.getUserId());
        TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                .userId(ttUser.getUserId())
                .amount(giftAmount.compareTo(BigDecimal.ZERO) > 0 ? giftAmount : null)
                .finalAmount(giftAmount.compareTo(BigDecimal.ZERO) > 0 ? ttUser.getAccountAmount().add(giftAmount) : null)
                .total(giftAmount)
                .type(TtAccountRecordType.INPUT.getCode())
                .source(TtAccountRecordSource.PROMOTION_LEVEL_CHARGE.getCode())
                .remark(TtAccountRecordSource.PROMOTION_LEVEL_CHARGE.getMsg())
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        userBlendErcashMapper.insert(blendErcash);
    }
}
