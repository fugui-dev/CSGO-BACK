package com.ruoyi.thirdparty.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.config.RedisConstants;
import com.ruoyi.admin.mapper.TtUserBlendErcashMapper;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.other.TtRechargeCard;
import com.ruoyi.domain.other.TtRechargeRecord;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.mapper.TtRechargeCardMapper;
import com.ruoyi.admin.mapper.TtRechargeRecordMapper;
import com.ruoyi.admin.service.TtBonusService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.thirdparty.common.service.ApiRechargeService;
import com.ruoyi.thirdparty.common.service.RechargeSuccessfulNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ApiRechargeServiceImpl implements ApiRechargeService {

    private final RedisLock redisLock;
    private final TtBonusService bonusService;
    private final TtUserService userService;
    private final TtRechargeCardMapper rechargeCardMapper;
    private final TtRechargeRecordMapper rechargeRecordMapper;

    private final TtUserBlendErcashMapper userBlendErcashMapper;

    public ApiRechargeServiceImpl(RedisLock redisLock,
                                  TtBonusService bonusService,
                                  TtUserService userService,
                                  TtRechargeCardMapper rechargeCardMapper,
                                  TtRechargeRecordMapper rechargeRecordMapper,
                                  TtUserBlendErcashMapper userBlendErcashMapper) {
        this.redisLock = redisLock;
        this.bonusService = bonusService;
        this.userService = userService;
        this.rechargeCardMapper = rechargeCardMapper;
        this.rechargeRecordMapper = rechargeRecordMapper;
        this.userBlendErcashMapper = userBlendErcashMapper;
    }

    @Autowired
    private RechargeSuccessfulNoticeService rechargeSuccessfulNoticeService;

    @Override
    @Transactional
    public String cardPay(String password, TtUser ttUser) {
        while (true) {
            Boolean cardPayLock = redisLock.tryLock(RedisConstants.CARD_PAY_LOCK + ttUser.getParentId(), 3L, 10L, TimeUnit.SECONDS);
            if (cardPayLock) {
                try {
                    TtRechargeCard ttRechargeCard = new LambdaQueryChainWrapper<>(rechargeCardMapper).eq(TtRechargeCard::getPassword, password)
                            .eq(TtRechargeCard::getStatus, "0").eq(TtRechargeCard::getDelFlag, "0").one();
                    if (StringUtils.isNull(ttRechargeCard)) return "卡密已被使用！";
                    ttRechargeCard.setStatus("1");
                    ttRechargeCard.setUseUserId(ttUser.getUserId());
                    ttRechargeCard.setUseTime(DateUtils.getNowDate());
                    ttRechargeCard.setUpdateBy(ttUser.getUserName());
                    ttRechargeCard.setUpdateTime(DateUtils.getNowDate());
                    if (rechargeCardMapper.updateById(ttRechargeCard) > 0) {
                        BigDecimal totalAmount = ttRechargeCard.getPrice();
                        ttUser.setAccountAmount(ttUser.getAccountAmount().add(totalAmount));
                        userService.updateById(ttUser);
                        userService.insertUserAmountRecords(ttUser.getUserId(), TtAccountRecordType.INPUT, TtAccountRecordSource.RECHARGE, totalAmount, ttUser.getAccountAmount());
                        TtRechargeRecord ttRechargeRecord = TtRechargeRecord.builder().build();
                        ttRechargeRecord.setUserId(ttUser.getUserId());
                        ttRechargeRecord.setParentId(ttUser.getParentId());
                        ttRechargeRecord.setArrivalAmount(totalAmount);
                        ttRechargeRecord.setAmountActuallyPaid(totalAmount);
                        ttRechargeRecord.setFinallyPrice(ttUser.getAccountAmount());
                        ttRechargeRecord.setOrderId(password);
                        ttRechargeRecord.setStatus("0");
                        ttRechargeRecord.setChannelType("0");
                        ttRechargeRecord.setCreateTime(DateUtils.getNowDate());
                        if (rechargeRecordMapper.insert(ttRechargeRecord) > 0) {
                            bonusService.bonus(ttUser.getUserId(), ttRechargeRecord.getId());
                        }

                        //写入记录
                        TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                                .userId(ttUser.getUserId())
                                .amount(totalAmount.compareTo(BigDecimal.ZERO) > 0 ? totalAmount : null)
                                .finalAmount(totalAmount.compareTo(BigDecimal.ZERO) > 0 ? ttUser.getAccountAmount().add(totalAmount) : null)
                                .total(totalAmount)
                                .type(TtAccountRecordType.INPUT.getCode())
                                .source(TtAccountRecordSource.RECHARGE.getCode())
                                .remark(TtAccountRecordSource.RECHARGE.getMsg())
                                .createTime(new Timestamp(System.currentTimeMillis()))
                                .build();
                        userBlendErcashMapper.insert(blendErcash);

                        //累加用户累计充值
                        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
                        userUpdate
                                .eq(TtUser::getUserId, ttUser.getUserId())
                                .setSql("total_recharge = total_recharge + " + totalAmount);
                        userService.update(userUpdate);


                        // 发送充值成功通知
                        rechargeSuccessfulNoticeService.sendRechargeSuccessNotice(ttUser.getUserId().toString(),
                                totalAmount);

                        return "";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "数据异常！";
                } finally {
                    redisLock.unlock(RedisConstants.CARD_PAY_LOCK + ttUser.getParentId());
                }
            } else {
                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (Exception e) {
                    return "数据异常！";
                }
            }
        }
    }
}
