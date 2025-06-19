package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.dto.userRecord.DeliveryRecordsConfition;
import com.ruoyi.domain.entity.delivery.TtDeliveryRecord;
import com.ruoyi.domain.other.TtDeliveryApplyBody;
import com.ruoyi.domain.vo.DeliveryApplyVO;
import com.ruoyi.domain.other.TtDeliveryRecordBody;
import com.ruoyi.domain.vo.TtDeliveryRecordDataVO;
import com.ruoyi.domain.vo.delivery.DeliveryRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TtDeliveryRecordMapper extends BaseMapper<TtDeliveryRecord> {
    List<DeliveryApplyVO> getDeliveryApplyList(TtDeliveryApplyBody deliveryApplyBody);

    List<TtDeliveryRecordDataVO> getDeliveryRecordList(TtDeliveryRecordBody deliveryRecordBody);

    List<TtDeliveryRecordDataVO> getDeliveryRecordByUserList(TtDeliveryRecordBody deliveryRecordBody);

    List<DeliveryRecordVO> byCondition(@Param("statusList") List<Integer> statusList,
                                       @Param("uidList") List<Integer> uidList,
                                       @Param("limit") Integer limit,
                                       @Param("size") Integer size);
}
