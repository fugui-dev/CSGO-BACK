package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.other.TtRechargeRecord;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface TtRechargeRecordMapper extends BaseMapper<TtRechargeRecord> {

    List<Integer> getLastHourRechargeUserIds(BigDecimal minRecharge);

    List<Integer> getLastDayRechargeUserIds(BigDecimal minRecharge);

    List<Integer> getLastWeekRechargeUserIds(BigDecimal minRecharge);

    List<Integer> getLastMonthRechargeUserIds(BigDecimal minRecharge);

    Integer getFirstChargeNumByChannelId(Integer id);

    BigDecimal getTotalChargeByChannelId(Integer id);

}
