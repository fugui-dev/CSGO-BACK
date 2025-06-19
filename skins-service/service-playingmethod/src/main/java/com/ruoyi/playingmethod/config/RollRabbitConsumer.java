package com.ruoyi.playingmethod.config;

import com.rabbitmq.client.Channel;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.rabbitmq.config.DelayedQueueConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.playingmethod.service.ApiRollService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class RollRabbitConsumer {

    private final ApiRollService apiRollService;

    public RollRabbitConsumer(ApiRollService apiRollService) {
        this.apiRollService = apiRollService;
    }

    // roll房开奖
    @RabbitListener(queues = DelayedQueueConfig.DLK_QUEUE)
    public void rollReceiveDelayed(Message message, Channel channel) throws IOException {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        Integer rollId = Integer.valueOf(new String(message.getBody()));
        log.info("roll房开奖,接收时间:{},接受到的Roll房ID:{}", DateUtils.getNowDate(), rollId);

        try {
            // Roll房分配结果逻辑
            R r = apiRollService.endROLL(rollId);
            if (!r.getCode().equals(200)){
                log.error("roll开奖 消费失败,尝试消息补发再次消费!"+r.getMsg());
                channel.basicNack(deliveryTag,true,true);
            }

            // 通知 MQ 消息已被接收,可以ACK(从队列中删除)了
            channel.basicAck(deliveryTag, true);
        } catch (Exception e) {
            log.error("roll开奖 消费失败,尝试消息补发再次消费!");
            log.error(e.getMessage());
            channel.basicNack(deliveryTag,true,true);
        }
    }

    //@RabbitListener(queues = DelayedQueueConfig.DLK_QUEUE)
    public void t1(Message message, Channel channel) throws IOException {

        //long deliveryTag = message.getMessageProperties().getDeliveryTag();

        log.info("mq 测试");

        System.out.println(new String(message.getBody()));
    }
}
