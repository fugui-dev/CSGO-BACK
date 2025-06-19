package com.ruoyi.playingmethod.mapper;

import com.ruoyi.domain.other.TtBoxA;
import com.ruoyi.domain.vo.ApiFightListDataVO;
import com.ruoyi.domain.vo.JoinFightUserDataVO;
import com.ruoyi.domain.vo.UserPackSackDataVO;
import com.ruoyi.playingmethod.model.vo.ApiFightRankingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ApiFightMapper {
    JoinFightUserDataVO selectJoinFightUserData(Integer fightUserId);

    List<UserPackSackDataVO> selectOrnamentsDataListByUserIdAndFightId(@Param("userId") Integer userId, @Param("fightId") Integer fightId);

    List<ApiFightListDataVO> getFightList(@Param("model") String model, @Param("status") String status, @Param("userId") Integer userId, @Param("fightId") Integer fightId);

    List<TtBoxA> getFightBoxList(Integer boxTypeId);

    /**
     * 旧 == 》获取指定日期的对战排行榜
     * 新 == 》信逻辑改为总流水前50
     */
    List<ApiFightRankingVO> getFightRankingByDate(String date);
}
