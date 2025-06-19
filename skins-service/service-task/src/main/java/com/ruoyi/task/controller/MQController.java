package com.ruoyi.task.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.task.DTO.pWelfareMQData;
import com.ruoyi.domain.task.VO.TtTaskDoingVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static com.ruoyi.domain.task.constant.mq.MQMoudle.PROMOTION_WELFARE_EXCHANGE;
import static com.ruoyi.domain.task.constant.mq.MQMoudle.PROMOTION_WELFARE_KEY1;

@Slf4j
@RestController
@RequestMapping("/api/mq")
public class MQController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/t1")
    @Anonymous
    public AjaxResult t1() {

        log.debug("mq测试");
        return AjaxResult.success("mq测试");

    }

}
