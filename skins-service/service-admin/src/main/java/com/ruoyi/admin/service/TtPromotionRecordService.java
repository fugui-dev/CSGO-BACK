package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.entity.TtPromotionRecord;
import com.ruoyi.domain.vo.PromotionDataVO;

import java.util.List;

public interface TtPromotionRecordService extends IService<TtPromotionRecord> {

    List<TtPromotionRecord> getPromotionRecord(TtPromotionRecord ttPromotionRecord);

    PromotionDataVO statisticsPromotionData(Integer userId);
}
