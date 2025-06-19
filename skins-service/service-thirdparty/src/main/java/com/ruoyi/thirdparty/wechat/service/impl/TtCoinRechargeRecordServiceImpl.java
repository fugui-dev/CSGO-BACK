package com.ruoyi.thirdparty.wechat.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.thirdparty.wechat.entity.TtCoinRechargeRecord;
import com.ruoyi.thirdparty.wechat.mapper.TtCoinRechargeRecordMapper;
import com.ruoyi.thirdparty.wechat.service.ITtCoinRechargeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 充值记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2023-07-06
 */
@Service
public class TtCoinRechargeRecordServiceImpl implements ITtCoinRechargeRecordService
{
    @Autowired
    private TtCoinRechargeRecordMapper ttCoinRechargeRecordMapper;



    /**
     * 新增充值记录
     * 
     * @param ttCoinRechargeRecord 充值记录
     * @return 结果
     */
    @Override
    public int insertTtCoinRechargeRecord(TtCoinRechargeRecord ttCoinRechargeRecord)
    {
        ttCoinRechargeRecord.setCreateTime(DateUtils.getNowDate());
        return ttCoinRechargeRecordMapper.insertTtCoinRechargeRecord(ttCoinRechargeRecord);
    }

    /**
     * 修改充值记录
     * 
     * @param ttCoinRechargeRecord 充值记录
     * @return 结果
     */
    @Override
    public int updateTtCoinRechargeRecord(TtCoinRechargeRecord ttCoinRechargeRecord)
    {
        ttCoinRechargeRecord.setUpdateTime(DateUtils.getNowDate());
        return ttCoinRechargeRecordMapper.updateTtCoinRechargeRecord(ttCoinRechargeRecord);
    }


    @Override
    public TtCoinRechargeRecord selectTtCoinRechargeRecordByOrder(String tradeNo) {
        return ttCoinRechargeRecordMapper.selectTtCoinRechargeRecordByOrder(tradeNo);
    }


}
