package com.ruoyi.thirdparty.jiujia.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.ruoyi.domain.common.constant.*;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.mapper.TtOrderMapper;
import com.ruoyi.admin.mapper.TtRechargeProdMapper;
import com.ruoyi.admin.mapper.TtRechargeRecordMapper;
import com.ruoyi.admin.service.TtBonusService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.thirdparty.common.service.RechargeSuccessfulNoticeService;
import com.ruoyi.thirdparty.jiujia.config.JiuJiaProperties;
import com.ruoyi.thirdparty.jiujia.domain.*;
import com.ruoyi.thirdparty.jiujia.service.JiuJiaPayService;
import com.ruoyi.thirdparty.jiujia.util.JiuJiaUtils;
import com.ruoyi.domain.other.CreateOrderParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@EnableConfigurationProperties(value = JiuJiaProperties.class)
@Slf4j
public class JiuJiaPayServiceImpl implements JiuJiaPayService {

    private final TtBonusService bonusService;
    private final JiuJiaProperties jiuJiaProperties;
    private final ThreadPoolExecutor customThreadPoolExecutor;
    private final TtRechargeProdMapper rechargeListMapper;
    private final TtOrderMapper orderMapper;
    private final TtUserService userService;
    private final TtRechargeRecordMapper rechargeRecordMapper;

    public JiuJiaPayServiceImpl(TtBonusService bonusService,
                                JiuJiaProperties jiuJiaProperties,
                                ThreadPoolExecutor customThreadPoolExecutor,
                                TtRechargeProdMapper rechargeListMapper,
                                TtOrderMapper orderMapper,
                                TtUserService userService,
                                TtRechargeRecordMapper rechargeRecordMapper) {

        this.bonusService = bonusService;
        this.jiuJiaProperties = jiuJiaProperties;
        this.customThreadPoolExecutor = customThreadPoolExecutor;
        this.rechargeListMapper = rechargeListMapper;
        this.orderMapper = orderMapper;
        this.userService = userService;
        this.rechargeRecordMapper = rechargeRecordMapper;
    }

    @Autowired
    private RechargeSuccessfulNoticeService rechargeSuccessfulNoticeService;

