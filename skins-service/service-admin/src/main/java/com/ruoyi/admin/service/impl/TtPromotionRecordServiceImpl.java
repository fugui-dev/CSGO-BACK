package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.TtUserAmountRecordsMapper;
import com.ruoyi.domain.entity.TtPromotionRecord;
import com.ruoyi.admin.mapper.TtPromotionRecordMapper;
import com.ruoyi.admin.service.TtPromotionRecordService;
import com.ruoyi.domain.vo.PromotionDataVO;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class TtPromotionRecordServiceImpl extends ServiceImpl<TtPromotionRecordMapper, TtPromotionRecord> implements TtPromotionRecordService {

    @Autowired
    private TtUserAmountRecordsMapper ttUserAmountRecordsMapper;

    @Override
    public List<TtPromotionRecord> getPromotionRecord(TtPromotionRecord ttPromotionRecord) {
        LambdaQueryWrapper<TtPromotionRecord> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotNull(ttPromotionRecord.getUserId())) wrapper.eq(TtPromotionRecord::getUserId, ttPromotionRecord.getUserId());
        if (StringUtils.isNotEmpty(ttPromotionRecord.getStatus())) wrapper.eq(TtPromotionRecord::getStatus, ttPromotionRecord.getStatus());
        if (StringUtils.isNotNull(ttPromotionRecord.getRechargeRecordId())) wrapper.eq(TtPromotionRecord::getRechargeRecordId, ttPromotionRecord.getRechargeRecordId());
        wrapper.orderByDesc(TtPromotionRecord::getId);
        List<TtPromotionRecord> ttPromotionRecordList = this.list(wrapper);
        for (TtPromotionRecord promotionRecord : ttPromotionRecordList) {
            BigDecimal totalConsumption = ttUserAmountRecordsMapper.getTotalConsumptionByUserId(promotionRecord.getSubordinateUserId());
            if (Objects.isNull(totalConsumption)) {
                promotionRecord.setTotalConsumption(new BigDecimal(0));
            } else {
                promotionRecord.setTotalConsumption(totalConsumption);
            }
        }
        return ttPromotionRecordList;
    }

    @Override
    public PromotionDataVO statisticsPromotionData(Integer userId) {
        return baseMapper.statisticsPromotionData(userId);
    }
}
