package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.roll.TtTimeRoll;
import com.ruoyi.domain.entity.roll.TtTimeRollUser;
import com.ruoyi.admin.mapper.TtBoxRecordsMapper;
import com.ruoyi.admin.mapper.TtTimeRollMapper;
import com.ruoyi.admin.service.TtTimeRollService;
import com.ruoyi.admin.service.TtTimeRollUserService;
import com.ruoyi.domain.vo.TtRollPrizeDataVO;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.job.TaskException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.quartz.domain.SysJob;
import com.ruoyi.quartz.service.ISysJobService;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ruoyi.domain.common.constant.TtboxRecordSource.ROLL;

@Service
public class TtTimeRollServiceImpl extends ServiceImpl<TtTimeRollMapper, TtTimeRoll> implements TtTimeRollService {

    private final ISysJobService sysJobService;
    private final TtTimeRollUserService timeRollUserService;
    private final TtBoxRecordsMapper boxRecordsMapper;

    public TtTimeRollServiceImpl(ISysJobService sysJobService,
                                 TtTimeRollUserService timeRollUserService,
                                 TtBoxRecordsMapper boxRecordsMapper) {
        this.sysJobService = sysJobService;
        this.timeRollUserService = timeRollUserService;
        this.boxRecordsMapper = boxRecordsMapper;
    }

    @Override
    public String insertTimeRoll(TtTimeRoll ttTimeRoll) throws SchedulerException, TaskException {
        this.save(ttTimeRoll);
        SysJob sysJob = new SysJob();
        sysJob.setJobGroup("TimeRollTask");
        sysJob.setInvokeTarget("TimeRollTask.timeRollSettle(" + ttTimeRoll.getId() + ")");
        sysJob.setCreateBy(SecurityUtils.getUsername());
        sysJob.setCreateTime(DateUtils.getNowDate());
        if ("0".equals(ttTimeRoll.getRechargeCondition())) {
            sysJob.setJobName("0");
            sysJob.setCronExpression("0 0 * * * ?");
        } else if ("1".equals(ttTimeRoll.getRechargeCondition())) {
            sysJob.setJobName("1");
            sysJob.setCronExpression("0 0 0 * * ?");
        } else if ("2".equals(ttTimeRoll.getRechargeCondition())) {
            sysJob.setJobName("2");
            sysJob.setCronExpression("0 0 0 ? * 1");
        } else if ("3".equals(ttTimeRoll.getRechargeCondition())) {
            sysJob.setJobName("3");
            sysJob.setCronExpression("0 0 0 1 * ?");
        }
        sysJobService.insertJob(sysJob);
        ttTimeRoll.setJobId(sysJob.getJobId().intValue());
        this.updateById(ttTimeRoll);
        return "";
    }

    @Override
    public AjaxResult removeTimeRollById(Integer id) {
        this.removeById(id);
        return AjaxResult.success();
    }

    @Override
    public String changeStatus(TtTimeRoll ttTimeRoll) throws SchedulerException {
        TtTimeRoll timeRoll = this.getById(ttTimeRoll.getId());
        timeRoll.setStatus(ttTimeRoll.getStatus());
        this.updateById(timeRoll);
        SysJob newJob = sysJobService.selectJobById(timeRoll.getJobId().longValue());
        newJob.setStatus(ttTimeRoll.getStatus());
        sysJobService.changeStatus(newJob);
        return "";
    }

    @Override
    public List<TtRollPrizeDataVO> getTimeRollPrizeList(Integer id) {
        List<TtRollPrizeDataVO> rollJackpotOrnamentsList = baseMapper.getRollJackpotOrnamentsList(id);
        List<TtRollPrizeDataVO> list = new ArrayList<>();
        for (TtRollPrizeDataVO rollPrizeData : rollJackpotOrnamentsList) {
            Integer count = new LambdaQueryChainWrapper<>(timeRollUserService.getBaseMapper())
                    .eq(TtTimeRollUser::getTimeRollId, rollPrizeData.getRollId())
                    .eq(TtTimeRollUser::getJackpotOrnamentsId, rollPrizeData.getJackpotOrnamentsListId())
                    .eq(TtTimeRollUser::getEndStatus, "0")
                    .count();
            for (int i = 0; i < rollPrizeData.getOrnamentNum() - count; i++) {
                list.add(rollPrizeData);
            }
        }
        List<TtRollPrizeDataVO> specifiedRollJackpotOrnamentsList = baseMapper.getSpecifiedTimeRollJackpotOrnamentsList(id);
        list.addAll(specifiedRollJackpotOrnamentsList);
        return list.stream().peek(rollPrizeData -> rollPrizeData.setOrnamentNum(1)).collect(Collectors.toList());
    }

    @Override
    public String namedWinner(TtRollPrizeDataVO rollPrizeData) {
        rollPrizeData.setDesignatedBy("");
        if (StringUtils.isNotNull(rollPrizeData.getRollUserId())) {
            TtTimeRollUser ttTimeRollUser = timeRollUserService.getById(rollPrizeData.getRollUserId());
            if (!Objects.equals(rollPrizeData.getUserId(), ttTimeRollUser.getUserId())) {
                ttTimeRollUser.setUserId(rollPrizeData.getUserId());
                ttTimeRollUser.setUpdateTime(DateUtils.getNowDate());
                timeRollUserService.updateById(ttTimeRollUser);
                TtBoxRecords ttBoxRecords = boxRecordsMapper.selectById(ttTimeRollUser.getBoxRecordId());
                ttBoxRecords.setUserId(rollPrizeData.getUserId());
                ttBoxRecords.setUpdateTime(DateUtils.getNowDate());
                ttBoxRecords.setHolderUserId(rollPrizeData.getUserId());
                boxRecordsMapper.updateById(ttBoxRecords);
            }
            return "";
        }
        TtBoxRecords boxRecords = TtBoxRecords.builder().build();
        boxRecords.setUserId(rollPrizeData.getUserId());
        boxRecords.setOrnamentId(rollPrizeData.getOrnamentsId());
        boxRecords.setOrnamentsPrice(rollPrizeData.getUsePrice());
        boxRecords.setOrnamentsLevelId(rollPrizeData.getOrnamentsLevelId());
        boxRecords.setStatus(TtboxRecordStatus.IN_PACKSACK_ON.getCode());
        boxRecords.setCreateTime(DateUtils.getNowDate());
        boxRecords.setSource(ROLL.getCode());
        boxRecords.setRollId(rollPrizeData.getRollId());
        boxRecords.setHolderUserId(rollPrizeData.getUserId());
        if (boxRecordsMapper.insert(boxRecords) > 0) {
            TtTimeRollUser timeRollUser = TtTimeRollUser.builder().build();
            timeRollUser.setTimeRollId(rollPrizeData.getRollId());
            timeRollUser.setUserId(rollPrizeData.getUserId());
            timeRollUser.setJackpotOrnamentsId(rollPrizeData.getJackpotOrnamentsListId());
            timeRollUser.setOrnamentsId(rollPrizeData.getOrnamentsId());
            timeRollUser.setBoxRecordId(boxRecords.getId());
            timeRollUser.setStatus("2");
            timeRollUser.setDesignatedBy(SecurityUtils.getUsername());
            timeRollUser.setJoinTime(DateUtils.getNowDate());
            if (timeRollUserService.save(timeRollUser)) return "";
        }
        return "error！";
    }
}
