package com.ruoyi.playingmethod.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.ruoyi.admin.mapper.TtWelfareMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.admin.util.core.fight.LotteryMachine;
import com.ruoyi.domain.common.constant.TtboxRecordSource;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.other.TtOrnamentsA;
import com.ruoyi.domain.other.TtWelfare;
import com.ruoyi.domain.vo.OpenBoxVO;
import com.ruoyi.playingmethod.mapper.ApiBindBoxMapper;
import com.ruoyi.playingmethod.mapper.ApiWelfareMapper;
import com.ruoyi.playingmethod.model.ApiWelfare;
import com.ruoyi.playingmethod.model.ApiWelfareRecord;
import com.ruoyi.playingmethod.service.ApiWelfareService;
import com.ruoyi.playingmethod.utils.customException.OrnamentNullException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ApiWelfareServiceImpl implements ApiWelfareService {

    @Autowired
    private ApiWelfareMapper apiWelfareMapper;

    @Autowired
    private TtUserService ttUserService;

    @Autowired
    private TtBoxService ttBoxService;

    @Autowired
    private LotteryMachine lotteryMachine;

    @Autowired
    private ApiBindBoxMapper bindBoxMapper;

    @Autowired
    private TtBoxRecordsService boxRecordsService;

    @Autowired
    private TtWelfareMapper ttWelfareMapper;

    @Override
    public List<ApiWelfare> getWelfareList(Long userId) {
        List<ApiWelfare> welfareList = apiWelfareMapper.getWelfareList(userId);
        for (ApiWelfare apiWelfare : welfareList) {
            if ("1".equals(apiWelfare.getType())) {
                boolean isEligible = checkVipUpgradeEligibility(userId, apiWelfare.getVipLevel());
                apiWelfare.setEligible(isEligible ? "1" : "0");
                boolean isClaimed = apiWelfareMapper.checkClaimed(apiWelfare.getWelfareId(), userId);
                apiWelfare.setClaimStatus(isClaimed ? "1" : "0");
            }
            if ("2".equals(apiWelfare.getType())) {
                boolean isEligible = checkMonthlyRechargeEligibility(userId);
                apiWelfare.setEligible(isEligible ? "1" : "0");
            }
        }
        return welfareList;
    }

    @Transactional
    @Override
    public OpenBoxVO claimWelfare(Integer welfareId, Long userId) {
        // 1.获取福利对应的宝箱信息
        Integer boxId = apiWelfareMapper.getBoxIdByWelfareId(welfareId);
        TtUser ttUser = ttUserService.getById(userId);
        TtBox ttBox = ttBoxService.getById(boxId);
        // 2.抽奖
        String ornamentId = lotteryMachine.singleLottery(ttUser, ttBox);
        if (Objects.isNull(ornamentId)) {
            throw new OrnamentNullException("福利开箱未抽中");
        }
        // 3.获取宝箱详细信息
        TtOrnamentsA ornamentsData = bindBoxMapper.ornamentsInfo(boxId, ornamentId);
        if (Objects.isNull(ornamentsData)) {
            throw new OrnamentNullException("福利开箱未抽中");
        }
        // 4.构建并存储开箱记录
        TtBoxRecords ttBoxRecords = new TtBoxRecords();
        ttBoxRecords.setUserId(ttUser.getUserId());
        ttBoxRecords.setBoxId(ttBox.getBoxId());
        ttBoxRecords.setBoxName(ttBox.getBoxName());
        ttBoxRecords.setBoxPrice(ttBox.getPrice());
        ttBoxRecords.setOrnamentId(Long.valueOf(ornamentId));
        ttBoxRecords.setOrnamentName(ornamentsData.getName());
        ttBoxRecords.setImageUrl(ornamentsData.getImageUrl());
        ttBoxRecords.setOrnamentsPrice(ornamentsData.getUsePrice());
        ttBoxRecords.setOrnamentsLevelId(ornamentsData.getOrnamentsLevelId());
        ttBoxRecords.setOrnamentLevelImg(ornamentsData.getLevelImg());
        ttBoxRecords.setHolderUserId(ttUser.getUserId());
        ttBoxRecords.setSource(TtboxRecordSource.BLIND_BOX.getCode());
        ttBoxRecords.setStatus(TtboxRecordStatus.IN_PACKSACK_ON.getCode());
        ttBoxRecords.setCreateTime(new Timestamp(System.currentTimeMillis()));
        boxRecordsService.save(ttBoxRecords);
        // 5.存储领取记录
        ApiWelfareRecord apiWelfareRecord = new ApiWelfareRecord();
        apiWelfareRecord.setWelfareId(welfareId);
        apiWelfareRecord.setUserId(userId);
        apiWelfareRecord.setCreateTime(new Date());
        apiWelfareMapper.saveClaimWelfareRecord(apiWelfareRecord);
        // 6.返回前端数据
        OpenBoxVO openBoxVO = new OpenBoxVO();
        BeanUtil.copyProperties(ttBoxRecords, openBoxVO);
        openBoxVO.setUsePrice(openBoxVO.getOrnamentsPrice());
        openBoxVO.setLevelImg(ttBoxRecords.getOrnamentLevelImg());
        return openBoxVO;
    }

    @Override
    public boolean checkNotEligible(Integer welfareId, Long userId) {
        TtWelfare ttWelfare = ttWelfareMapper.selectTtWelfareByWelfareId(welfareId);
        if ("1".equals(ttWelfare.getType())) {
            return !checkVipUpgradeEligibility(userId, ttWelfare.getVipLevel());
        }
        if ("2".equals(ttWelfare.getType())) {
            return !checkMonthlyRechargeEligibility(userId);
        }
        return false;
    }

    @Override
    public boolean checkClaimed(Integer welfareId, Long userId) {
        return apiWelfareMapper.checkClaimed(welfareId, userId);
    }

    /**
     * 检查是否具备VIP等级福利领取条件
     */
    private boolean checkVipUpgradeEligibility(Long userId, Integer vipLevel) {
        TtUser ttUser = ttUserService.getById(userId);
        return ttUser.getVipLevel() >= vipLevel;
    }

    /**
     * 检查是否具备VIP每月充值福利领取条件
     */
    private boolean checkMonthlyRechargeEligibility(Long userId) {
        // 1.查询用户当月充值量

        // 2.比较指定的充值量

        return false;
    }
}
