package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.dto.userRecord.DeliveryRecordsConfition;
import com.ruoyi.domain.entity.delivery.TtDeliveryRecord;
import com.ruoyi.domain.other.TtDeliveryApplyBody;
import com.ruoyi.domain.vo.DeliveryApplyVO;
import com.ruoyi.domain.other.TtDeliveryRecordBody;
import com.ruoyi.domain.vo.TtDeliveryRecordDataVO;
import com.ruoyi.domain.vo.delivery.DeliveryRecordVO;

import java.util.List;

public interface TtDeliveryRecordService extends IService<TtDeliveryRecord> {

    List<DeliveryApplyVO> getDeliveryApplyList(TtDeliveryApplyBody deliveryApplyBody);

    String deliveryFail(Integer deliveryRecordId, String message);

    List<TtDeliveryRecordDataVO> getDeliveryRecordList(TtDeliveryRecordBody deliveryRecordBody);


    List<TtDeliveryRecordDataVO> getDeliveryRecordByUserList(TtDeliveryRecordBody deliveryRecordBody);

    List<DeliveryRecordVO> byCondition(DeliveryRecordsConfition param);
}
