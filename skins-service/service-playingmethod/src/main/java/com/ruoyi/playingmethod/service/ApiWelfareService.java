package com.ruoyi.playingmethod.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.vo.OpenBoxVO;
import com.ruoyi.playingmethod.model.ApiWelfare;
import com.ruoyi.playingmethod.model.ApiWelfareRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApiWelfareService {

    /**
     * 获取福利列表
     */
    List<ApiWelfare> getWelfareList(Long userId);

    /**
     * 领取福利
     */
    OpenBoxVO claimWelfare(Integer welfareId, Long userId);

    /**
     * 检查是否不符合领取条件
     */
    boolean checkNotEligible(Integer welfareId, Long userId);

    /**
     * 检查是否已领取
     */
    boolean checkClaimed(Integer welfareId, Long userId);
}
