package com.ruoyi.playingmethod.mapper;

import com.ruoyi.domain.vo.UserPackSackDataVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApiCompoundMapper {
    UserPackSackDataVO selectCompoundDataById(Long packSackId);

    List<UserPackSackDataVO> selectCompoundRecordByUserId(Integer userId);
}
