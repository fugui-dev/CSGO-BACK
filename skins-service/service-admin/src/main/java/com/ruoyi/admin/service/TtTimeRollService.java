package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.entity.roll.TtTimeRoll;
import com.ruoyi.domain.vo.TtRollPrizeDataVO;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.job.TaskException;
import org.quartz.SchedulerException;

import java.util.List;

public interface TtTimeRollService extends IService<TtTimeRoll> {

    String insertTimeRoll(TtTimeRoll ttTimeRoll) throws SchedulerException, TaskException;

    AjaxResult removeTimeRollById(Integer id) throws SchedulerException;

    String changeStatus(TtTimeRoll ttTimeRoll) throws SchedulerException;

    List<TtRollPrizeDataVO> getTimeRollPrizeList(Integer id);

    String namedWinner(TtRollPrizeDataVO rollPrizeData);
}
