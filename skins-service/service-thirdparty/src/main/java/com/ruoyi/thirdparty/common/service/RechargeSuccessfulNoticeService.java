package com.ruoyi.thirdparty.common.service;

import java.math.BigDecimal;

/**
 * 充值成功通知
 */
public interface RechargeSuccessfulNoticeService {

    /**
     * 发送充值成功通知
     */
    void sendRechargeSuccessNotice(String userId, BigDecimal amount);
}
