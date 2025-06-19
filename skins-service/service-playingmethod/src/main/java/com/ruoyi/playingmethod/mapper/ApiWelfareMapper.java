package com.ruoyi.playingmethod.mapper;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.playingmethod.model.ApiWelfare;
import com.ruoyi.playingmethod.model.ApiWelfareRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiWelfareMapper {

    List<ApiWelfare> getWelfareList(Long userId);

    /**
     * 检查是否已领取
     */
    boolean checkClaimed(@Param("welfareId") Integer welfareId, @Param("userId") Long userId);

    Integer getBoxIdByWelfareId(Integer welfareId);

    int saveClaimWelfareRecord(ApiWelfareRecord apiWelfareRecord);
}
