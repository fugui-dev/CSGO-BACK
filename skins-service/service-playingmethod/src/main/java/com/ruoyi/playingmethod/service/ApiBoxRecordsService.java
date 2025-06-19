package com.ruoyi.playingmethod.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.dto.boxRecords.queryCondition;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;

import java.util.List;

public interface ApiBoxRecordsService extends IService<TtBoxRecords> {

    List<TtBoxRecordsVO> byCondition(queryCondition condition);

}
