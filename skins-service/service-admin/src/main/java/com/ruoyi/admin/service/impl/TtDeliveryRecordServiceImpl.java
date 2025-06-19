package com.ruoyi.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.common.constant.DeliveryOrderStatus;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.dto.userRecord.DeliveryRecordsConfition;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.delivery.TtDeliveryRecord;
import com.ruoyi.admin.mapper.TtBoxRecordsMapper;
import com.ruoyi.admin.mapper.TtDeliveryRecordMapper;
import com.ruoyi.admin.service.TtDeliveryRecordService;
import com.ruoyi.domain.other.TtDeliveryApplyBody;
import com.ruoyi.domain.vo.DeliveryApplyVO;
import com.ruoyi.domain.other.TtDeliveryRecordBody;
import com.ruoyi.domain.vo.TtDeliveryRecordDataVO;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.domain.vo.delivery.DeliveryRecordVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TtDeliveryRecordServiceImpl extends ServiceImpl<TtDeliveryRecordMapper, TtDeliveryRecord> implements TtDeliveryRecordService {

    private final TtBoxRecordsMapper boxRecordsMapper;

    public TtDeliveryRecordServiceImpl(TtBoxRecordsMapper boxRecordsMapper) {
        this.boxRecordsMapper = boxRecordsMapper;
    }

    @Override
    public List<DeliveryApplyVO> getDeliveryApplyList(TtDeliveryApplyBody deliveryApplyBody) {
        return baseMapper.getDeliveryApplyList(deliveryApplyBody);
    }

    @Override
    public String deliveryFail(Integer deliveryRecordId, String message) {

        TtDeliveryRecord ttDeliveryRecord = this.getById(deliveryRecordId);
        ttDeliveryRecord.setStatus(DeliveryOrderStatus.ORDER_CANCEL.getCode());
        ttDeliveryRecord.setMessage(DeliveryOrderStatus.ORDER_CANCEL.getMsg());
        ttDeliveryRecord.setUpdateBy(SecurityUtils.getUsername());
        ttDeliveryRecord.setUpdateTime(DateUtils.getNowDate());

        this.updateById(ttDeliveryRecord);

        TtBoxRecords ttBoxRecords = new LambdaQueryChainWrapper<>(boxRecordsMapper)
                .eq(TtBoxRecords::getId, ttDeliveryRecord.getBoxRecordsId())
                .eq(TtBoxRecords::getHolderUserId, ttDeliveryRecord.getUserId())
                .eq(TtBoxRecords::getOrnamentId, ttDeliveryRecord.getOrnamentId())
                .eq(TtBoxRecords::getStatus, TtboxRecordStatus.APPLY_DELIVERY.getCode())
                .one();
        ttBoxRecords.setStatus(TtboxRecordStatus.IN_PACKSACK_ON.getCode());
        ttBoxRecords.setUpdateTime(DateUtils.getNowDate());
        boxRecordsMapper.updateById(ttBoxRecords);
        return "";
    }

    @Override
    public List<TtDeliveryRecordDataVO> getDeliveryRecordList(TtDeliveryRecordBody deliveryRecordBody) {
        return baseMapper.getDeliveryRecordList(deliveryRecordBody);
    }

    @Override
    public List<TtDeliveryRecordDataVO> getDeliveryRecordByUserList(TtDeliveryRecordBody deliveryRecordBody) {
        return baseMapper.getDeliveryRecordByUserList(deliveryRecordBody);
    }

    @Override
    public List<DeliveryRecordVO> byCondition(DeliveryRecordsConfition param) {

        param.setLimit((param.getPage()-1)*param.getSize());

        List<DeliveryRecordVO> list = baseMapper.byCondition(
                param.getStatusList(),
                param.getUIdList(),
                param.getLimit(),
                param.getSize());

        return list;

        // Page<TtDeliveryRecord> pageInfo = new Page<>(param.getPage(), param.getSize());
        // pageInfo.setOptimizeCountSql(false);
        //
        // LambdaQueryWrapper<TtDeliveryRecord> wrapper = new LambdaQueryWrapper<>();
        // wrapper
        //         .in(ObjectUtil.isNotEmpty(param.getStatusList()),TtDeliveryRecord::getStatus,param.getStatusList())
        //         .in(ObjectUtil.isNotEmpty(param.getUIdList()),TtDeliveryRecord::getStatus,param.getUIdList());
        // return this.page(pageInfo, wrapper);

    }
}
