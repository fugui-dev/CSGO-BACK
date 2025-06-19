package com.ruoyi.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.task.TtTask;
import com.ruoyi.domain.task.TtTaskDoing;
import com.ruoyi.domain.task.VO.TtTaskDoingVO;

import java.util.List;

public interface TtTaskService extends IService<TtTask> {
    List<TtTaskDoingVO> taskOfme(Integer page, Integer size, Integer userId);

    AjaxResult getAward(Integer userId, Integer taskDoingid);

    AjaxResult firsDownLoadTask(Integer userId);
}
