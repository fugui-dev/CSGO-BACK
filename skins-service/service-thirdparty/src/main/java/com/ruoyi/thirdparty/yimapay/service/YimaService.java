package com.ruoyi.thirdparty.yimapay.service;

/*
 * @description
 * @date 2025/6/12
 */

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.*;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.domain.common.constant.PayOrderStatus;
import com.ruoyi.domain.common.constant.PayType;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.dto.yima.YimaPayAddOrderResponse;
import com.ruoyi.domain.dto.yima.YimaPayNotifyRequest;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.TtPromotionLevel;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.domain.other.TtRechargeRecord;
import com.ruoyi.domain.other.TtVipLevel;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.MaYi.service.MYService;
import com.ruoyi.thirdparty.MaYi.utils.signUtil;
import com.ruoyi.thirdparty.baiduPromotion.BdPromotionProcess;
import com.ruoyi.thirdparty.common.service.RechargeSuccessfulNoticeService;
import com.ruoyi.thirdparty.unifypaycallbackprocess.UnifyPayPerOrderProcess;
import com.ruoyi.thirdparty.yimapay.config.YimaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ruoyi.admin.config.RedisConstants.NOTIFY_PAY;
import static com.ruoyi.admin.config.RedisConstants.VIP_ANCHOR_EXPERIENCE_KEY;

@Service
@Slf4j
public class YimaService {

    @Autowired
    YimaConfig yimaConfig;

    @Autowired
    TtOrderMapper orderMapper;

    @Autowired
    TtUserService userService;

    @Autowired
    TtUserMapper userMapper;

    @Autowired
    private TtRechargeProdMapper rechargeProdMapper;

    @Autowired
    private MYService myService;

    @Autowired
    private RechargeSuccessfulNoticeService rechargeSuccessfulNoticeService;

    @Autowired
    private TtVipLevelService vipLevelService;

    @Autowired
    private TtUserBlendErcashService userBlendErcashService;

    @Autowired
    private TtRechargeRecordMapper rechargeRecordMapper;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private BdPromotionProcess bdPromotionProcess;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private TtPromotionLevelService promotionLevelService;

    @Autowired
    private TtPromotionRecordMapper promotionRecordMapper;

    @Autowired
    private TtBonusService bonusService;


    @Autowired
    private TtPromotionLevelMapper ttPromotionLevelMapper;

    @Autowired
    UnifyPayPerOrderProcess unifyPayPerOrderProcess;
    @Autowired
    private TtFirstRechargeMapper ttFirstRechargeMapper;

    @Autowired
    private TtRechargeProdMapper rechargeListMapper;

    @Autowired
    private TtUserBlendErcashMapper userBlendErcashMapper;

    // 构建基本请求信息
    private Map<String, String> buildBaseParam(Map<String, String> map, HttpServletRequest request) {
        map.put("app_id", yimaConfig.getAppId());

        return map;
    }

    //预订单
    public R preOrder(CreateOrderParam param, TtUser user, HttpServletRequest request) {

        // 查询商品信息
        TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeProdMapper)
                .eq(TtRechargeProd::getId, param.getGoodsId())
                .eq(TtRechargeProd::getStatus, 0)
                .one();
        if (ObjectUtil.isEmpty(goods)) return R.fail("不存在的商品。");
        if (goods.getPrice().compareTo(param.getGoodsPrice()) != 0) return R.fail("商品价格不一致。");

        // 总价值
        BigDecimal totalAmount = param.getGoodsPrice().multiply(new BigDecimal(param.getGoodsNum()))
                .multiply(BigDecimal.valueOf(100))
                ;

        // 构建完整参数
        Map<String, String> map = buildBaseParam(new HashMap<>(), request);
        map.put("pay_type",param.getPayType());
        map.put("description",goods.getName());
        map.put("amount",totalAmount.toString());
        map.put("client_ip", IpUtils.getIpAddr());
        map.put("notify_url", yimaConfig.getCallBackUrl() + "/api/yimaPay/notify");

