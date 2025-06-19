package com.ruoyi.user.mapper;

import com.ruoyi.domain.vo.UserPackSackDataVO;
import com.ruoyi.domain.vo.client.PackSackGlobalData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiUserPackSackMapper {
    List<UserPackSackDataVO> getPackSack(Integer userId);

    List<UserPackSackDataVO> clientPackSack(
            @Param("uidList") List<Integer> uidList,
            @Param("statusList") List<Integer> statusList,
            @Param("name") String name,
            @Param("beginTime") String beginTime,
            @Param("endTime") String endTime,
            @Param("orderByFie") Integer orderByFie,
            @Param("orderByType") Integer orderByType);

    PackSackGlobalData packSackGlobalData(@Param("userId") Integer userId);
}
