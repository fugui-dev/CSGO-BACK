package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtRechargeRecord;
import com.ruoyi.admin.mapper.TtRechargeRecordMapper;
import com.ruoyi.admin.service.TtRechargeRecordService;
import com.ruoyi.domain.other.TtRechargeRecordBody;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TtRechargeRecordServiceImpl extends ServiceImpl<TtRechargeRecordMapper, TtRechargeRecord> implements TtRechargeRecordService {

    @Override
    public List<TtRechargeRecord> queryList(TtRechargeRecordBody rechargeRecordBody) {
        LambdaQueryWrapper<TtRechargeRecord> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotNull(rechargeRecordBody.getUserId()))
            wrapper.eq(TtRechargeRecord::getUserId, rechargeRecordBody.getUserId());
        if (StringUtils.isNotNull(rechargeRecordBody.getAnchorVirtual()))
            wrapper.eq(TtRechargeRecord::getAnchorVirtual, rechargeRecordBody.getAnchorVirtual());
        if (StringUtils.isNotNull(rechargeRecordBody.getParentId()))
            wrapper.eq(TtRechargeRecord::getParentId, rechargeRecordBody.getParentId());
        if (StringUtils.isNotEmpty(rechargeRecordBody.getOrderId()))
            wrapper.eq(TtRechargeRecord::getOrderId, rechargeRecordBody.getOrderId());
        if (StringUtils.isNotEmpty(rechargeRecordBody.getOutTradeNo()))
            wrapper.eq(TtRechargeRecord::getOutTradeNo, rechargeRecordBody.getOutTradeNo());
        if (StringUtils.isNotEmpty(rechargeRecordBody.getChannelType()))
            wrapper.eq(TtRechargeRecord::getChannelType, rechargeRecordBody.getChannelType());
        if (StringUtils.isNotNull(rechargeRecordBody.getStartTime()) && StringUtils.isNotNull(rechargeRecordBody.getEndTime()))
            wrapper.between(TtRechargeRecord::getCreateTime, rechargeRecordBody.getStartTime(), rechargeRecordBody.getEndTime());
        wrapper.orderByDesc(TtRechargeRecord::getCreateTime);
        return this.list(wrapper);
    }
}
