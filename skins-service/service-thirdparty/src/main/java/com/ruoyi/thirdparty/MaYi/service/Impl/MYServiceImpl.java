package com.ruoyi.thirdparty.MaYi.service.Impl;

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
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.domain.common.constant.PayOrderStatus;
import com.ruoyi.domain.common.constant.PayType;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.dto.mayi.ApiPayAddOrderResponse;
import com.ruoyi.domain.dto.mayi.PayNotifyRequest;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.TtPromotionLevel;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.MaYi.config.MYConfig;
import com.ruoyi.thirdparty.MaYi.service.MYService;
import com.ruoyi.thirdparty.MaYi.utils.signUtil;
import com.ruoyi.thirdparty.common.service.RechargeSuccessfulNoticeService;
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
public class MYServiceImpl implements MYService {

    @Autowired
    private MYConfig myConfig;

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
    private TtRechargeProdMapper rechargeProdMapper;

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

        // 查询商品信息
        TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeProdMapper)
                .eq(TtRechargeProd::getId, param.getGoodsId())
                .eq(TtRechargeProd::getStatus, 0)
                .one();
        if (ObjectUtil.isEmpty(goods)) return R.fail("不存在的商品。");
        if (goods.getPrice().compareTo(param.getGoodsPrice()) != 0) return R.fail("商品价格不一致。");

        // 总价值
        BigDecimal totalAmount = param.getGoodsPrice().multiply(new BigDecimal(param.getGoodsNum()));

        // 构建完整参数
        Map<String, String> map = buildBaseParam(new HashMap<>(), request);

        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        String orderId = String.valueOf(snowflake.nextId());
        map.put("pay_orderid", orderId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String tranDateTime = dateFormat.format(new Date());
        map.put("pay_applydate", tranDateTime);

        map.put("pay_bankcode", myConfig.getPayBankCode());

        // 回调地址
        map.put("pay_notifyurl", myConfig.getNotifyBaseUrl());

        // 同步跳转
        map.put("pay_callbackurl", myConfig.getCallBackUrl() + "/api/mayi/notify");

        map.put("pay_amount", totalAmount.toString());

        // 签名
        String sign = signUtil.getSign(map, myConfig.getApiKey());
        map.put("pay_md5sign", sign);
        map.put("pay_clientip", IpUtils.getIpAddr());
        map.put("pay_productname", goods.getName());
        map.put("type", "json");

        // System.out.println(JSONUtil.toJsonStr(map));

        // 发送请求
        HttpRequest post = HttpUtil.createPost(myConfig.getGateway() + MYConfig.ApiPayAddOrder);
        post.header("Content-Type","application/x-www-form-urlencoded");
        post.formStr(map);
        HttpResponse res = post.execute();

        // 解析响应
        ApiPayAddOrderResponse resBody = JSONUtil.toBean(res.body(), ApiPayAddOrderResponse.class);
        if (!resBody.getStatus().equals("1")) {
            return R.fail(resBody);
        }

        // 创建订单
        TtOrder order = new TtOrder();
        order.setOrderId(orderId);
        order.setOutTradeNo(resBody.getPay_orderid());

        order.setUserId(user.getUserId());
        order.setType(PayType.JU_HE_ZHI_FU.getCode());

        order.setGoodsId(param.getGoodsId());
        order.setGoodsPrice(param.getGoodsPrice());
        order.setGoodsNum(param.getGoodsNum());
        order.setTotalAmount(totalAmount);

        order.setSign(sign);
        order.setStatus(PayOrderStatus.NO_PAY.getCode());

        order.setPayUrl(resBody.getPayUrl());

        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        orderMapper.insert(order);
        return R.ok(order);
    }

    @Override
    public String payNotify(PayNotifyRequest data) {
        TtOrder order = null;
        // TODO: 2024/4/12 最好再查询一下第三方平台的订单信息
        try {
            // 查询订单信息
            order = new LambdaQueryChainWrapper<>(orderMapper)
                    .eq(TtOrder::getOrderId, data.getOrderid())
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

            // 发送充值成功通知
            rechargeSuccessfulNoticeService.sendRechargeSuccessNotice(user.getUserId().toString(), order.getGoodsPrice());

            return "success";
        } catch (Exception e) {
            e.printStackTrace();

            log.warn("聚合支付，回调异常");

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
        map.put("pay_memberid", myConfig.getMemberid());
        return map;
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

//        user = userService.getById(user.getUserId());
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
        log.info("写入成功？【{}】充值记录==>【{}】", insert, blendErcash);
    }

    /**
     * 首充赠送
     */
    public Boolean firstChargeGiftAmount(TtUser ttUser, TtRechargeProd goods, Integer goodsNumber) {
        // 1.判断是否为首充
        LambdaQueryWrapper<TtUserBlendErcash> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(TtUserBlendErcash::getSource, 1)
                .eq(TtUserBlendErcash::getUserId, ttUser.getUserId());
        List<TtUserBlendErcash> ttUserBlendErcashes = userBlendErcashMapper.selectList(wrapper);
        if (ttUserBlendErcashes.size() > 0) {
            return false;
        }

        // 2.加钱
        // 充值金额
        BigDecimal totalAmount = goods.getProductA().multiply(new BigDecimal(goodsNumber));
        // 查询赠送比例
        BigDecimal firstChargeAmountRatio = ttFirstRechargeMapper.selectRatioByMinAmount(totalAmount);
        if (Objects.isNull(firstChargeAmountRatio) || firstChargeAmountRatio.equals(new BigDecimal("0.00"))) {
            return false;
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
        int insert = userBlendErcashMapper.insert(blendErcash);

        log.info("写入成功？【{}】首充赠送记录==>【{}】", insert, blendErcash);

        return insert > 0;
    }

    /**
     * 推广等级充值赠送
     */
    public void promotionLevelChargeGiftAmount(TtUser ttUser, TtRechargeProd goods, Integer goodsNumber) {
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
