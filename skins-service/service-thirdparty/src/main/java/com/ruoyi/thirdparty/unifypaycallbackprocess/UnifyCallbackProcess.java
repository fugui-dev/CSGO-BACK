package com.ruoyi.thirdparty.unifypaycallbackprocess;

import cn.hutool.core.util.ObjectUtil;
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
import com.ruoyi.domain.common.constant.PayOrderStatus;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.entity.*;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtRechargeRecord;
import com.ruoyi.domain.other.TtVipLevel;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.MaYi.service.MYService;
import com.ruoyi.thirdparty.baiduPromotion.BdPromotionProcess;
import com.ruoyi.thirdparty.common.service.RechargeSuccessfulNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.ruoyi.admin.config.RedisConstants.*;


@Service
@Slf4j
public class UnifyCallbackProcess {

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


    /**
     * 回调处理，调用改方法之前已经以前和向支付方查询订单
     * @param orderNo 系统订单号，非支付方订单号
     * @return
     */
    public String notifyProcess(String orderNo){
        return notifyProcess(orderNo, false);
    }


    /**
     * 回调处理，调用改方法之前已经以前和向支付方查询订单
     * @param orderNo 系统订单号，非支付方订单号
     * @return
     */
    public String notifyProcess(String orderNo, Boolean isAnchorVirtual){
        log.info("开始处理订单支付状态==>【{}】", orderNo);
        String lockKey = NOTIFY_PAY + "order_id:" + orderNo;
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return "fail";
        }

        log.info("获得锁，开始处理订单支付状态==>【{}】", orderNo);

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
                log.warn("不存在待支付的订单状态！");
                return "fail";
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
                log.warn("支付回调异常，不存在的有效用户。");
                return "fail";
            }

//            if (order.getStatus().equals(PayOrderStatus.PAY_COMPLE.getCode()) || order.getStatus().equals(PayOrderStatus.PAY_YET.getCode())) {
//                log.warn("重复的回调！该订单已完成。");
//                return "success";
//            }

            // 查询商品信息
            TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeProdMapper)
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
            myService.payNotifyAccounting(order, user, goods, order.getGoodsNum());

            // 首充赠送
            log.warn("处理首充逻辑==>【{}】", orderNo);
            Boolean firstRechargeFlag = myService.firstChargeGiftAmount(user, goods, order.getGoodsNum());

            // 推广等级充值赠送
//            myService.promotionLevelChargeGiftAmount(user, goods, order.getGoodsNum());

            //累加充值金额，累积VIP等级，VIP等级红包累加
            log.warn("处理VIP返利和红包逻辑==>【{}】", orderNo);
            addTotalRecharge(order, user, goods);

            // 更新订单
            boolean update = new LambdaUpdateChainWrapper<>(orderMapper)
                    .eq(TtOrder::getId, order.getId())
                    .set(TtOrder::getStatus, PayOrderStatus.PAY_COMPLE.getCode())
                    .set(TtOrder::getUpdateTime, new Date())
                    .update();

            log.warn("订单状态更新为已支付【{}】==>【{}】", update, orderNo);

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

            log.info("统一回调处理完成，订单号==>【{}】", orderNo);
            redisLock.unlock(lockKey);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();

            log.warn("支付回调处理异常");
            redisLock.unlock(lockKey);

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
            log.error("保存充值记录失败，但没有影响订单流程处理===>【{}】", order, e);
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

        log.info("保存充值日志成功？【{}】记录：【{}】", insert, ttRechargeRecord);

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

        log.info("保存消费日志成功？【{}】，添加金额【{}】，本次记录金额 【{}】，类型：【{}】", save, addMoney, record.getFinalAmount(), recharge.getMsg());

        return record.getFinalAmount();

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
}
