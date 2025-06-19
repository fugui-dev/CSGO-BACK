package com.ruoyi.task.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.admin.service.TtUserCreditsRecordsService;
import com.ruoyi.domain.entity.recorde.TtUserCreditsRecords;
import com.ruoyi.domain.task.TtTask;
import com.ruoyi.domain.task.TtTaskDoing;
import com.ruoyi.domain.task.constant.TaskCompletionState;
import com.ruoyi.task.service.TtTaskDoingService;
import com.ruoyi.task.service.TtTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

@Configuration
public class TasKUtil {

    @Autowired
    private TtTaskService ttTaskService;

    @Autowired
    private TtTaskDoingService ttTaskDoingService;

    @Autowired
    private TtUserCreditsRecordsService ttUserCreditsRecordsService;

    // 任务更新
    public boolean updateTask(TtTaskDoing ttTaskDoing){

        Integer taskId = ttTaskDoing.getTaskId();

        LambdaQueryWrapper<TtTask> ttTaskQuery = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<TtTaskDoing> ttTaskDoingUpdate = new LambdaUpdateWrapper<>();
        if (taskId.equals(1)){

            // {首次下载奖励}
            ttTaskQuery
                    .eq(TtTask::getId,1);
            TtTask task1 = ttTaskService.getOne(ttTaskQuery);

            ttTaskDoingUpdate
                    .eq(TtTaskDoing::getUserId,ttTaskDoing.getUserId())
                    .eq(TtTaskDoing::getTaskId,1)
                    .set(TtTaskDoing::getProgress,task1.getTargetValue())
                    .set(TtTaskDoing::getCompletionState, TaskCompletionState.COMPLETION.getCode())
                    .set(TtTaskDoing::getCompeteTime,new Timestamp(System.currentTimeMillis()));
            ttTaskDoingService.update(ttTaskDoingUpdate);

            return true;
        }else if (taskId.equals(2)){

            // {每日流水奖励}
            ttTaskQuery.clear();
            ttTaskQuery
                    .eq(TtTask::getId,2);
            TtTask task2 = ttTaskService.getOne(ttTaskQuery);

            Calendar nowDay = Calendar.getInstance();
            nowDay.set(Calendar.HOUR_OF_DAY, 0);
            nowDay.set(Calendar.MINUTE, 0);
            nowDay.set(Calendar.SECOND, 0);
            nowDay.set(Calendar.MILLISECOND, 0);
            Timestamp toDay = new Timestamp(nowDay.getTimeInMillis());

            LambdaQueryWrapper<TtUserCreditsRecords> creditsRecordsQuery = new LambdaQueryWrapper<>();
            creditsRecordsQuery
                    .eq(TtUserCreditsRecords::getUserId,ttTaskDoing.getUserId())
                    .ge(TtUserCreditsRecords::getCreateTime,toDay);
            List<TtUserCreditsRecords> todayRecords = ttUserCreditsRecordsService.list(creditsRecordsQuery);

            // 累计今日流水
            BigDecimal credits = new BigDecimal("0");
            todayRecords.stream().forEach((item)->{
                if (item.getCredits().compareTo(BigDecimal.ZERO)>=0){
                    credits.add(item.getCredits());
                }
            });

            ttTaskDoingUpdate.clear();
            ttTaskDoingUpdate
                    .eq(TtTaskDoing::getUserId,ttTaskDoing.getUserId())
                    .eq(TtTaskDoing::getTaskId,2)
                    .set(TtTaskDoing::getProgress,credits.intValue())
                    .set(credits.compareTo(new BigDecimal(task2.getTargetValue()))>=0,TtTaskDoing::getCompletionState,TaskCompletionState.COMPLETION.getCode())
                    .set(credits.compareTo(new BigDecimal(task2.getTargetValue()))>=0,TtTaskDoing::getCompeteTime,new Timestamp(System.currentTimeMillis()));
            ttTaskDoingService.update(ttTaskDoingUpdate);

            return true;
        }else {
            return false;
        }

    }

}
