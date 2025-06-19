package com.ruoyi.playingmethod.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.upgrade.UpgradeCondition;
import com.ruoyi.domain.other.*;


import java.util.List;

public interface ApiUpgradeRecordService extends IService<TtUpgradeRecord> {

    R historyDetail(UpgradeCondition param);
}
