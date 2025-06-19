package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.TtPromotionLevel;
import com.ruoyi.domain.vo.PromotionInfoVO;

public interface TtPromotionLevelService extends IService<TtPromotionLevel> {

    String generateVipLevel(Integer num);

    String updatePromotionLevelById(TtPromotionLevel ttPromotionLevel);
    void truncatePromotionLevel();

    R<PromotionInfoVO> getPromotionInfo();

}
