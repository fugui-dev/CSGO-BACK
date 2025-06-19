package com.ruoyi.promo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.entity.TtCommissionRecord;

public interface CommissionRecordService extends IService<TtCommissionRecord> {

    AjaxResult changeCommissionRecord(Integer commissionId, String operationBy);

}
