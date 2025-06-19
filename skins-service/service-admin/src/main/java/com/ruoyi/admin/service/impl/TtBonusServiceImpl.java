package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.TtBonusMapper;
import com.ruoyi.admin.mapper.TtPromotionRecordMapper;
import com.ruoyi.admin.mapper.TtRechargeRecordMapper;
import com.ruoyi.admin.service.*;
import com.ruoyi.admin.util.RandomUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.entity.TtPromotionLevel;
import com.ruoyi.domain.entity.TtPromotionRecord;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TtBonusServiceImpl extends ServiceImpl<TtBonusMapper, TtBonus> implements TtBonusService {

    private final TtUserService userService;
    private final TtVipLevelService vipLevelService;
    private final TtPromotionLevelService promotionLevelService;
    private final TtRechargeRecordMapper rechargeRecordMapper;
    private final TtPromotionRecordMapper promotionRecordMapper;
    private final TtBonusReceiveRecordService bonusReceiveRecordService;

    public TtBonusServiceImpl(TtUserService userService,
                              TtVipLevelService vipLevelService,
                              TtPromotionLevelService promotionLevelService,
                              TtRechargeRecordMapper rechargeRecordMapper,
                              TtPromotionRecordMapper promotionRecordMapper,
                              TtBonusReceiveRecordService bonusReceiveRecordService) {
        this.userService = userService;
        this.vipLevelService = vipLevelService;
        this.promotionLevelService = promotionLevelService;
        this.rechargeRecordMapper = rechargeRecordMapper;
        this.promotionRecordMapper = promotionRecordMapper;
        this.bonusReceiveRecordService = bonusReceiveRecordService;
    }

    @Override
    public String updateBonusById(TtBonus ttBonus) {
        String coverPicture = ttBonus.getCoverPicture();
        ttBonus.setCoverPicture(RuoYiConfig.getDomainName() + coverPicture);
        this.updateById(ttBonus);
        return "";
    }

    @Override
    public void bonus(Integer userId, Integer rechargeRecordId) {

        List<TtRechargeRecord> rechargeRecords = new LambdaQueryChainWrapper<>(rechargeRecordMapper)
                .eq(TtRechargeRecord::getUserId, userId)
                .eq(TtRechargeRecord::getStatus, "0")
                .list();

        //总充值
        BigDecimal priceTotal = rechargeRecords.stream()
                .map(TtRechargeRecord::getAmountActuallyPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TtUser ttUser = userService.getById(userId);

        //本次充值记录
        TtRechargeRecord ttRechargeRecord = rechargeRecordMapper.selectById(rechargeRecordId);

        //实际支付金额
        BigDecimal amountActuallyPaid = ttRechargeRecord.getAmountActuallyPaid();

        //上级用户推广福利
        if (!Objects.isNull(ttUser.getParentId())) {
            TtUser parentUser = userService.getById(ttUser.getParentId());
            if (insertParentUserPromotionRecord(ttUser.getUserId(), parentUser, amountActuallyPaid, rechargeRecordId)) {
                updateParentUserPromotionLevel(parentUser);
            }
        }

        //会员福利
        if (ttUser.getVipLevel() > 0) {
            TtVipLevel ttVipLevel = vipLevelService.getById(ttUser.getVipLevel());
            BigDecimal rebate = ttVipLevel.getCommissions().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    .multiply(amountActuallyPaid).setScale(2, RoundingMode.HALF_UP);
            ttUser.setAccountAmount(ttUser.getAccountAmount().add(rebate));
            userService.updateById(ttUser);
            userService.insertUserAmountRecords(ttUser.getUserId(), TtAccountRecordType.INPUT, TtAccountRecordSource.BONUS, rebate, ttUser.getAccountAmount());
        }
        updateUserVIPLevel(ttUser.getUserId(), priceTotal);

        //暂时取消卡密充值的福利
//        List<TtBonus> bonusList = new LambdaQueryChainWrapper<>(getBaseMapper()).eq(TtBonus::getStatus, "0").list();

        //充值福利领取记录
//        List<TtBonusReceiveRecord> bonusReceiveRecordList = new LambdaQueryChainWrapper<>(bonusReceiveRecordService.getBaseMapper())
//                .eq(TtBonusReceiveRecord::getUserId, userId)
//                .eq(TtBonusReceiveRecord::getType, "2")
//                .list();
//        List<Integer> bonusIds = bonusReceiveRecordList.stream().map(TtBonusReceiveRecord::getBonusId).collect(Collectors.toList());
//        List<TtBonusReceiveRecord> insertList = new ArrayList<>();
//        for (TtBonus bonus : bonusList) {
//            if ("2".equals(bonus.getType())) { //充值福利
//                if ("0".equals(bonus.getConditionType())) { //日充值福利
//
//                } else if ("1".equals(bonus.getConditionType())) { //周充值福利
//
//                } else if ("2".equals(bonus.getConditionType())) { //月充值福利
//
//                } else if ("3".equals(bonus.getConditionType())) { //总充值福利
//                    BigDecimal rechargeThreshold = bonus.getRechargeThreshold();
//                    if (!bonusIds.contains(bonus.getId()) && rechargeThreshold.compareTo(priceTotal) < 0) {
//                        TtBonusReceiveRecord bonusReceiveRecord = TtBonusReceiveRecord.builder().build();
//                        bonusReceiveRecord.setType("2");
//                        bonusReceiveRecord.setBonusId(bonus.getId());
//                        bonusReceiveRecord.setUserId(userId);
//                        bonusReceiveRecord.setAwardType("0");
//                        bonusReceiveRecord.setAwardPrice(RandomUtils.getRandomPrice(bonus.getAwardSection()));
//                        bonusReceiveRecord.setCreateTime(DateUtils.getNowDate());
//                        insertList.add(bonusReceiveRecord);
//                    }
//                } else if ("4".equals(bonus.getConditionType())) { // 单笔充值福利
//
//                }
//
//
//
//            } else if ("3".equals(bonus.getType())) {
//                if ("0".equals(bonus.getConditionType())) {
//
//                } else if ("1".equals(bonus.getConditionType())) {
//
//                } else if ("2".equals(bonus.getConditionType())) {
//
//                } else if ("3".equals(bonus.getConditionType())) {
//
//                }
//            }
//        }
//        bonusReceiveRecordService.saveBatch(insertList, 1);
    }

    public boolean insertParentUserPromotionRecord(Integer userId, TtUser parentUser, BigDecimal amountActuallyPaid, Integer rechargeRecordId) {
        TtPromotionRecord ttPromotionRecord = TtPromotionRecord.builder().build();
        ttPromotionRecord.setUserId(parentUser.getUserId());
        ttPromotionRecord.setSubordinateUserId(userId);
        ttPromotionRecord.setRechargePrice(amountActuallyPaid);
        ttPromotionRecord.setRechargeRecordId(rechargeRecordId);
        ttPromotionRecord.setCreateTime(DateUtils.getNowDate());
        if (parentUser.getPromotionLevel() > 0) {
            TtPromotionLevel ttPromotionLevel = promotionLevelService.getById(parentUser.getPromotionLevel());
            BigDecimal rebate = ttPromotionLevel.getCommissions().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    .multiply(amountActuallyPaid).setScale(2, RoundingMode.HALF_UP);
            ttPromotionRecord.setRebate(rebate);
        }
        return promotionRecordMapper.insert(ttPromotionRecord) > 0;
    }

    private void updateUserVIPLevel(Integer userId, BigDecimal priceTotal) {
        TtUser ttUser = userService.getById(userId);
        List<TtVipLevel> vipLevelList = vipLevelService.list();
        List<BigDecimal> VIPLevelPriceList = vipLevelList.stream().map(TtVipLevel::getRechargeThreshold).collect(Collectors.toList());
        int VIPLevel = this.getLevel(priceTotal, VIPLevelPriceList);
        if (ttUser.getVipLevel() != VIPLevel) {
            ttUser.setVipLevel(VIPLevel);
            userService.updateById(ttUser);

            //暂时取消卡密充值后的待领取福利
//            List<TtBonusReceiveRecord> bonusReceiveRecordList = new LambdaQueryChainWrapper<>(bonusReceiveRecordService.getBaseMapper())
//                    .eq(TtBonusReceiveRecord::getUserId, ttUser.getUserId())
//                    .eq(TtBonusReceiveRecord::getType, "0")
//                    .list();
//            List<Integer> VipLevelIds = bonusReceiveRecordList.stream().map(TtBonusReceiveRecord::getVipLevelId).collect(Collectors.toList());
//            List<Integer> VIPLevelIdList = getLevelIdList(VIPLevel, VipLevelIds);
//            if (!VIPLevelIdList.isEmpty()) {
//                List<TtBonusReceiveRecord> bonusReceiveRecords = new ArrayList<>();
//                for (TtVipLevel ttVipLevel : vipLevelService.getBaseMapper().selectBatchIds(VIPLevelIdList)) {
//                    if (BigDecimal.ZERO.compareTo(ttVipLevel.getAddedBonus()) < 0) {
//                        TtBonusReceiveRecord bonusReceiveRecord = TtBonusReceiveRecord.builder().build();
//                        bonusReceiveRecord.setType("0");
//                        bonusReceiveRecord.setVipLevelId(ttVipLevel.getId());
//                        bonusReceiveRecord.setUserId(ttUser.getUserId());
//                        bonusReceiveRecord.setAwardType("0");
//                        bonusReceiveRecord.setAwardPrice(ttVipLevel.getAddedBonus());
//                        bonusReceiveRecord.setCreateTime(DateUtils.getNowDate());
//                        bonusReceiveRecords.add(bonusReceiveRecord);
//                    }
//                }
//                bonusReceiveRecordService.saveBatch(bonusReceiveRecords, 1);
//            }
        }
    }

    private List<Integer> getLevelIdList(int level, List<Integer> levelIds) {
        List<Integer> levelList = new ArrayList<>();
        for (int i = 1; i <= level; i++) {
            levelList.add(i);
        }
        levelList.removeAll(levelIds);
        return levelList;
    }

    public void updateParentUserPromotionLevel(TtUser parentUser) {
        List<TtPromotionRecord> promotionRecordList = new LambdaQueryChainWrapper<>(promotionRecordMapper)
                .eq(TtPromotionRecord::getUserId, parentUser.getUserId())
                .list();
        List<TtPromotionLevel> promotionLevelList = promotionLevelService.list();
        BigDecimal rechargePriceTotal = promotionRecordList.stream().map(TtPromotionRecord::getRechargePrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<BigDecimal> promotionLevelPriceList = promotionLevelList.stream().map(TtPromotionLevel::getRechargeThreshold).collect(Collectors.toList());
        int promotionLevel = this.getLevel(rechargePriceTotal, promotionLevelPriceList);
        if (parentUser.getPromotionLevel() != promotionLevel) {
            parentUser.setPromotionLevel(promotionLevel);
            userService.updateById(parentUser);

            //取消推广等级的奖励
//            List<TtBonusReceiveRecord> bonusReceiveRecordList = new LambdaQueryChainWrapper<>(bonusReceiveRecordService.getBaseMapper())
//                    .eq(TtBonusReceiveRecord::getUserId, parentUser.getUserId())
//                    .eq(TtBonusReceiveRecord::getType, "1")
//                    .list();
//            List<Integer> promotionLevelIds = bonusReceiveRecordList.stream().map(TtBonusReceiveRecord::getPromotionLevelId).collect(Collectors.toList());
//            List<Integer> promotionLevelIdList = getLevelIdList(promotionLevel, promotionLevelIds);
//            if (!promotionLevelIdList.isEmpty()) {
//                List<TtBonusReceiveRecord> bonusReceiveRecords = new ArrayList<>();
//                for (TtPromotionLevel ttPromotionLevel : promotionLevelService.getBaseMapper().selectBatchIds(promotionLevelIdList)) {
//                    if (BigDecimal.ZERO.compareTo(ttPromotionLevel.getAddedBonus()) < 0) {
//                        TtBonusReceiveRecord bonusReceiveRecord = TtBonusReceiveRecord.builder().build();
//                        bonusReceiveRecord.setType("1");
//                        bonusReceiveRecord.setPromotionLevelId(ttPromotionLevel.getId());
//                        bonusReceiveRecord.setUserId(parentUser.getUserId());
//                        bonusReceiveRecord.setAwardType("0");
//                        bonusReceiveRecord.setAwardPrice(ttPromotionLevel.getAddedBonus());
//                        bonusReceiveRecord.setCreateTime(DateUtils.getNowDate());
//                        bonusReceiveRecords.add(bonusReceiveRecord);
//                    }
//                }
//                bonusReceiveRecordService.saveBatch(bonusReceiveRecords, 1);
//            }
        }
    }

    private int getLevel(BigDecimal price, List<BigDecimal> levelPriceList) {
        levelPriceList.sort(Comparator.naturalOrder());
        for (int i = 0; i < levelPriceList.size(); i++) {
            if (price.compareTo(levelPriceList.get(i)) < 0) {
                return i;
            }
        }
        return levelPriceList.size();
    }
}
