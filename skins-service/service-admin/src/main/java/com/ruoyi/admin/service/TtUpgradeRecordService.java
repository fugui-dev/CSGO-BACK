package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.domain.dto.upgrade.UpgradeCondition;
import com.ruoyi.domain.other.TtUpgradeRecord;
import com.ruoyi.domain.other.TtUpgradeRecordBody;
import com.ruoyi.domain.vo.upgrade.UpgradeRecordVO;

import java.util.List;

public interface TtUpgradeRecordService extends IService<TtUpgradeRecord> {

    List<UpgradeRecordVO> getUpgradeRecord(TtUpgradeRecordBody ttUpgradeRecordBody);

    R<Page<TtUpgradeRecord>> historyDetail(UpgradeCondition param);

    R<Page<UpgradeRecordVO>> adminGetLog(UpgradeCondition param);
}
