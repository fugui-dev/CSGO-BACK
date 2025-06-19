package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.other.TtFightUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TtFightUserMapper extends BaseMapper<TtFightUser> {
    List<Integer> myOwnFights(@Param("playerId") Integer playerId);
}
