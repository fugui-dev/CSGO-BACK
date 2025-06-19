package com.ruoyi.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.TtOrderMapper;
import com.ruoyi.admin.service.TtOrderService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.PayOrderStatus;
import com.ruoyi.domain.dto.sys.OrderQueryCondition;
import com.ruoyi.domain.dto.sys.TeamUsersParam;
import com.ruoyi.domain.dto.userRecord.OrderCondition;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.vo.order.TtOrderVO;
import com.ruoyi.domain.vo.sys.SimpleTtUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class TtOrderServiceImpl extends ServiceImpl<TtOrderMapper, TtOrder> implements TtOrderService {

    private final TtOrderMapper ttOrderMapper;

    public TtOrderServiceImpl(TtOrderMapper ttOrderMapper) {
        this.ttOrderMapper = ttOrderMapper;
    }

    @Override
    public List<TtOrder> queryList(TtOrder ttOrder) {
        LambdaQueryWrapper<TtOrder> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotNull(ttOrder.getStatus()))
            wrapper.eq(TtOrder::getStatus, ttOrder.getStatus());
        if (StringUtils.isNotNull(ttOrder.getUserId()))
            wrapper.eq(TtOrder::getUserId, ttOrder.getUserId());
        return this.list(wrapper);
    }

    @Override
    public List<TtOrderVO> byCondition(OrderCondition param) {

        param.setLimit((param.getPage() - 1) * param.getSize());
        List<TtOrderVO> list = baseMapper.byCondition(param);

        Comparator<TtOrderVO> comparator = null;
        if (ObjectUtil.isNull(param.getOrderBy()) || param.getOrderBy().equals(1)) {
            comparator = new Comparator<TtOrderVO>() {
                @Override
                public int compare(TtOrderVO o1, TtOrderVO o2) {
                    return o1.getCreateTime().compareTo(o2.getCreateTime());
                }
            };
        } else if (param.getOrderBy().equals(2)) {
            comparator = new Comparator<TtOrderVO>() {
                @Override
                public int compare(TtOrderVO o1, TtOrderVO o2) {
                    return o2.getCreateTime().compareTo(o1.getCreateTime());
                }
            };
        }

        Collections.sort(list, comparator);

        return list;

    }

    // 批量统计消费
    @Override
    public List<SimpleTtUserVO> batchRechargeTotal(List<Integer> allEmployeesId,
                                                   String beginTime,
                                                   String endTime,
                                                   Integer orderType,
                                                   Integer page,
                                                   Integer size) {

        int limit = (page - 1) * size;

        if (StringUtils.isBlank(beginTime)) beginTime = null;
        if (StringUtils.isBlank(endTime)) endTime = null;
        if (ObjectUtil.isNull(orderType)) orderType = 1;

        return baseMapper.batchRechargeTotal(
                allEmployeesId,
                beginTime,
                endTime,
                orderType,
                limit,
                size);
    }

    @Override
    public R clientList(Integer page, Integer size, int uid) {

        Page<TtOrder> pageInfo = new Page<>(page, size);
        pageInfo.setOptimizeCountSql(false);

        LambdaQueryWrapper<TtOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(TtOrder::getUserId,uid)
                .eq(TtOrder::getStatus, PayOrderStatus.PAY_COMPLE.getCode())
                .orderByDesc(TtOrder::getCreateTime);
        pageInfo = this.page(pageInfo, wrapper);

        return R.ok(pageInfo);
    }

    @Override
    public R adminList(OrderQueryCondition con) {

        if (ObjectUtil.isNull(con.getPage()) || ObjectUtil.isNull(con.getSize())){
            return R.fail("分页参数不能为空");
        }

        Page<TtOrder> pageInfo = new Page<>(con.getPage(), con.getSize());
        pageInfo.setOptimizeCountSql(false);

        LambdaQueryWrapper<TtOrder> wrapper = new LambdaQueryWrapper<>();

        wrapper
                .in(ObjectUtil.isNotEmpty(con.getUidList()),TtOrder::getUserId,con.getUidList())
                .in(ObjectUtil.isNotEmpty(con.getStatusList()),TtOrder::getStatus,con.getStatusList());

        if (ObjectUtil.isNotEmpty(con.getBeginTime()) && ObjectUtil.isNotEmpty(con.getEndTime())){
            wrapper.between(TtOrder::getCreateTime,con.getBeginTime(),con.getEndTime());
        }

        if (ObjectUtil.isNull(con.getOrderByFie())) con.setOrderByFie("time");
        if (ObjectUtil.isNull(con.getOrderByType())) con.setOrderByType(1);

        if (con.getOrderByFie().equals("time")){

            if (con.getOrderByType().equals(1)){
                wrapper.orderByDesc(TtOrder::getCreateTime);
            }else if (con.getOrderByType().equals(2)){
                wrapper.orderByAsc(TtOrder::getCreateTime);
            }else {
                wrapper.orderByDesc(TtOrder::getCreateTime);
            }

        }else if (con.getOrderByFie().equals("totalAmount")){
            if (con.getOrderByType().equals(1)){
                wrapper.orderByDesc(TtOrder::getTotalAmount);
            }else if (con.getOrderByType().equals(2)){
                wrapper.orderByAsc(TtOrder::getTotalAmount);
            }else {
                wrapper.orderByDesc(TtOrder::getTotalAmount);
            }
        }else {
            if (con.getOrderByType().equals(1)){
                wrapper.orderByDesc(TtOrder::getCreateTime);
            }else if (con.getOrderByType().equals(2)){
                wrapper.orderByAsc(TtOrder::getCreateTime);
            }else {
                wrapper.orderByDesc(TtOrder::getCreateTime);
            }
        }

        Page<TtOrder> page = this.page(pageInfo, wrapper);

        return R.ok(page);
    }

    @Override
    public SimpleTtUserVO rechargeTotalOfBoss(Integer employeeId, Timestamp beginTime, String endTime, Integer orderType, Integer page, Integer size) {

        int limit = (page - 1) * size;

        if (StringUtils.isBlank(endTime)) endTime = null;
        if (ObjectUtil.isNull(orderType)) orderType = 1;

        return baseMapper.rechargeTotalOfBoss(
                employeeId,
                beginTime,
                endTime,
                orderType,
                limit,
                size);

    }
}
