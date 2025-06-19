package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.entity.TtPromotionRecord;
import com.ruoyi.domain.vo.PromotionDataVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TtPromotionRecordMapper extends BaseMapper<TtPromotionRecord> {

    PromotionDataVO statisticsPromotionData(Integer userId);
}
