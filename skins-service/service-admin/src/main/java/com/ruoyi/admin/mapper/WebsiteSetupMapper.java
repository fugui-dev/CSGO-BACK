package com.ruoyi.admin.mapper;

import com.ruoyi.domain.other.ConfigData;
import com.ruoyi.domain.other.OperationalStatistics;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WebsiteSetupMapper {

    List<OperationalStatistics> getOperationalStatistics();

    List<ConfigData> selectParameterSettingList();

    void truncateSysJobLog();

    void truncateSysLogininfor();

    void truncateSysOperLog();

    void truncateTtBoxRecords();

    void truncateTtFight();

    void truncateTtFightResult();

    void truncateTtFightUser();

    void truncateTtVipLevel();

    void truncateTtPromotionLevel();

    void truncateTtOrnamentsLevel();
}
