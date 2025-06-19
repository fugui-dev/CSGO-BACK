package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.sys.OrderQueryCondition;
import com.ruoyi.domain.dto.sys.TeamUsersParam;
import com.ruoyi.domain.dto.userRecord.OrderCondition;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.vo.order.TtOrderVO;
import com.ruoyi.domain.vo.sys.SimpleTtUserVO;

import java.sql.Timestamp;
import java.util.List;

public interface TtOrderService extends IService<TtOrder> {

    List<TtOrder> queryList(TtOrder ttOrder);

    List<TtOrderVO> byCondition(OrderCondition param);

    List<SimpleTtUserVO> batchRechargeTotal(List<Integer> allEmployeesId,
                                            String beginTime,
                                            String endTime,
                                            Integer orderType,
                                            Integer page,
                                            Integer size);

    R clientList(Integer page, Integer size, int uid);

    R adminList(OrderQueryCondition condition);

    SimpleTtUserVO rechargeTotalOfBoss(
            Integer employeeId,
            Timestamp beginTime,
            String endTime,
            Integer orderType,
            Integer page,
            Integer size);
}