        map.put("time_expire",String.valueOf(5));
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        String orderId = String.valueOf(snowflake.nextId());
        map.put("out_trade_no", orderId);
        //微信JSAPI必传，其他部分渠道需要使用openid的
//        map.put("openid", );

        String sign = signUtil.getSignOfYima(map,null, yimaConfig.getApiKey());
        map.put("sign",sign);

        log.info("yima创建订单参数:{}",map.toString());

        // 发送请求
        HttpRequest post = HttpUtil.createPost(yimaConfig.getGateway() + YimaConfig.ApiPayAddOrder);
        post.header("Content-Type","application/x-www-form-urlencoded");
        post.formStr(map);
        HttpResponse res = post.execute();

        // 解析响应
        YimaPayAddOrderResponse resBody = JSONUtil.toBean(res.body(), YimaPayAddOrderResponse.class);
        if (!resBody.getResultCode().equals("200")) {
            return R.fail(resBody);
        }

        // 创建订单
        TtOrder order = new TtOrder();
        order.setOrderId(orderId);
        order.setOutTradeNo(resBody.getData().getOut_trade_no());

        order.setUserId(user.getUserId());
        order.setType(PayType.YM_PAY.getCode());

        order.setGoodsId(param.getGoodsId());
        order.setGoodsPrice(param.getGoodsPrice());
        order.setGoodsNum(param.getGoodsNum());
        order.setTotalAmount(totalAmount);

        order.setSign(sign);
        order.setStatus(PayOrderStatus.NO_PAY.getCode());

