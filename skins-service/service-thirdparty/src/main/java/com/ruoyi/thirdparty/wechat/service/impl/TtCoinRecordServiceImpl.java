package com.ruoyi.thirdparty.wechat.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.thirdparty.wechat.entity.TtCoinRecord;
import com.ruoyi.thirdparty.wechat.mapper.TtCoinRecordMapper;
import com.ruoyi.thirdparty.wechat.service.ITtCoinRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 金币变动记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2023-07-06
 */
@Service
public class TtCoinRecordServiceImpl implements ITtCoinRecordService
{
    @Autowired
    private TtCoinRecordMapper ttCoinRecordMapper;

    /**
     * 新增金币变动记录
     *
     * @param ttCoinRecord 金币变动记录
     * @return 结果
     */
    @Override
    public int insertTtCoinRecord(TtCoinRecord ttCoinRecord)
    {
        ttCoinRecord.setCreateTime(DateUtils.getNowDate());
        return ttCoinRecordMapper.insertTtCoinRecord(ttCoinRecord);
    }


}
