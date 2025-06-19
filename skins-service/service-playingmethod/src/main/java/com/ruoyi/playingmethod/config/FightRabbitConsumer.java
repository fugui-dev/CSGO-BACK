package com.ruoyi.playingmethod.config;

import com.rabbitmq.client.Channel;
import com.ruoyi.admin.service.TtFightService;
import com.ruoyi.common.rabbitmq.config.DelayedQueueConfig;
import com.ruoyi.common.rabbitmq.config.FightQueueConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.playingmethod.service.ApiRollService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@Slf4j
public class FightRabbitConsumer {

    // private final TtFightService ttFightService;
    //
    // public FightRabbitConsumer(TtFightService ttFightService) {
    //     this.ttFightService = ttFightService;
    // }
    //
    // @RabbitListener(queues = FightQueueConfig.FIGHT_QUEUE)
    // public void rollReceiveDelayed(String fightId,Message message, Channel channel) throws IOException {
    //     log.info("===============接收对战队列消息====================" + fightId);
    //
    //     // 通知 MQ 消息已被接收,可以ACK(从队列中删除)了
    //     channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    //     try {
    //         ttFightService.endFight(fightId);
    //     } catch (Exception e) {
    //         log.error("============消费失败,尝试消息补发再次消费!==============");
    //         log.error(e.getMessage());
    //         // 消息补发 true=消息退回到queue(有可能被其它的consumer(集群)接收到) false=只补发给当前的consumer
    //         channel.basicRecover(false);
    //     }
    // }
}
