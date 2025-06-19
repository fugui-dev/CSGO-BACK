package com.ruoyi.playingmethod.mapper;

import com.ruoyi.domain.dto.upgrade.UpgradeCondition;
import com.ruoyi.domain.vo.ApiLuckyOrnamentsDataVO;
import com.ruoyi.domain.other.ApiLuckyUpgradeBody;
import com.ruoyi.domain.vo.ApiLuckyUpgradeRecordDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiLuckyUpgradeMapper {
    List<ApiLuckyOrnamentsDataVO> getOrnamentsList(ApiLuckyUpgradeBody apiLuckyUpgradeBody);

    List<ApiLuckyUpgradeRecordDataVO> getUpgradeRecord(UpgradeCondition param);
}
