package com.ruoyi.promo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.entity.TtCommissionRecord;
import com.ruoyi.promo.mapper.TtCommissionRecordMapper;
import com.ruoyi.promo.service.CommissionRecordService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class CommissionRecordServiceImpl extends ServiceImpl<TtCommissionRecordMapper, TtCommissionRecord> implements CommissionRecordService {
    @Override
    public AjaxResult changeCommissionRecord(Integer commissionId, String operationBy) {

        TtCommissionRecord commissionRecord = this.getById(commissionId);
        if (commissionRecord == null){
            return AjaxResult.error("不存在的佣金记录！");
        }
        if ("1".equals(commissionRecord.getClaimStatus())){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return AjaxResult.error("日期：" + dateFormat.format(commissionRecord.getSummaryTime()) + "的佣金已领取！");
        }

        //修改为已领取
        commissionRecord.setClaimStatus("1");
        commissionRecord.setClaimTime(new Date());
        commissionRecord.setOperationBy(operationBy);
        boolean update = commissionRecord.updateById();

        if (update){
            return AjaxResult.success("领取成功！");
        }

        return AjaxResult.error("领取失败！");

    }
}
