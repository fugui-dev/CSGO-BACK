package com.ruoyi.playingmethod.scheduled;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.ruoyi.admin.mapper.TtBoxRecordsMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.playingmethod.service.ApiFightService;
import com.ruoyi.playingmethod.websocket.WsFightHall;
import com.ruoyi.playingmethod.websocket.WsFightRoom;
import com.ruoyi.playingmethod.websocket.util.WsResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.ruoyi.domain.common.constant.TtboxRecordStatus.IN_PACKSACK_ON;
import static com.ruoyi.playingmethod.websocket.constant.SMsgKey.ALL_FIGHT_ROOM;

@Slf4j
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class fightTask {

    @Value("${mkcsgo.fight.roundTime}")
    private final Integer fightRoundTime = null;

    @Autowired
    private ApiFightService fightService;

    @Autowired
    private TtBoxRecordsMapper boxRecordsMapper;

    @Autowired
    private TtBoxRecordsService boxRecordsService;

    @Autowired
    private ThreadPoolExecutor customThreadPoolExecutor;

    // 定时更新超时未结束的对局
//    @Scheduled(cron = "0/6 * * * * ?")
    private void refreshDayTask() {
        log.debug("定时更新超时未结束的对局");
        // Timestamp now = new Timestamp(System.currentTimeMillis());
        LambdaQueryWrapper<TtFight> fightQuery = new LambdaQueryWrapper<>();
        fightQuery
                .eq(TtFight::getStatus,1)
                .and(wrapper->{
                    wrapper.isNull(TtFight::getEndTime);
                })
                .and(wrapper->{
                    wrapper.isNotNull(TtFight::getBeginTime);
                })
                // TODO: 2024/4/13 优化sql
                // 对局开始时间早于当前时间减去一定时间间隔（再减去6秒），就会被认为是超时的对局
                // begin_time < DATE_SUB(NOW(), INTERVAL 100 SECOND)
                .last("AND begin_time < DATE_SUB(NOW(), INTERVAL " + fightRoundTime / 1000 + "* round_number + 6"+" second)");

        List<TtFight> list = fightService.list(fightQuery);

        if (list.isEmpty()) return;

        List<Integer> fightIds = new ArrayList<>();
        // 更新fight
        list.stream().forEach(item->{
            item.setEndTime(new Timestamp(System.currentTimeMillis()));
            item.setStatus(2);
            item.setRemark("超时强制更新结束状态");
            fightIds.add(item.getId());

            // 断开房间所有连接
            WsFightRoom.batchClose(item);
        });
        fightService.updateBatchById(list);

        // 异步更新对局数据
        CompletableFuture.runAsync(() -> {
            WsFightHall.broadcast(WsResult.ok(ALL_FIGHT_ROOM.name(), list));
        }, customThreadPoolExecutor);

        // 更新openBoxRecords
        new LambdaUpdateChainWrapper<>(boxRecordsMapper)
                // .eq(TtBoxRecords::getStatus,IN_PACKSACK_ON.getCode())
                .in(TtBoxRecords::getFightId,fightIds)
                .set(TtBoxRecords::getStatus,IN_PACKSACK_ON.getCode())
                .update();

        log.info("更新 {} 条超时未结束的对局记录",list.size());
    }
}