    @Override
    public String createPay(CreateOrderParam createOrderBody, TtUser ttUser) {

        Integer goodsId = createOrderBody.getGoodsId();
        BigDecimal goodsPrice = createOrderBody.getGoodsPrice().setScale(2, RoundingMode.HALF_UP);
        Integer goodsNum = createOrderBody.getGoodsNum();
        BigDecimal totalAmount = goodsPrice.multiply(new BigDecimal(goodsNum)).setScale(2, RoundingMode.HALF_UP);

        // 本站充值产品
        TtRechargeProd ttRechargeProd = rechargeListMapper.selectById(goodsId);
        if (goodsPrice.compareTo(ttRechargeProd.getPrice()) != 0) return "客户端传参产品价格不一致异常！";
        if (ttRechargeProd.getPrice().multiply(new BigDecimal(goodsNum)).compareTo(totalAmount) != 0)
            return "客户端传参产品价格不一致异常！";

        // 是否有购买相同商品并未超时的订单
        TtOrder order = new LambdaQueryChainWrapper<>(orderMapper)
                .eq(TtOrder::getUserId, ttUser.getUserId())
                .eq(TtOrder::getThirdParty, PartyType.JIU_JIA_ZFB.getCode())
                .eq(TtOrder::getType, PayType.JIU_JIA_ZFB.getCode())
                .eq(TtOrder::getGoodsId, goodsId)
                .eq(TtOrder::getGoodsPrice, goodsPrice)
                .eq(TtOrder::getGoodsNum, goodsNum)
                .eq(TtOrder::getTotalAmount, totalAmount)
                .eq(TtOrder::getStatus, PayOrderStatus.NO_PAY.getCode())
                .one();

        if (ObjectUtil.isNotNull(order)) {

            // 已存在相同订单
            Date orderCreateTime = order.getCreateTime();
            DateTime timeout = DateUtil.offset(orderCreateTime, DateField.MINUTE, 5);
            if (DateUtils.getNowDate().compareTo(DateUtil.date(timeout)) < 0) {
                // 订单未超时直接返回支付url
                return order.getPayUrl();
            } else {
                // 超时
                // order.setStatus(PayOrderStatus.CANCEL.getCode());
                // order.setUpdateTime(DateUtils.getNowDate());
                // orderMapper.updateById(order);
                new LambdaUpdateChainWrapper<>(orderMapper)
                        .eq(TtOrder::getId,order.getId())
                        .eq(TtOrder::getOrderId,order.getOrderId())
                        .set(TtOrder::getStatus,PayOrderStatus.CANCEL.getCode())
                        .set(TtOrder::getUpdateTime,DateUtils.getNowDate())
                        .update();
                // if (orderMapper.updateById(order) > 0) {
                //     log.info("订单超时已关闭！");
                // }else {
                //     return "订单超时关闭失败，请联系管理员！";
                // }
            }

        }

        // 初始化预下单请求参数
        String orderId = IdUtils.fastSimpleUUID().toUpperCase().substring(0, 20);
        OrderBody orderBody = new OrderBody(totalAmount, orderId);
        String sign = JiuJiaUtils.createSign(jiuJiaProperties, orderBody);

        // CreateOrderRequestParam requestParam = CreateOrderRequestParam.builder().build();
        CreateOrderRequestParam apiParam = new CreateOrderRequestParam();
        apiParam.setAppKey(jiuJiaProperties.getAppKey());
        apiParam.setApiDomain(jiuJiaProperties.getApiDomain());
        apiParam.setSubject(JiuJiaUtils.createSubject() + "*" + goodsNum);
        apiParam.setPayType(Integer.valueOf(PayType.JIU_JIA_ZFB.getCode()));
        apiParam.setGoodsId(goodsId);
        apiParam.setGoodsPrice(goodsPrice);
        apiParam.setGoodsNum(goodsNum);
        apiParam.setOrderId(orderId);
        apiParam.setTotalAmount(totalAmount);
        apiParam.setMemberId(jiuJiaProperties.getMemberId());
        apiParam.setCallbackUrl(jiuJiaProperties.getCallbackUrl());
        apiParam.setSign(sign);
        apiParam.setUserIp(IpUtils.getIpAddr());
        String jsonParam = JSONObject.toJSONString(apiParam);

        // 请求api
        String result = HttpUtils.sendPostJSONString(jiuJiaProperties.getAliPayUrl() + "/create_pay", jsonParam);

        CreateOrderResponseResult responseResult = JSONObject.parseObject(result, CreateOrderResponseResult.class);
        if (responseResult.getCode() > 0) return responseResult.getMsg();
        if (responseResult.getCode() < 0) return "创建订单异常，请联系管理员！";

        // 构建新订单
        TtOrder ttOrder = new TtOrder();
        ttOrder.setUserId(ttUser.getUserId());
        ttOrder.setThirdParty(String.valueOf(PartyType.JIU_JIA_ZFB.getCode()));
        ttOrder.setType(PayType.JIU_JIA_ZFB.getCode());
        ttOrder.setGoodsId(goodsId);
        ttOrder.setGoodsPrice(goodsPrice);
        ttOrder.setGoodsNum(goodsNum);
        ttOrder.setTotalAmount(totalAmount);
        ttOrder.setOrderId(orderId);
        ttOrder.setSign(sign);
        ttOrder.setStatus(PayOrderStatus.NO_PAY.getCode());
        ttOrder.setOutTradeNo(responseResult.getData());
        ttOrder.setPayUrl(responseResult.getUrl());
        ttOrder.setCreateTime(DateUtils.getNowDate());

        int isSuccess = orderMapper.insert(ttOrder);
        if (isSuccess <= 0) {
            return "创建订单异常，请联系管理员！";
        }

        // 发送充值成功通知
        rechargeSuccessfulNoticeService.sendRechargeSuccessNotice(ttUser.getUserId().toString(), order.getGoodsPrice());

        return responseResult.getUrl();
    }

    @Override
    public String callback(CallbackBody callbackBody) {

        String resultCode = callbackBody.getResult_code();
        if (!"success".equals(resultCode)) {
            log.error("九嘉回调出现异常，请联系客服！");
            return "fail";
        }

        String outTradeNo = callbackBody.getOut_trade_no();
        String orderId = callbackBody.getTrade_no();
        // 查询创建的订单
        TtOrder ttOrder = new LambdaQueryChainWrapper<>(orderMapper)
                .eq(TtOrder::getOutTradeNo, outTradeNo)
                .eq(TtOrder::getOrderId, orderId)
                .eq(TtOrder::getStatus, PayOrderStatus.NO_PAY.getCode())
                .one();

        if (StringUtils.isNull(ttOrder)) {
            log.error("不存在的订单。");
            return "fail";
        }

        // 查询用户
        TtUser user = new LambdaQueryChainWrapper<>(userService.getBaseMapper())
                .eq(TtUser::getUserId, ttOrder.getUserId())
                .eq(TtUser::getDelFlag, "0")
                .one();
        if (ObjectUtil.isNull(user)) {
            log.error("不存在的订单支付用户。");
            return "fail";
        }

        // 验签
        String sign = callbackBody.getSign();
        VisaVerificationBody vvBody = VisaVerificationBody.builder().build();
        vvBody.setTotalFee(String.valueOf(ttOrder.getTotalAmount()));
        vvBody.setResultCode(resultCode);
        vvBody.setTradeNo(ttOrder.getOrderId());
        vvBody.setOutTradeNo(ttOrder.getOutTradeNo());
        vvBody.setSign(sign);
        if (!JiuJiaUtils.visaVerification(jiuJiaProperties, vvBody)) {
            log.warn("九嘉支付回调，验签失败。");
            return "fail";
        }

        // 查询支付情况
        CheckOrderResponseData check = checkPay(outTradeNo);
        if (ObjectUtil.isNull(check)) {
            log.error("九嘉支付回调，订单支付情况复查无结果！");
            return "fail";
        }

        // 状态：TRADE_FINISHED、TRADE_SUCCESS、TRADE_CLOSED、WAIT_BUYER_PAY
        String status = check.getStatus();
        if (!status.equals("TRADE_SUCCESS") && !status.equals("TRADE_FINISHED")){
            ttOrder.setStatus(PayOrderStatus.PAY_FAIL.getCode());
            ttOrder.setUpdateTime(DateUtils.getNowDate());
            orderMapper.updateById(ttOrder);
            return "fail";
        }

        rechargeSuccess(ttOrder,user);

        return resultCode;
    }

