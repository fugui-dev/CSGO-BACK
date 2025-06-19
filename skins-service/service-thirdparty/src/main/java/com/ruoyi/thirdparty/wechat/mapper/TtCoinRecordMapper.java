package com.ruoyi.thirdparty.wechat.mapper;

import com.ruoyi.thirdparty.wechat.entity.TtCoinRecord;

/**
 * 金币变动记录Mapper接口
 * 
 * @author ruoyi
 * @date 2023-07-06
 */
public interface TtCoinRecordMapper 
{

    /**
     * 新增金币变动记录
     * 
     * @param ttCoinRecord 金币变动记录
     * @return 结果
     */
    public int insertTtCoinRecord(TtCoinRecord ttCoinRecord);


}
