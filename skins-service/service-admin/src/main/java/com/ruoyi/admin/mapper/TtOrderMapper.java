package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.dto.userRecord.OrderCondition;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.vo.order.TtOrderVO;
import com.ruoyi.domain.vo.sys.SimpleTtUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface TtOrderMapper extends BaseMapper<TtOrder> {
    List<TtOrderVO> byCondition(OrderCondition param);

    List<SimpleTtUserVO> batchRechargeTotal(@Param("userIdList") List<Integer> userIdList,
                                            @Param("beginTime") String beginTime,
                                            @Param("endTime") String endTime,
                                            @Param("orderByType") Integer orderbyType,
                                            @Param("limit") Integer limit,
                                            @Param("size") Integer size);

    SimpleTtUserVO rechargeTotalOfBoss(
            @Param("employeeId") Integer employeeId,
            @Param("beginTime") Timestamp beginTime,
            @Param("endTime") String endTime,
            @Param("orderType") Integer orderType,
            @Param("limit") int limit,
            @Param("size") Integer size);
}
