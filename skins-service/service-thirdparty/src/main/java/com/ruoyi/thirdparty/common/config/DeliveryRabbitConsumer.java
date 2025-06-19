package com.ruoyi.thirdparty.common.config;

import com.rabbitmq.client.Channel;
import com.ruoyi.common.rabbitmq.config.DelayedQueueConfig;
import com.ruoyi.thirdparty.common.service.DeliverGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class DeliveryRabbitConsumer {

    private final DeliverGoodsService deliverGoodsService;

    public DeliveryRabbitConsumer(DeliverGoodsService deliverGoodsService) {
        this.deliverGoodsService = deliverGoodsService;
    }

    // 自动提货业务
    @RabbitListener(queues = DelayedQueueConfig.DELIVERY_QUEUE)
    public void deliveryReceive(Message message, Channel channel) throws IOException {
        Integer userId = Integer.valueOf(new String(message.getBody()));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            deliverGoodsService.autoDelivery(userId);
        } catch (Exception e) {
            log.error(e.getMessage());
            channel.basicRecover(false);
        }
    }
}