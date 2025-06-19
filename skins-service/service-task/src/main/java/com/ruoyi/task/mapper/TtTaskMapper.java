package com.ruoyi.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.task.TtTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TtTaskMapper extends BaseMapper<TtTask> {

    TtTask byId(Integer id);

    List<TtTask> listByState(@Param("state") Integer state);
}
