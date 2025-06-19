package com.ruoyi.playingmethod.scheduled;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.ruoyi.admin.mapper.TtBoxRecordsMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtRollService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.rabbitmq.config.DelayedQueueConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.entity.roll.TtRoll;
import com.ruoyi.playingmethod.service.ApiFightService;
import com.ruoyi.playingmethod.websocket.WsFightHall;
import com.ruoyi.playingmethod.websocket.WsFightRoom;
import com.ruoyi.playingmethod.websocket.util.WsResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.ruoyi.domain.common.constant.TtboxRecordStatus.IN_PACKSACK_ON;
import static com.ruoyi.playingmethod.websocket.constant.SMsgKey.ALL_FIGHT_ROOM;

@Slf4j
@Configuration      // 1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class RollTask {

    @Value("${mkcsgo.fight.roundTime}")
    private final Integer fightRoundTime = null;

    @Autowired
    private ApiFightService fightService;

    @Autowired
    private TtBoxRecordsMapper boxRecordsMapper;

    @Autowired
    private TtBoxRecordsService boxRecordsService;

    @Autowired
    private TtRollService ttRollService;

    @Autowired
    private ThreadPoolExecutor customThreadPoolExecutor;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 定时检查即将开奖的roll房（临界区：2分钟）
    @Scheduled(cron = "0 */2 * * * ?")
    private void refreshDayTask() {

        //System.out.println("roll 开奖临界区检查");

        // 查询两分钟以内将要开奖的roll房
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 2);
        Timestamp criticalTime = new Timestamp(c.getTimeInMillis());
        LambdaQueryWrapper<TtRoll> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(TtRoll::getRollStatus, 0)
                .eq(TtRoll::getDelFlag, 0)
                .lt(TtRoll::getEndTime, criticalTime);
        List<TtRoll> criticalRoll = ttRollService.list(wrapper);

        if (!criticalRoll.isEmpty()){
            log.info("roll开奖临界区检查，{}个roll房进入临界区",criticalRoll.size());
        }

        // 加入延时队列
        for (TtRoll roll : criticalRoll) {

            //相差时间
            long betweenDay = DateUtil.between(DateUtils.getNowDate(), roll.getEndTime(), DateUnit.MS);

            if (betweenDay < 0) {
                // 直接开奖 todo !!!
            }

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setExpiration(String.valueOf(betweenDay)); // 消息的过期属性，单位 ms
            Message message = new Message(String.valueOf(roll.getId()).getBytes(), messageProperties);

            // 延时队列实现开奖
            rabbitTemplate.convertAndSend(
                    DelayedQueueConfig.OPEN_ROLL_EXCHANGE,
                    DelayedQueueConfig.OPEN_ROLL_KEY,
                    message);

            log.info("roll房{}-{}加入临界区,{}秒后开奖",roll.getId(),roll.getRollName(),betweenDay*1000);
        }

    }

}
