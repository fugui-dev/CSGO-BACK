package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtRechargeRecord;
import com.ruoyi.domain.other.TtRechargeRecordBody;

import java.util.List;

public interface TtRechargeRecordService extends IService<TtRechargeRecord> {

    List<TtRechargeRecord> queryList(TtRechargeRecordBody rechargeRecordBody);
}
