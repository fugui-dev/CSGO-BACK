package com.ruoyi.user.consumer;

import com.rabbitmq.client.Channel;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.domain.other.TtNotice;
import com.ruoyi.user.service.ApiNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class NoticeConsumer {

    @Autowired
    private ApiNoticeService apiNoticeService;

    @RabbitListener(queues = "notice_queue")
    public void insertNotice(Map<String, String> noticeMap, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        String userId = noticeMap.get("userId");
        String title = noticeMap.get("title");
        String content = noticeMap.get("content");
        String createTime = noticeMap.get("createTime");
        TtNotice ttNotice = new TtNotice();
        ttNotice.setUserId(Long.valueOf(userId));
        ttNotice.setTitle(title);
        ttNotice.setContent(content);
        ttNotice.setCreateTime(DateUtils.parseDate(createTime));
        apiNoticeService.addNotice(ttNotice);

        try {
            //手动确认消费
            channel.basicAck(tag, false);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
