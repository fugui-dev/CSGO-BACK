package com.ruoyi.user.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.ruoyi.admin.config.RedisConstants;
import com.ruoyi.admin.mapper.TtUserBlendErcashMapper;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.other.TtRedPacket;
import com.ruoyi.domain.other.TtRedPacketRecord;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.mapper.TtRedPacketMapper;
import com.ruoyi.admin.mapper.TtRedPacketRecordMapper;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.admin.util.RandomUtils;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.user.service.ApiBonusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ApiBonusServiceImpl implements ApiBonusService {

    private final TtUserService userService;
    private final TtRedPacketMapper redPacketMapper;
    private final TtRedPacketRecordMapper redPacketRecordMapper;
    private final RedisLock redisLock;

    public ApiBonusServiceImpl(TtUserService userService,
                               TtRedPacketMapper redPacketMapper,
                               TtRedPacketRecordMapper redPacketRecordMapper,
                               RedisLock redisLock) {
        this.userService = userService;
        this.redPacketMapper = redPacketMapper;
        this.redPacketRecordMapper = redPacketRecordMapper;
        this.redisLock = redisLock;
    }

    @Autowired
    private TtUserBlendErcashMapper userBlendErcashMapper;

    // 领红包检查
    public R<TtRedPacket> receiveRedPacketCheck(String code, TtUser ttUser) {

        if (StringUtils.isBlank(code)) return R.fail("口令错误。");

        TtRedPacket ttRedPacket = new LambdaQueryChainWrapper<>(redPacketMapper)
                .eq(TtRedPacket::getPassword, code)
                .eq(TtRedPacket::getDelFlag, 0)
                .one();
        if (ObjectUtil.isNull(ttRedPacket)) return R.fail("口令错误，没有合法的红包。");
        if (ttRedPacket.getStatus().equals(1)) return R.fail("红包已经抢光了。");

        List<TtRedPacketRecord> allRedPackRecord = new LambdaQueryChainWrapper<>(redPacketRecordMapper)
                .eq(TtRedPacketRecord::getRedPacketId, ttRedPacket.getId())
                // .eq(TtRedPacketRecord::getUserId, ttUser.getUserId())
                // .eq(TtRedPacketRecord::getReceivePassword, ttRedPacket.getPassword())
                .list();
        if (allRedPackRecord.size() >= ttRedPacket.getNum()) return R.fail("红包已经抢光了！");
        if (Objects.isNull(ttRedPacket.getUseStatus()) || ttRedPacket.getUseStatus().equals(1)) return R.fail("该红包被禁用，请联系管理员。");
        if (ObjectUtil.isNotEmpty(ttRedPacket.getUserId()) && Objects.equals(ttUser.getUserId(), ttRedPacket.getUserId()))
            return R.fail("给粉丝的专属红包，您自己无法领取。");
        if (ObjectUtil.isNotEmpty(ttRedPacket.getUserId())
                && ttRedPacket.getUserId() != 0
                && !Objects.equals(ttUser.getParentId(), ttRedPacket.getUserId()))
            return R.fail("粉丝专属红包，您尚未关注该主播。");

        Date openingTime = ttRedPacket.getOpeningTime();
        if (DateUtils.getNowDate().compareTo(ttRedPacket.getOpeningTime()) < 0)
            return R.fail("红包开启时间：" + DateUtil.format(openingTime, "yyyy-MM-dd HH:mm:ss") + ",敬请期待。");

        Date validity = ttRedPacket.getValidity();
        if (StringUtils.isNotNull(validity) && DateUtils.getNowDate().compareTo(validity) > 0) {
            ttRedPacket.setStatus(1);
            // redPacketMapper.updateById(ttRedPacket);
            new LambdaUpdateChainWrapper<>(redPacketMapper)
                    .eq(TtRedPacket::getId, ttRedPacket.getId())
                    .set(TtRedPacket::getStatus, 1)
                    .update();
            return R.fail("红包已结束");
        }

        for (TtRedPacketRecord record : allRedPackRecord) {
            if (record.getUserId().equals(ttUser.getUserId())) return R.fail("您已领取过该红包，请勿重复领取。");
            break;
        }

        return R.ok(ttRedPacket);
    }

    @Override
    public R receiveRedPacket(String code, TtUser ttUser) {

        R<TtRedPacket> check = receiveRedPacketCheck(code, ttUser);
        if (!check.getCode().equals(200)) return check;

        TtRedPacket ttRedPacket = check.getData();

        Boolean lock = false;
        for (int l = 0; l < 5; l++) {
            lock = redisLock.tryLock(RedisConstants.RECEIVE_RED_PACKET_LOCK + ttRedPacket.getId(), 5L, 10L, TimeUnit.SECONDS);
        }

        if (!lock) return R.fail("没抢到红包。");

        try {

            List<TtRedPacketRecord> redPacketRecords = new LambdaQueryChainWrapper<>(redPacketRecordMapper)
                    .eq(TtRedPacketRecord::getRedPacketId, ttRedPacket.getId())
                    .list();

            if (ttRedPacket.getNum() > redPacketRecords.size()) {

                // 构建抢红包记录
                BigDecimal receiveAmount = RandomUtils.getRandomPrice(ttRedPacket.getAmount());
                receiveAmount = receiveAmount.setScale(2, RoundingMode.HALF_UP);

                TtRedPacketRecord redPacketRecord = new TtRedPacketRecord();
                redPacketRecord.setRedPacketId(ttRedPacket.getId());
                redPacketRecord.setUserId(ttUser.getUserId());
                redPacketRecord.setReceivePassword(code);
                redPacketRecord.setReceiveAmount(receiveAmount);
                redPacketRecord.setReceiveTime(DateUtils.getNowDate());

                // 保存记录
                redPacketRecordMapper.insert(redPacketRecord);

                // 更新用户账户
                LambdaUpdateWrapper<TtUser> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper
                        .eq(TtUser::getUserId, ttUser.getUserId())
                        .eq(TtUser::getDelFlag, 0)
                        .setSql("account_amount = account_amount + " + receiveAmount);
                userService.update(updateWrapper);

                // 更新红包状态
                Integer count = new LambdaQueryChainWrapper<>(redPacketRecordMapper)
                        .eq(TtRedPacketRecord::getRedPacketId, ttRedPacket.getId())
                        .count();
                if (ttRedPacket.getNum() <= count) {
                    // 抢完更新为已结束
                    ttRedPacket.setStatus(1);
                    new LambdaUpdateChainWrapper<>(redPacketMapper)
                            .eq(TtRedPacket::getId, ttRedPacket.getId())
                            .set(TtRedPacket::getStatus, 1)
                            .update();
                    // redPacketMapper.updateById(ttRedPacket);
                }

                // 综合消费日志
                TtUser userById = userService.getById(ttUser.getUserId());
                TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                        .userId(userById.getUserId())

                        .amount(ObjectUtil.isNotEmpty(receiveAmount) ? receiveAmount : null)
                        .finalAmount(ObjectUtil.isNotEmpty(receiveAmount) ? userById.getAccountAmount().add(receiveAmount) : null)

                        .credits(null)
                        .finalCredits(null)

                        .total(receiveAmount)  // 收支合计

                        .type(TtAccountRecordType.INPUT.getCode())
                        .source(TtAccountRecordSource.RECEIVE_RED_PACKET.getCode())
                        .remark(TtAccountRecordSource.RECEIVE_RED_PACKET.getMsg())

                        .createTime(new Timestamp(System.currentTimeMillis()))
                        .updateTime(new Timestamp(System.currentTimeMillis()))
                        .build();
                userBlendErcashMapper.insert(blendErcash);

                return R.ok(receiveAmount);

            } else {
                new LambdaUpdateChainWrapper<>(redPacketMapper)
                        .eq(TtRedPacket::getId, ttRedPacket.getId())
                        .set(TtRedPacket::getStatus, 1)
                        .update();
                return R.fail("红包已经抢光了！");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("系统繁忙，请稍后重试。");
        } finally {
            redisLock.unlock(RedisConstants.RECEIVE_RED_PACKET_LOCK + ttRedPacket.getId());
        }
    }
}
