package com.ruoyi.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.task.TtTask;
import com.ruoyi.domain.task.TtTaskDoing;

import java.util.List;

public interface TtTaskDoingService extends IService<TtTaskDoing> {
    TtTaskDoing isOwnUser(Integer userId, Integer tid);
}
