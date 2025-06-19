package com.ruoyi.user.mapper;

import com.ruoyi.domain.vo.ApiMessageDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiMessageMapper {
    List<ApiMessageDataVO> getMessageList(@Param("userId") Long userId, @Param("id") Integer id);
}