    private CheckOrderResponseData checkPay(String outTradeNo) {

        // 查询订单
        CheckOrderRequestParam requestParam = CheckOrderRequestParam.builder()
                .appKey(jiuJiaProperties.getAppKey())
                .memberId(jiuJiaProperties.getMemberId())
                .outTradeNo(outTradeNo)
                .build();
        String jsonParam = JSONObject.toJSONString(requestParam);

        String result = HttpUtils.sendPostJSONString(jiuJiaProperties.getAliPayUrl() + "/check_pay", jsonParam);

        CheckOrderResponseResult<CheckOrderResponseData> responseResult =
                JSONObject.parseObject(result, new TypeReference<CheckOrderResponseResult<CheckOrderResponseData>>() {
                });

        return responseResult.getCode() == 0 ? responseResult.getData() : null;
    }

    private void rechargeSuccess(TtOrder ttOrder,TtUser user) {

        //String status = ttOrder.getStatus();
        ttOrder.setStatus(PayOrderStatus.PAY_YET.getCode());
        ttOrder.setUpdateTime(DateUtils.getNowDate());
        orderMapper.updateById(ttOrder);


        // TODO: 2024/4/12 这张表弃用
        // TtRechargeRecord rechargeRecord = new LambdaQueryChainWrapper<>(rechargeRecordMapper)
        //         .eq(TtRechargeRecord::getOrderId, ttOrder.getOrderId())
        //         .eq(TtRechargeRecord::getOutTradeNo, ttOrder.getOutTradeNo())
        //         .one();
        // if (StringUtils.isNotNull(rechargeRecord)) return;

        // 本站产品
        TtRechargeProd goods = rechargeListMapper.selectById(ttOrder.getGoodsId());

        // 更新用户账户
        BigDecimal prodA = goods.getProductA().multiply(new BigDecimal(ttOrder.getGoodsNum()));
        BigDecimal prodC = goods.getProductC().multiply(new BigDecimal(ttOrder.getGoodsNum()));
        LambdaUpdateWrapper<TtUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper
                .eq(TtUser::getUserId,user.getUserId())
                .setSql("account_amount = account_amount + "+prodA.toString()+
                        ",account_credits = account_credits + "+prodC.toString());
        userService.update(wrapper);

        user = userService.getById(user.getUserId());
        // 更新充值记录
        userService.insertUserAmountRecords(
                user.getUserId(),
                TtAccountRecordType.INPUT,
                TtAccountRecordSource.RECHARGE,
                prodA,
                user.getAccountAmount());
        userService.insertUserCreditsRecords(
                user.getUserId(),
                TtAccountRecordType.INPUT,
                TtAccountRecordSource.RECHARGE,
                prodC,
                user.getAccountCredits());
        // TtRechargeRecord ttRechargeRecord = TtRechargeRecord.builder().build();
        // ttRechargeRecord.setUserId(ttOrder.getUserId());
        // ttRechargeRecord.setParentId(user.getParentId());
        // ttRechargeRecord.setArrivalAmount(totalAmount);
        // ttRechargeRecord.setAmountActuallyPaid(totalAmount);
        // ttRechargeRecord.setFinallyPrice(user.getAccountAmount());
        // ttRechargeRecord.setOrderId(ttOrder.getOrderId());
        // ttRechargeRecord.setOutTradeNo(ttOrder.getOutTradeNo());
        // ttRechargeRecord.setStatus("0");
        // ttRechargeRecord.setChannelType("1");
        // ttRechargeRecord.setCreateTime(DateUtils.getNowDate());
        // if (rechargeRecordMapper.insert(ttRechargeRecord) > 0) {
        //
        //     // todo 奖励津贴？
        //     // bonusService.bonus(user.getUserId(), ttRechargeRecord.getId());
        // }
    }
}
