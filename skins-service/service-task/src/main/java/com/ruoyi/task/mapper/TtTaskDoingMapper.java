package com.ruoyi.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.task.TtTask;
import com.ruoyi.domain.task.TtTaskDoing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TtTaskDoingMapper extends BaseMapper<TtTaskDoing> {

    TtTaskDoing isOwnUser(@Param("userId") Integer userId,@Param("tid") Integer tid);
}
