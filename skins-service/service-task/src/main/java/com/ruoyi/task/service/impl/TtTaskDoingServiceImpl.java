package com.ruoyi.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.domain.task.TtTaskDoing;
import com.ruoyi.task.mapper.TtTaskDoingMapper;
import com.ruoyi.task.service.TtTaskDoingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TtTaskDoingServiceImpl extends ServiceImpl<TtTaskDoingMapper, TtTaskDoing> implements TtTaskDoingService {

    @Autowired
    private TtUserService userService;

    @Override
    public TtTaskDoing isOwnUser(Integer userId, Integer tid) {
        return baseMapper.isOwnUser(userId,tid);
    }
}
