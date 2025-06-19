package com.ruoyi.thirdparty.alipay.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
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
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.alipay.config.AliProperties;
import com.ruoyi.thirdparty.alipay.service.AliPayService;
import com.ruoyi.thirdparty.common.service.RechargeSuccessfulNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private TtRechargeProdMapper rechargeProdMapper;

    @Autowired
    private AliProperties aliProperties;

    @Autowired
    private TtOrderMapper orderMapper;

    @Autowired
    private TtUserBlendErcashMapper userBlendErcashMapper;

    @Autowired
    private TtUserService userService;

    @Autowired
    private TtUserMapper userMapper;

    @Autowired
    private RechargeSuccessfulNoticeService rechargeSuccessfulNoticeService;

    @Autowired
    private TtRechargeProdMapper rechargeListMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private TtPromotionLevelMapper ttPromotionLevelMapper;

    @Autowired
    private TtFirstRechargeMapper ttFirstRechargeMapper;

    @Override
    public R pay(CreateOrderParam param, TtUser ttUser, String ip) {
        // 查询商品信息
        TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeProdMapper)
                .eq(TtRechargeProd::getId, param.getGoodsId())
                .eq(TtRechargeProd::getStatus, 0)
                .one();
        if (ObjectUtil.isEmpty(goods)) {
            return R.fail("不存在的商品。");
        }
        if (goods.getPrice().compareTo(param.getGoodsPrice()) != 0) {
            return R.fail("商品价格不一致。");
        }

        // 计算总价值
        BigDecimal totalAmount = param.getGoodsPrice().multiply(new BigDecimal(param.getGoodsNum()));

        // 构建支付
        String serverUrl = aliProperties.getServerUrl();
        String appId = aliProperties.getMerchants().get(0).getAppId();
        String privateKey = aliProperties.getMerchants().get(0).getPrivateKey();
        String format = aliProperties.getFormat();
        String charset = aliProperties.getCharset();
        String alipayPublicKey = aliProperties.getMerchants().get(0).getAlipayPublicKey();
        String signType = aliProperties.getSignType();
        String notifyUrl = aliProperties.getNotifyUrl();

        // 初始化SDK
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, appId, privateKey, format, charset, alipayPublicKey, signType);

        // 构造请求参数以调用接口
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();

        // 设置通知地址
        request.setNotifyUrl(notifyUrl);

        // 设置订单标题
        model.setSubject(goods.getName());
        // 设置商户订单号
        String orderId = String.valueOf(IdUtil.getSnowflake(1, 1).nextId());
        model.setOutTradeNo(orderId);
        // 设置订单总金额
        model.setTotalAmount(totalAmount.toString());
        request.setBizModel(model);

        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                // 创建订单
                TtOrder order = new TtOrder();
                order.setOrderId(orderId);
                order.setOutTradeNo(response.getOutTradeNo());
                order.setUserId(ttUser.getUserId());
                order.setType(PayType.ZFB.getCode());
                order.setGoodsId(param.getGoodsId());
                order.setGoodsPrice(param.getGoodsPrice());
                order.setGoodsNum(param.getGoodsNum());
                order.setTotalAmount(totalAmount);
                order.setStatus(PayOrderStatus.NO_PAY.getCode());
                order.setCreateTime(new Date());
                orderMapper.insert(order);
                JSONObject jsonObject =  JSON.parseObject(response.getBody());
                return R.ok(jsonObject);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return R.fail("支付失败");
    }

    @Override
    public String callBack(Map<String, String> params) {
        log.info("支付成功通知");
        String result = "failure";
        String alipayPublicKey = aliProperties.getMerchants().get(0).getAlipayPublicKey();
        try {
            // 异步通知校验
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey,
                    AlipayConstants.CHARSET_UTF8,
                    AlipayConstants.SIGN_TYPE_RSA2);
            if (!signVerified) {
                log.error("支付成功,异步通知验签失败!");
                return result;
            }
            // 进行二次校验
            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                log.error("交易失败");
                return result;
            }
            // 处理业务
            // 查询订单信息
            String outTradeNo = params.get("out_trade_no");
            TtOrder order = new LambdaQueryChainWrapper<>(orderMapper)
                    .eq(TtOrder::getOutTradeNo, outTradeNo)
                    .eq(TtOrder::getStatus, PayOrderStatus.NO_PAY.getCode())
                    .one();
            // 防止重复通知
            if (!PayOrderStatus.NO_PAY.getCode().equals(order.getStatus())) {
                return result;
            }
            // 查询用户信息
            TtUser user = new LambdaQueryChainWrapper<>(userMapper)
                    .eq(TtUser::getUserId, order.getUserId())
                    .eq(TtUser::getDelFlag, 0)
                    .one();
            // 查询商品信息
            TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeListMapper)
                    .eq(TtRechargeProd::getId, order.getGoodsId())
                    .eq(TtRechargeProd::getStatus, 0)
                    .one();
            // 账户结算
            if (ObjectUtil.isNull(goods.getProductA())) goods.setProductA(BigDecimal.ZERO);
            if (ObjectUtil.isNull(goods.getProductC())) goods.setProductC(BigDecimal.ZERO);
            payNotifyAccounting(order, user, goods, order.getGoodsNum());
            // 首充赠送
            firstChargeGiftAmount(user, goods, order.getGoodsNum());
            // 推广等级充值赠送
            promotionLevelChargeGiftAmount(user, goods, order.getGoodsNum());
            // 更新订单
            boolean update = new LambdaUpdateChainWrapper<>(orderMapper)
                    .eq(TtOrder::getId, order.getId())
                    .set(TtOrder::getStatus, PayOrderStatus.PAY_COMPLE.getCode())
                    .set(TtOrder::getUpdateTime, new Date())
                    .update();
            if (update) {
                result = "success";
            }

            // 发送充值成功通知
            rechargeSuccessfulNoticeService.sendRechargeSuccessNotice(order.getUserId().toString(), order.getGoodsPrice());
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
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
