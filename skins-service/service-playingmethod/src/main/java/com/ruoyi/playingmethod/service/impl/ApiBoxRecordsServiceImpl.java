package com.ruoyi.playingmethod.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.domain.dto.boxRecords.queryCondition;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import com.ruoyi.playingmethod.service.ApiBoxRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ApiBoxRecordsServiceImpl extends ServiceImpl<TtBoxRecordsMapper, TtBoxRecords> implements ApiBoxRecordsService {
    @Override
    public List<TtBoxRecordsVO> byCondition(queryCondition condition) {

        condition.setStatus(new ArrayList<>(0));

        condition.setLimit((condition.getPage() - 1) * condition.getSize());
        if (ObjectUtil.isNull(condition.getOrderByFie())) condition.setOrderByFie(1);

        List<TtBoxRecordsVO> list = this.baseMapper.byCondition(
                condition.getLimit(),
                condition.getSize(),
                condition.getOrderByFie(),
                condition.getBoxRecordId(),
                condition.getBoxId(),
                condition.getUserId(),
                condition.getUserType(),
                condition.getSource(),
                condition.getStatus(),
                condition.getOrnamentPriceMin(),
                condition.getOrnamentPriceMax(),
                condition.getOrnamentLevelIds()
        );

        // 排序
        // Comparator<TtBoxRecordsVO> comparator = null;
        // if (ObjectUtil.isNull(condition.getOrderByFie()) || condition.getOrderByFie().equals(1)) {
        //     comparator = new Comparator<TtBoxRecordsVO>() {
        //         @Override
        //         public int compare(TtBoxRecordsVO o1, TtBoxRecordsVO o2) {
        //             return o1.getCreateTime().compareTo(o2.getCreateTime());
        //         }
        //     };
        // } else if (condition.getOrderByFie().equals(0)) {
        //     comparator = new Comparator<TtBoxRecordsVO>() {
        //         @Override
        //         public int compare(TtBoxRecordsVO o1, TtBoxRecordsVO o2) {
        //             return o2.getCreateTime().compareTo(o1.getCreateTime());
        //         }
        //     };
        // }
        //
        // Collections.sort(list, comparator);

        return list;

    }
}
