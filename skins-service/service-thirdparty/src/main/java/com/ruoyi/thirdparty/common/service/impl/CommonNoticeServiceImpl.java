package com.ruoyi.thirdparty.common.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.thirdparty.common.service.CommonNoticeService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommonNoticeServiceImpl implements CommonNoticeService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void sendTopUpRebateNotice(String userId, BigDecimal amount) {
        Map<String, String> noticeMap = new HashMap<>();
        noticeMap.put("userId", userId);
        noticeMap.put("title", "充值返利到账");
        noticeMap.put("content", "昨日充值返利" + amount + "已到账，请注意查收。");
        noticeMap.put("createTime", DateUtils.getTime());
        rabbitTemplate.convertAndSend("notice_queue", noticeMap);
    }

}
