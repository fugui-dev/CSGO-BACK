package com.ruoyi.thirdparty.zyZFB.service.Impl;

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
import com.ruoyi.domain.common.constant.PayOrderStatus;
import com.ruoyi.domain.common.constant.PayType;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.TtPromotionLevel;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.*;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.common.service.RechargeSuccessfulNoticeService;
import com.ruoyi.thirdparty.zyZFB.config.ZYConfig;
import com.ruoyi.thirdparty.zyZFB.controller.ZyController.PayNotifyData;
import com.ruoyi.thirdparty.zyZFB.service.zyService;
import com.ruoyi.thirdparty.zyZFB.utils.signUtil;
import com.ruoyi.thirdparty.zyZFB.utils.ZYHTTPRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class zyServiceImpl implements zyService {

    @Autowired
    private ZYConfig zyConfig;

    @Autowired
    private TtOrderMapper orderMapper;

    @Autowired
    private ThreadPoolExecutor customThreadPoolExecutor;

    @Autowired
    private TtUserService userService;

    @Autowired
    private TtUserMapper userMapper;

    @Autowired
    private TtRechargeProdMapper rechargeListMapper;

    @Autowired
    private TtUserBlendErcashMapper userBlendErcashMapper;

    @Autowired
    private RechargeSuccessfulNoticeService rechargeSuccessfulNoticeService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private TtPromotionLevelMapper ttPromotionLevelMapper;

    @Autowired
    private TtFirstRechargeMapper ttFirstRechargeMapper;

    @Override
    public R ApiAddTrans(@Validated CreateOrderParam param, TtUser user, HttpServletRequest request) {

        // 构建完整参数
        Map<String, String> map = buildBaseParam(new HashMap<>(), request);

        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        String merReqNo = String.valueOf(snowflake.nextId());
        map.put("merReqNo", merReqNo);
        map.put("notifyUrl", zyConfig.getNotifyBaseUrl() + "/api/zyZFB/addTransNotify");
        map.put("returnUrl", zyConfig.getNotifyBaseUrl());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String tranDateTime = dateFormat.format(new Date());
        map.put("tranDateTime", tranDateTime);

        BigDecimal totalAmount = param.getGoodsPrice()
                .multiply(new BigDecimal(param.getGoodsNum()))
                .multiply(new BigDecimal("100"))
                .setScale(0, BigDecimal.ROUND_HALF_EVEN);
        map.put("amt", totalAmount.toString());
        map.put("goodsName", param.getGoodsId().toString());

        String sign = signUtil.getSign(map, zyConfig.getMerKey());
        map.put("sign", sign);

        // 发送请求
        HttpRequest post = HttpUtil.createPost(zyConfig.getGateway() + ZYConfig.ApiAddTrans);
        // post.header("Content-Type","application/x-www-form-urlencoded");
        // post.body(param, "application/x-www-form-urlencoded");
        post.body(JSONUtil.toJsonStr(map), "json");
        HttpResponse res = post.execute();

        // 解析响应
        ZYHTTPRes resBody = JSONUtil.toBean(res.body(), ZYHTTPRes.class);
        if (!resBody.getRespCode().equals("0000")) {
            return R.fail(resBody);
        }

        TtOrder order = new TtOrder();
        order.setUserId(user.getUserId());
        // ttOrder.setThirdParty("0");
        order.setType(PayType.ZY_ZFB.getCode());
        order.setGoodsId(param.getGoodsId());
        order.setGoodsPrice(param.getGoodsPrice());
        order.setGoodsNum(param.getGoodsNum());
        order.setTotalAmount(totalAmount);
        order.setOrderId(map.get("merReqNo"));
        order.setSign(sign);
        order.setStatus(PayOrderStatus.NO_PAY.getCode());
        order.setOutTradeNo(resBody.getMerReqNo());
        order.setPayUrl(resBody.getPayUrl());
        order.setCreateTime(new Date());

        // System.out.println(order.getPayUrl().length());

        orderMapper.insert(order);

        // 发送充值成功通知
        rechargeSuccessfulNoticeService.sendRechargeSuccessNotice(user.getUserId().toString(), order.getGoodsPrice());

        return R.ok(order);
    }

    @Override
    public String payNotify(PayNotifyData data) {

        // System.out.println("中云 支付回调" + JSONUtil.toJsonStr(data));

        TtOrder order = null;
        // TODO: 2024/4/12 最好再查询一下第三方平台的订单信息

        try {

            // 查询订单信息
            order = new LambdaQueryChainWrapper<>(orderMapper)
                    .eq(TtOrder::getOrderId, data.getMerReqNo())
                    .eq(TtOrder::getStatus, PayOrderStatus.NO_PAY.getCode())
                    .one();

            if (ObjectUtil.isEmpty(order)) {
                log.warn("支付回调异常，不存在的有效订单。");
                return "fail";
            }

            // 查询用户信息
            TtUser user = new LambdaQueryChainWrapper<>(userMapper)
                    .eq(TtUser::getUserId, order.getUserId())
                    .eq(TtUser::getDelFlag, 0)
                    .one();
            if (ObjectUtil.isEmpty(user)) {
                log.warn("支付回调异常，不存在的有效用户。");
                return "fail";
            }

            if (order.getStatus().equals(PayOrderStatus.PAY_COMPLE.getCode()) || order.getStatus().equals(PayOrderStatus.PAY_YET.getCode())) {
                log.warn("重复的回调！该订单已完成。");
                return "success";
            }

            // 查询商品信息
            TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeListMapper)
                    .eq(TtRechargeProd::getId, order.getGoodsId())
                    .eq(TtRechargeProd::getStatus, 0)
                    .one();
            if (ObjectUtil.isEmpty(goods)) {
                log.warn("支付回调异常，不存在的商品。");
                return "fail";
            }

            // 账户结算
            if (ObjectUtil.isNull(goods.getProductA())) goods.setProductA(BigDecimal.ZERO);
            if (ObjectUtil.isNull(goods.getProductC())) goods.setProductC(BigDecimal.ZERO);
            payNotifyAccounting(order, user, goods, order.getGoodsNum());

            // 首充赠送
            firstChargeGiftAmount(user, goods, order.getGoodsNum());

            // 推广等级充值赠送
            promotionLevelChargeGiftAmount(user, goods, order.getGoodsNum());

            // 更新订单
            new LambdaUpdateChainWrapper<>(orderMapper)
                    .eq(TtOrder::getId, order.getId())
                    .set(TtOrder::getStatus, PayOrderStatus.PAY_COMPLE.getCode())
                    .set(TtOrder::getUpdateTime, new Date())
                    .update();

            return "success";

        } catch (Exception e) {

            e.printStackTrace();

            log.warn("zy zfb 回调异常");

            if (ObjectUtil.isNotNull(order)) {
                new LambdaUpdateChainWrapper<>(orderMapper)
                        .eq(TtOrder::getId, order.getId())
                        .set(TtOrder::getStatus, PayOrderStatus.CALL_BACK_ERRO.getCode())
                        .set(TtOrder::getUpdateTime, new Date())
                        .update();
            }

            return "fail";

        }

    }

    @Override
    public R ApiQueryTrans(Map<String, String> param, TtUser user) {
        return null;
    }

    @Override
    public R ApiPropayTrans(Map<String, String> param, TtUser user) {
        return null;
    }

    @Override
    public R ApiQueryBalancePHP(Map<String, String> param, TtUser user) {
        return null;
    }

    // 构建基本请求信息
    private Map<String, String> buildBaseParam(Map<String, String> map, HttpServletRequest request) {
        map.put("merchantId", zyConfig.getMerchantId());
        map.put("tranType", zyConfig.getTranType());
        map.put("msgExt", "success");
        // map.put("tranType", "2004");
        // map.put("notifyUrl",zyConfig.getNotifyBaseUrl()+"api/zyZFB/addTransNotify");
        if (ObjectUtil.isNotEmpty(request)) {
            map.put("creatIp", request.getRemoteAddr());
            // map.put("creatIp", "110.228.15.148");
        }
        return map;
    }

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

        int insert = userBlendErcashMapper.insert(blendErcash);

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
