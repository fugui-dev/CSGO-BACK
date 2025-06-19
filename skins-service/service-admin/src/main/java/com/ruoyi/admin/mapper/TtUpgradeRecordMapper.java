package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.dto.upgrade.UpgradeCondition;
import com.ruoyi.domain.other.TtUpgradeRecord;
import com.ruoyi.domain.other.TtUpgradeRecordBody;
import com.ruoyi.domain.vo.upgrade.UpgradeRecordVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TtUpgradeRecordMapper extends BaseMapper<TtUpgradeRecord> {
    List<UpgradeRecordVO> getUpgradeRecord(TtUpgradeRecordBody ttUpgradeRecordBody);

    List<UpgradeRecordVO> adminGetLog(UpgradeCondition param);
}
