package com.ruoyi.thirdparty.wechat.service;

import com.ruoyi.thirdparty.wechat.entity.TtCoinRechargeRecord;

/**
 * 充值记录Service接口
 * 
 * @author ruoyi
 * @date 2023-07-06
 */
public interface ITtCoinRechargeRecordService {

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
     * 根据订单号查询
     *
     * @param tradeNo
     * @return
     */
    TtCoinRechargeRecord selectTtCoinRechargeRecordByOrder(String tradeNo);

}
