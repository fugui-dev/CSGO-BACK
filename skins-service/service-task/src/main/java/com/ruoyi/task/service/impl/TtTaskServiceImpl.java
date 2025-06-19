package com.ruoyi.task.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.service.TtUserCreditsRecordsService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.entity.recorde.TtUserCreditsRecords;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.task.TtTask;
import com.ruoyi.domain.task.TtTaskDoing;
import com.ruoyi.domain.task.VO.TtTaskDoingVO;
import com.ruoyi.domain.task.constant.*;
import com.ruoyi.task.mapper.TtTaskMapper;
import com.ruoyi.task.service.TtTaskDoingService;
import com.ruoyi.task.service.TtTaskService;
import com.ruoyi.task.utils.TasKUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TtTaskServiceImpl extends ServiceImpl<TtTaskMapper, TtTask> implements TtTaskService {

    @Autowired
    private TtUserService userService;

    @Autowired
    private TtTaskDoingService ttTaskDoingService;

    @Autowired
    private TtTaskService ttTaskService;

    @Autowired
    private TtUserService ttUserService;

    @Autowired
    private TtUserCreditsRecordsService ttUserCreditsRecordsService;

    @Autowired
    private TasKUtil tasKUtil;

    @Override
    public List<TtTaskDoingVO> taskOfme(Integer page, Integer size, Integer userId) {

        // 用户信息
        TtUser userInfo = userService.getById(userId);

        // 尝试新接任务
        AjaxResult r1 = tryCreateNewTaskDoing(userInfo);

        // 维护任务
        AjaxResult r2 = maintainTask(userInfo);

        // 查询结果
        LambdaQueryWrapper<TtTaskDoing> ttTaskDoingQuery = new LambdaQueryWrapper<>();
        ttTaskDoingQuery
                .eq(TtTaskDoing::getUserId,userInfo.getUserId())
                .eq(TtTaskDoing::getCompletionState,TaskState.UP.getCode());
        List<TtTaskDoing> ownTask = ttTaskDoingService.list(ttTaskDoingQuery);
        return ownTask.stream().map((item)->{

            TtTask task = ttTaskService.getById(item.getTaskId());
            TtTaskDoingVO vo = new TtTaskDoingVO();
            BeanUtil.copyProperties(task,vo);
            vo.setTaskId(task.getId());
            vo.setTaskDoingId(item.getId());
            vo.setUserId(item.getUserId());
            vo.setBeginTime(item.getBeginTime());
            vo.setCompeteTime(item.getCompeteTime());
            vo.setCompletionState(item.getCompletionState());
            vo.setProgress(item.getProgress());
            return vo;

        }).collect(Collectors.toList());

    }

    private AjaxResult maintainTask(TtUser userInfo) {

        // 已接任务
        LambdaQueryWrapper<TtTaskDoing> ttTaskDoingQuery = new LambdaQueryWrapper<>();
        ttTaskDoingQuery
                .eq(TtTaskDoing::getUserId,userInfo.getUserId());
        List<TtTaskDoing> ownTask = ttTaskDoingService.list(ttTaskDoingQuery);

        ownTask.stream().forEach((item)->{
            if (!tasKUtil.updateTask(item)){
                log.warn("任务{}-类型{} 数据更新失败，检查任务配置信息！",item.getId(),item.getTaskId());
            }
        });


        // LambdaQueryWrapper<TtTask> ttTaskQuery = new LambdaQueryWrapper<>();
        // LambdaUpdateWrapper<TtTaskDoing> ttTaskDoingUpdate = new LambdaUpdateWrapper<>();
        //
        // // 维护普通任务{首次下载奖励}
        // ttTaskQuery
        //         .eq(TtTask::getId,1);
        // TtTask task1 = ttTaskService.getOne(ttTaskQuery);
        //
        // ttTaskDoingUpdate
        //         .eq(TtTaskDoing::getUserId,userInfo.getUserId())
        //         .eq(TtTaskDoing::getTaskId,1)
        //         .set(TtTaskDoing::getProgress,task1.getTargetValue())
        //         .set(TtTaskDoing::getCompletionState,TaskCompletionState.COMPLETION.getCode())
        //         .set(TtTaskDoing::getCompeteTime,new Timestamp(System.currentTimeMillis()));
        // ttTaskDoingService.update(ttTaskDoingUpdate);
        //
        //
        // // 维护每日任务{每日流水奖励}
        // ttTaskQuery.clear();
        // ttTaskQuery
        //         .eq(TtTask::getId,2);
        // TtTask task2 = ttTaskService.getOne(ttTaskQuery);
        //
        // Calendar nowDay = Calendar.getInstance();
        // nowDay.set(Calendar.HOUR_OF_DAY, 0);
        // nowDay.set(Calendar.MINUTE, 0);
        // nowDay.set(Calendar.SECOND, 0);
        // nowDay.set(Calendar.MILLISECOND, 0);
        // Timestamp toDay = new Timestamp(nowDay.getTimeInMillis());
        //
        // LambdaQueryWrapper<TtUserCreditsRecords> creditsRecordsQuery = new LambdaQueryWrapper<>();
        // creditsRecordsQuery
        //         .eq(TtUserCreditsRecords::getUserId,userInfo.getUserId())
        //         .ge(TtUserCreditsRecords::getCreateTime,toDay);
        // List<TtUserCreditsRecords> todayRecords = ttUserCreditsRecordsService.list(creditsRecordsQuery);
        //
        // // 累计今日流水
        // BigDecimal credits = new BigDecimal("0");
        // todayRecords.stream().forEach((item)->{
        //     if (item.getCredits().compareTo(BigDecimal.ZERO)>=0){
        //         credits.add(item.getCredits());
        //     }
        // });
        //
        // ttTaskDoingUpdate.clear();
        // ttTaskDoingUpdate
        //         .eq(TtTaskDoing::getUserId,userInfo.getUserId())
        //         .eq(TtTaskDoing::getTaskId,2)
        //         .set(TtTaskDoing::getProgress,credits.intValue())
        //         .set(credits.compareTo(new BigDecimal(task2.getTargetValue()))>=0,TtTaskDoing::getCompletionState,TaskCompletionState.COMPLETION.getCode())
        //         .set(credits.compareTo(new BigDecimal(task2.getTargetValue()))>=0,TtTaskDoing::getCompeteTime,new Timestamp(System.currentTimeMillis()));
        // ttTaskDoingService.update(ttTaskDoingUpdate);

        return AjaxResult.success();
    }

    private AjaxResult tryCreateNewTaskDoing(TtUser userInfo) {

        // 已接任务
        LambdaQueryWrapper<TtTaskDoing> ttTaskDoingQuery = new LambdaQueryWrapper<>();
        ttTaskDoingQuery
                .eq(TtTaskDoing::getUserId,userInfo.getUserId());
        List<TtTaskDoing> ownTask = ttTaskDoingService.list(ttTaskDoingQuery);

        // 所有上架的任务
        List<TtTask> allTask = baseMapper.listByState(TaskState.UP.getCode());

        // 筛选可接的新任务
        List<TtTask> filterT = filterTask(allTask,ownTask);

        // 自动接手新任务
        ArrayList<TtTaskDoing> newTaskDoings = new ArrayList<>();
        for (TtTask task:filterT){

            TtTaskDoing newTaskDoing = TtTaskDoing.builder()
                    .taskId(task.getId())
                    .userId(userInfo.getUserId())
                    .completionState(TaskCompletionState.DOING.getCode())
                    .beginTime(new Timestamp(System.currentTimeMillis()))
                    .progress(0)
                    .build();
            newTaskDoings.add(newTaskDoing);

        }
        // 保存新接的任务
        for (TtTaskDoing taskDoing:newTaskDoings){
            ttTaskDoingService.save(taskDoing);
        }

        return AjaxResult.success();
    }

    @Override
    public AjaxResult getAward(Integer userId, Integer taskDoingid) {

        // 任务是否存在
        TtTaskDoing taskDoing =  ttTaskDoingService.isOwnUser(userId,taskDoingid);
        if (ObjectUtil.isEmpty(taskDoing)){
            return AjaxResult.error("任务不存在");
        }

        // 是否可领取
        AjaxResult r = taskIsComplete(taskDoing);
        if (!r.isSuccess()){
            return r;
        }

        // 领奖
        getPrize(taskDoing);

        return AjaxResult.success("操作成功");
    }

    @Override
    public AjaxResult firsDownLoadTask(Integer userId) {

        // 是否已完成
        LambdaQueryWrapper<TtTaskDoing> ttTaskDoingQuery = new LambdaQueryWrapper<>();
        ttTaskDoingQuery
                .eq(TtTaskDoing::getUserId,userId)
                .eq(TtTaskDoing::getTaskId,1);
        TtTaskDoing one = ttTaskDoingService.getOne(ttTaskDoingQuery);
        if (ObjectUtil.isNotEmpty(one)){
            return AjaxResult.success("首次下载任务已经完成，不能重复执行。");
        }

        // 接手任务并直接完成
        TtTask task = baseMapper.byId(1);
        TtTaskDoing taskDoing = new TtTaskDoing();
        BeanUtil.copyProperties(task,taskDoing);
        taskDoing.setUserId(userId);
        taskDoing.setTaskId(task.getId());
        taskDoing.setProgress(1);
        taskDoing.setCompletionState(TaskCompletionState.COMPLETION.getCode());
        taskDoing.setCompeteTime(new Timestamp(System.currentTimeMillis()));
        ttTaskDoingService.save(taskDoing);

        return AjaxResult.success("任务完成。");

    }

    private AjaxResult getPrize(TtTaskDoing taskDoing) {
        TtTask task = baseMapper.byId(taskDoing.getTaskId());
        if (task.getAwardType().equals(TaskAwardType.CREDITS.getCode())){

            BigDecimal prize = BigDecimal.ZERO;

            if (ObjectUtil.isEmpty(task.getAwardValue())){
                // 暂时没有动态奖励的任务
                // 计算奖金
                // prize = computerPrize(task, taskDoing.getUserId());
            }
            prize = new BigDecimal(task.getAwardValue());

            TtUser user = ttUserService.getById(taskDoing.getUserId());
            BigDecimal add = user.getAccountCredits().add(prize);
            user.setAccountCredits(add);

            LambdaUpdateWrapper<TtUser> ttUserUpdate = new LambdaUpdateWrapper<>();
            ttUserUpdate
                    .eq(TtUser::getUserId,user.getUserId())
                    .set(TtUser::getAccountCredits,add);
            ttUserService.update(ttUserUpdate);
            userService.insertUserCreditsRecords(user.getUserId(), TtAccountRecordType.INPUT, TtAccountRecordSource.TASK, prize, add,taskDoing.getTaskId());

            taskDoing.setCompletionState(TaskCompletionState.COMPLETION_PRIZE.getCode());
            ttTaskDoingService.updateById(taskDoing);

            return AjaxResult.success("领取奖励：弹药"+add.toString());
        }else if (task.getAwardType().equals(TaskAwardType.OTHER.getCode())){
            System.out.println("其他奖励类型");
            return AjaxResult.error("其他奖励类型开发中。");
        }else {
            System.out.println("非法的奖励类型");
            return AjaxResult.error("非法的奖励类型。");
        }
    }

    private BigDecimal computerPrize(TtTask task,Integer userId){

        // 流水奖励动态计算奖金
        if (task.getId().equals(2)){

            Calendar nowDay = Calendar.getInstance();
            nowDay.set(Calendar.HOUR_OF_DAY, 0);
            nowDay.set(Calendar.MINUTE, 0);
            nowDay.set(Calendar.SECOND, 0);
            nowDay.set(Calendar.MILLISECOND, 0);
            Timestamp toDay = new Timestamp(nowDay.getTimeInMillis());

            nowDay.add(Calendar.DAY_OF_MONTH,-1);
            Timestamp yesterday = new Timestamp(nowDay.getTimeInMillis());

            LambdaQueryWrapper<TtUserCreditsRecords> creditsRecordsQuery = new LambdaQueryWrapper<>();
            creditsRecordsQuery
                    .eq(TtUserCreditsRecords::getUserId,userId)
                    .ge(TtUserCreditsRecords::getCreateTime,yesterday)
                    .le(TtUserCreditsRecords::getCreateTime,toDay);
            List<TtUserCreditsRecords> list = ttUserCreditsRecordsService.list(creditsRecordsQuery);

            BigDecimal prize = new BigDecimal("0");
            list.stream().forEach((item)->{
                if (item.getCredits().compareTo(BigDecimal.ZERO)>=0){
                    prize.add(item.getCredits());
                }
            });

            prize.multiply(new BigDecimal("0.01"));
            return prize;

        }else {
            return BigDecimal.ZERO;
        }

    }

    private AjaxResult taskIsComplete(TtTaskDoing taskDoing) {

        TtTask task = baseMapper.byId(taskDoing.getTaskId());
        if (task.getType().equals(TaskType.COMMON.getCode())){

            if (taskDoing.getCompletionState().equals(TaskCompletionState.COMPLETION_PRIZE.getCode())){
                return AjaxResult.error("不可重复领取奖励。");
            }
            if (taskDoing.getCompletionState().equals(TaskCompletionState.COMPLETION.getCode())){
                return AjaxResult.success();
            }

            // if (taskDoing.getProgress()>=task.getTargetValue()){
            //     return AjaxResult.success();
            // }
            return AjaxResult.error("任务未完成，不可领奖。");
        }else if (task.getType().equals(TaskType.DAY.getCode())){

            if (taskDoing.getCompletionState().equals(TaskCompletionState.COMPLETION.getCode())){
                return AjaxResult.success();
            }
            return AjaxResult.error("未完成/已领取");

        }else if (task.getType().equals(TaskType.WEEK.getCode())){
            System.out.println("WEEK");
            return AjaxResult.error("非法taskType");
        }else if (task.getType().equals(TaskType.MONTH.getCode())){
            System.out.println("MONTH");
            return AjaxResult.error("非法taskType");
        }else {
            System.out.println("非法任务类型");
            return AjaxResult.error("非法taskType");
        }

    }

    // 废弃方法
    private List<TtTaskDoingVO> getDoingTask(List<TtTask> filterT,List<TtTaskDoing> ownTask, TtUser userInfo) {

        ArrayList<TtTaskDoingVO> result = new ArrayList<>();

        // 自动接手新任务
        ArrayList<TtTaskDoing> newTaskDoings = new ArrayList<>();
        for (TtTask task:filterT){

            TtTaskDoing newTaskDoing = TtTaskDoing.builder()
                    .taskId(task.getId())
                    .userId(userInfo.getUserId())
                    .completionState(TaskCompletionState.DOING.getCode())
                    .beginTime(new Timestamp(System.currentTimeMillis()))
                    .progress(0)
                    .build();
            newTaskDoings.add(newTaskDoing);

            TtTaskDoingVO ttTaskDoingVO = new TtTaskDoingVO();
            BeanUtil.copyProperties(task,ttTaskDoingVO);
            ttTaskDoingVO.setCompletionState(newTaskDoing.getCompletionState());
            ttTaskDoingVO.setProgress(newTaskDoing.getProgress());
            result.add(ttTaskDoingVO);

        }
        // 保存新接的任务
        for (TtTaskDoing taskDoing:newTaskDoings){
            ttTaskDoingService.save(taskDoing);
        }

        List<TtTaskDoingVO> collect = ownTask.stream().map((item) -> {
            TtTaskDoingVO vo = new TtTaskDoingVO();
            TtTask byId = baseMapper.byId(item.getTaskId());
            BeanUtil.copyProperties(byId, vo);
            vo.setTaskId(byId.getId());
            vo.setTaskDoingId(item.getId());
            vo.setCompletionState(item.getCompletionState());
            vo.setProgress(item.getProgress());
            vo.setBeginTime(item.getBeginTime());
            vo.setCompeteTime(item.getCompeteTime());
            return vo;
        }).collect(Collectors.toList());
        // 维护旧任务
        collect.stream().forEach((item)->{
            if (item.getType().equals(TaskType.DAY.getCode())){

                // 当天零点时间戳
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                Timestamp currenDay = new Timestamp(c.getTimeInMillis());

                // 任务开始时间
                Timestamp beginTime = item.getBeginTime();
                // 任务完成时间
                Timestamp competeTime = item.getCompeteTime();

                // 尝试重置当日任务
                // 完成时间为空或小于当天零点 1刷新开始时间，2状态更新为完成
                if (ObjectUtil.isEmpty(competeTime) || competeTime.compareTo(currenDay)<0){
                    UpdateWrapper<TtTaskDoing> updateWrapper = new UpdateWrapper<TtTaskDoing>();
                    updateWrapper
                            .eq("id",item.getTaskDoingId())
                            .set("begin_time",new Timestamp(System.currentTimeMillis()))
                            .set("completion_state",TaskCompletionState.COMPLETION.getCode());
                    ttTaskDoingService.update();
                }
                // 开始时间小于当天零点 并 未完成 刷新开始时间，状态更新为完成
                // 开始时间大于当天零点 并 进行中 1状态更新为完成，2更新完成时间
                // if (beginTime.compareTo(currenDay)>=0 && item.getCompletionState().equals(TaskCompletionState.DOING.getCode())){
                //     UpdateWrapper<TtTaskDoing> updateWrapper = new UpdateWrapper<TtTaskDoing>();
                //     updateWrapper
                //             .eq("id",item.getTaskDoingId())
                //             .set("completion_state",TaskCompletionState.COMPLETION.getCode())
                //             .set("compete_time",new Timestamp(System.currentTimeMillis()));
                //     ttTaskDoingService.update();
                // }
                // 开始时间大于当天零点 并 已完成 不处理

            }else if (item.getType().equals(TaskType.WEEK.getCode())){

            }else if (item.getType().equals(TaskType.MONTH.getCode())){

            }else {

            }
        });

        // 合并所有正在进行的任务
        result.addAll(collect);

        return result;
    }

    //筛选可接的新任务
    private List<TtTask> filterTask(List<TtTask> allTask,List<TtTaskDoing> ownTask) {

        ArrayList<TtTask> result = new ArrayList<>();

        // 条件过滤
        for (TtTask task:allTask){

            String condition = task.getTaskCondition();
            if (StringUtils.isBlank(condition)||condition.equals("{}")){
                result.add(task);
                continue;
            }

            JSONObject conditionObj = JSON.parseObject(condition);
            if (conditionObj.get("key1").equals("key1")){
                System.out.println("key1");
            }else if (conditionObj.get("key2").equals("key2")){
                System.out.println("key2");
            }

        }

        // 筛选可接的新任务
        ArrayList<TtTask> canDoingTasks = new ArrayList<>();
        for (TtTask task:result){

            TtTaskDoing flag = null;
            for (TtTaskDoing taskDoing:ownTask){
                if (taskDoing.getTaskId().equals(task.getId())){
                    flag = taskDoing;
                    break;
                }
            }

            if (ObjectUtil.isEmpty(flag)){
                canDoingTasks.add(task);
            }
        }

        return canDoingTasks;
    }
}
