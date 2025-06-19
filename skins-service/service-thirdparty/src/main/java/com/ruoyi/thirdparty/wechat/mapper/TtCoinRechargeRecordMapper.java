package com.ruoyi.thirdparty.wechat.mapper;

import com.ruoyi.thirdparty.wechat.entity.TtCoinRechargeRecord;

/**
 * 充值记录Mapper接口
 *
 * @author ruoyi
 * @date 2023-07-06
 */
public interface TtCoinRechargeRecordMapper
{

    /**
     * 新增充值记录
     *
     * @param ttCoinRechargeRecord 充值记录
     * @return 结果
     */
    public int insertTtCoinRechargeRecord(TtCoinRechargeRecord ttCoinRechargeRecord);

    /**
     * 修改充值记录
     *
     * @param ttCoinRechargeRecord 充值记录
     * @return 结果
     */
    public int updateTtCoinRechargeRecord(TtCoinRechargeRecord ttCoinRechargeRecord);

    /**
     * 根据订单号查询数据
     *
     * @param orderNo
     * @return
     */
    TtCoinRechargeRecord selectTtCoinRechargeRecordByOrder(String orderNo);

}
