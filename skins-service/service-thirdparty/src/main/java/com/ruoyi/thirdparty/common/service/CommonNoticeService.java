package com.ruoyi.thirdparty.common.service;

import java.math.BigDecimal;

/**
 * 充值成功通知
 */
public interface CommonNoticeService {

    /**
     * 发送返利成功通知
     */
    void sendTopUpRebateNotice(String userId, BigDecimal amount);

}