        // 生成
        order.setPayUrl(resBody.getData().getBody());

        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        orderMapper.insert(order);
        return R.ok(order);
    }

    public String payNotify(YimaPayNotifyRequest data) {
        TtOrder order = null;
        // TODO:  最好再查询一下第三方平台的订单信息
        try {
            // 查询订单信息
            order = new LambdaQueryChainWrapper<>(orderMapper)
                    .eq(TtOrder::getOutTradeNo, data.getOut_trade_no())
                    .eq(TtOrder::getStatus, PayOrderStatus.NO_PAY.getCode())
                    .one();

           /* if (ObjectUtil.isEmpty(order)) {
                log.warn("支付回调异常，不存在的有效订单。");
                return "FAIL";
            }

            // 查询用户信息
            TtUser user = new LambdaQueryChainWrapper<>(userMapper)
                    .eq(TtUser::getUserId, order.getUserId())
                    .eq(TtUser::getDelFlag, 0)
                    .one();
            if (ObjectUtil.isEmpty(user)) {
                log.warn("支付回调异常，不存在的有效用户。");
                return "FAIL";
            }

            if (order.getStatus().equals( PayOrderStatus.PAY_COMPLE.getCode()) || order.getStatus().equals(PayOrderStatus.PAY_YET.getCode())) {
                log.warn("重复的回调！该订单已完成。");
                return "SUCCESS";
            }

            // 查询商品信息
            TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeListMapper)
                    .eq(TtRechargeProd::getId, order.getGoodsId())
                    .eq(TtRechargeProd::getStatus, 0)
                    .one();
            if (ObjectUtil.isEmpty(goods)) {
                log.warn("支付回调异常，不存在的商品。");
                return "FAIL";
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
*/
            // 发送充值成功通知
            notifyProcess(order.getOrderId(), false);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();

            log.warn("yima聚合支付，回调异常");

            if (ObjectUtil.isNotNull(order)) {
                new LambdaUpdateChainWrapper<>(orderMapper)
                        .eq(TtOrder::getId, order.getId())
                        .set(TtOrder::getStatus, PayOrderStatus.CALL_BACK_ERRO.getCode())
                        .set(TtOrder::getUpdateTime, new Date())
                        .update();
            }
            return "FAIL";
        }
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

    public R<Boolean> orderStatus(String orderNo) {
        TtOrder order = orderMapper.selectOne(Wrappers.lambdaQuery(TtOrder.class)
                .eq(TtOrder::getOrderId, orderNo));

        //如果是主播用户，假充值
        Long userId = SecurityUtils.getUserId();
        TtUser user = userService.getById(userId);
        if (user.getUserType().equals("01") && user.getAutoRecharge() == 1){
            //至少订单五秒后可以支付成功
            if (System.currentTimeMillis() - order.getCreateTime().getTime() > 5 * 1000){
                //处理充值逻辑
                notifyProcess(orderNo, true);
            }
        }

        if (order == null || (! order.getStatus().equals(PayOrderStatus.PAY_COMPLE.getCode()))){
            return R.ok(false);
        }

        return R.ok(true);

    }



    /**
     * 回调处理，调用改方法之前已经以前和向支付方查询订单
     * @param orderNo 系统订单号，非支付方订单号
     * @return
     */
    public String notifyProcess(String orderNo, Boolean isAnchorVirtual){
        log.info("yima开始处理订单支付状态==>【{}】", orderNo);
        String lockKey = NOTIFY_PAY + "order_id:" + orderNo;
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return "FAIL";
        }

        log.info("yima获得锁，开始处理订单支付状态==>【{}】", orderNo);

        //处理订单
        TtOrder order = null;
        // 在该方法开始前已经向支付方查询过订单
        try {
            // 查询订单信息
            order = new LambdaQueryChainWrapper<>(orderMapper)
                    .eq(TtOrder::getOrderId, orderNo)
                    .eq(TtOrder::getStatus, PayOrderStatus.NO_PAY.getCode())
                    .one();

            if (ObjectUtil.isNull(order)) {
                log.warn("yima不存在待支付的订单状态！");
                return "FAIL";
            }
            if (isAnchorVirtual){ //如果是主播虚拟充值
                order.setAnchorVirtual(1);
                order.setOutTradeNo("");
            }

            // 查询用户信息
            TtUser user = new LambdaQueryChainWrapper<>(userMapper)
                    .eq(TtUser::getUserId, order.getUserId())
                    .eq(TtUser::getDelFlag, 0)
                    .one();
            if (ObjectUtil.isEmpty(user)) {
                log.warn("yima支付回调异常，不存在的有效用户。");
                return "FAIL";
            }

            if (order.getStatus().equals(PayOrderStatus.PAY_COMPLE.getCode()) || order.getStatus().equals(PayOrderStatus.PAY_YET.getCode())) {
                log.warn("重复的回调！该订单已完成。");
                return "SUCCESS";
            }

            // 查询商品信息
            TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeProdMapper)
                    .eq(TtRechargeProd::getId, order.getGoodsId())
                    .eq(TtRechargeProd::getStatus, 0)
                    .one();
            if (ObjectUtil.isEmpty(goods)) {
                log.warn("yima支付回调异常，不存在的商品。");
                return "FAIL";
            }

            // 账户结算
            if (ObjectUtil.isNull(goods.getProductA())) goods.setProductA(BigDecimal.ZERO);
            if (ObjectUtil.isNull(goods.getProductC())) goods.setProductC(BigDecimal.ZERO);
            payNotifyAccounting(order, user, goods, order.getGoodsNum());

            // 首充赠送
            log.warn("yima处理首充逻辑==>【{}】", orderNo);
            Boolean firstRechargeFlag = firstChargeGiftAmount(user, goods, order.getGoodsNum());

            // 推广等级充值赠送
            promotionLevelChargeGiftAmount(user, goods, order.getGoodsNum());

            //累加充值金额，累积VIP等级，VIP等级红包累加
            log.warn("yima处理VIP返利和红包逻辑==>【{}】", orderNo);
            addTotalRecharge(order, user, goods);

            // 更新订单
            boolean update = new LambdaUpdateChainWrapper<>(orderMapper)
                    .eq(TtOrder::getId, order.getId())
                    .set(TtOrder::getStatus, PayOrderStatus.PAY_COMPLE.getCode())
                    .set(TtOrder::getUpdateTime, new Date())
                    .update();

            log.warn("yima订单状态更新为已支付【{}】==>【{}】", update, orderNo);

            // 发送充值成功通知
            rechargeSuccessfulNoticeService.sendRechargeSuccessNotice(user.getUserId().toString(), order.getGoodsPrice());

            //如果是首充，同步百度营销
//            if (firstRechargeFlag){
//                TtOrder finalOrder = order;
//                threadPoolTaskExecutor.execute(()->{
//                    log.info("订单【{}】为首充，向百度推广数据==>", orderNo);
//                    bdPromotionProcess.firstRecharge(finalOrder);
//                });
//            }

            log.info("yima统一回调处理完成，订单号==>【{}】", orderNo);
            redisLock.unlock(lockKey);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();

            log.warn("yima支付回调处理异常");
            redisLock.unlock(lockKey);

            if (ObjectUtil.isNotNull(order)) {
                new LambdaUpdateChainWrapper<>(orderMapper)
                        .eq(TtOrder::getId, order.getId())
                        .set(TtOrder::getStatus, PayOrderStatus.CALL_BACK_ERRO.getCode())
                        .set(TtOrder::getUpdateTime, new Date())
                        .update();
            }
            return "FAIL";
        }

    }


    private void addTotalRecharge(TtOrder order, TtUser user, TtRechargeProd goods) {

        //本次充值金额
        BigDecimal rechargeMoney = goods.getProductA().multiply(new BigDecimal(order.getGoodsNum()));
        BigDecimal rechargeMoneyBak = goods.getProductA().multiply(new BigDecimal(order.getGoodsNum()));

        //充值记录前面已经保存了
//        zancun = initRecord(user, rechargeMoney, user.getAccountAmount(), TtAccountRecordSource.RECHARGE);

        //充值后总充值金额
        BigDecimal totalAmount = rechargeMoney.add(user.getTotalRecharge() != null ? user.getTotalRecharge() : BigDecimal.valueOf(0));

        //暂存金额
        BigDecimal zancun = rechargeMoney.add(user.getTotalRecharge() != null ? user.getTotalRecharge() : BigDecimal.valueOf(0));

        //查询用户充值后的下一个vip等级
        TtVipLevel afterLevel = vipLevelService.getOne(Wrappers.lambdaQuery(TtVipLevel.class)
                .gt(TtVipLevel::getRechargeThreshold, totalAmount)
                .orderByAsc(TtVipLevel::getRechargeThreshold)
                .last("limit 1"));

        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();

        //查询充值后的VIP等级
        if (afterLevel != null){
            int vipLevelId = afterLevel.getId() - 1;
            if (vipLevelId <= 1) vipLevelId = 1; //确保最低等级为1
            TtVipLevel currentLeven = vipLevelService.getById(vipLevelId); //充值后处于的VIP等级

            //等级返利（设置返利不为0时）
            if (!BigDecimal.valueOf(0).equals(currentLeven.getCommissions())){
                BigDecimal rebate = currentLeven.getCommissions()
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                        .multiply(rechargeMoney).setScale(2, RoundingMode.HALF_UP);
                //返利收入记录
                zancun = initRecord(user, rebate, zancun, TtAccountRecordSource.VIP_LEVEL_FANXIAN);

                rechargeMoney = rechargeMoney.add(rebate);
            }

            //红包累加（等级有升级）
            if (currentLeven.getId().compareTo(user.getVipLevel()) > 0){
                //从VIP几开始，升级可以获得十次主播爆率
                Integer vipAnchorExperienceStart = Integer.valueOf(sysConfigService.selectConfigByKey("vipAnchorExperienceStart"));
                if (currentLeven.getId().compareTo(vipAnchorExperienceStart) >= 0){
                    redisCache.setCacheObject(VIP_ANCHOR_EXPERIENCE_KEY + user.getUserId(), 10);
                }

                //红包累加（设置红包不为0时）
                if (!BigDecimal.valueOf(0).equals(currentLeven.getAddedBonus())){

                    //红包收入记录
                    initRecord(user, currentLeven.getAddedBonus(), zancun, TtAccountRecordSource.VIP_LEVEL_RED_PACK);

                    rechargeMoney = rechargeMoney.add(currentLeven.getAddedBonus());

                    userUpdate.setSql("vip_level = " + currentLeven.getId());
                }
            }

        }

        //更新最终用户金额
        userUpdate
                .eq(TtUser::getUserId, user.getUserId())
                .setSql("account_amount = account_amount +" + rechargeMoney.subtract(rechargeMoneyBak)) //之前的方法已经累加过充值了，这里需要减去
                .setSql("total_recharge = total_recharge + " + rechargeMoneyBak);


        userService.update(userUpdate);


        TtUser userLast = userService.selectTtUserById(user.getUserId().longValue());

        //保存充值记录
        try {
            insertRechargeRecord(order, rechargeMoneyBak, userLast);
        }catch (Exception e){
            log.error("yima保存充值记录失败，但没有影响订单流程处理===>【{}】", order, e);
        }

    }

    private void insertRechargeRecord(TtOrder order, BigDecimal rechargeMoneyBak, TtUser userLast) {
        TtRechargeRecord ttRechargeRecord = TtRechargeRecord.builder().build();
        ttRechargeRecord.setUserId(userLast.getUserId());
        ttRechargeRecord.setParentId(userLast.getParentId());
        ttRechargeRecord.setArrivalAmount(rechargeMoneyBak);
        ttRechargeRecord.setAmountActuallyPaid(rechargeMoneyBak);
        ttRechargeRecord.setFinallyPrice(userLast.getAccountAmount());
        ttRechargeRecord.setOrderId(order.getOrderId());
        ttRechargeRecord.setOutTradeNo(order.getOutTradeNo());
        ttRechargeRecord.setStatus("0");
        ttRechargeRecord.setChannelType("1"); //写死支付宝
        ttRechargeRecord.setCreateTime(DateUtils.getNowDate());
        ttRechargeRecord.setBdPromotionChannelId(userLast.getBdChannelId());
        if (order.getAnchorVirtual() != null && order.getAnchorVirtual() == 1){ //如果是主播虚拟充值
            ttRechargeRecord.setAnchorVirtual(1);
        }

        int insert = rechargeRecordMapper.insert(ttRechargeRecord);

        log.info("yima保存充值日志成功？【{}】记录：【{}】", insert, ttRechargeRecord);

        //上级用户推广福利（订单不是主播虚拟充值）
        if (!Objects.isNull(userLast.getParentId()) && order.getAnchorVirtual() != 1) {
            TtUser parentUser = userService.getById(userLast.getParentId());
            if (bonusService.insertParentUserPromotionRecord(userLast.getUserId(), parentUser, ttRechargeRecord.getAmountActuallyPaid(), ttRechargeRecord.getId())) {
                bonusService.updateParentUserPromotionLevel(parentUser);
            }
        }
    }

    private BigDecimal initRecord(TtUser user, BigDecimal addMoney, BigDecimal beforeMoney, TtAccountRecordSource recharge) {
        TtUserBlendErcash record = TtUserBlendErcash.builder()
                .userId(user.getUserId())
                .amount(addMoney.compareTo(BigDecimal.ZERO) > 0 ? addMoney : null)
                .finalAmount(addMoney.compareTo(BigDecimal.ZERO) > 0 ? beforeMoney.add(addMoney) : null)
                .total(addMoney)
                .type(TtAccountRecordType.INPUT.getCode())
                .source(recharge.getCode())
                .remark(recharge.getMsg())
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();

        boolean save = userBlendErcashService.save(record);

        log.info("yima保存消费日志成功？【{}】，添加金额【{}】，本次记录金额 【{}】，类型：【{}】", save, addMoney, record.getFinalAmount(), recharge.getMsg());

        return record.getFinalAmount();

    }
}
