package com.ruoyi.playingmethod.mapper;

import com.ruoyi.domain.other.TtBoxA;
import com.ruoyi.domain.other.TtOrnamentsA;
import com.ruoyi.domain.other.TtBoxLevelA;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiBindBoxMapper {

    TtBoxA getBoxData(Integer boxId);

    List<TtOrnamentsA> getBoxOrnamentsList(Integer boxId);

    List<TtBoxA> getBoxList(@Param("boxTypeId") Integer boxTypeId, @Param("homeFlag") String homeFlag, @Param("isFight") Integer isFight);

    TtOrnamentsA getOrnamentsData(@Param("boxId") Integer boxId, @Param("ornamentsId") Long ornamentsId, @Param("id") Long id);

    // List<TtBoxUser> getBindBoxHistory(ApiBindBoxController.boxHistoryParam param);

    List<TtBoxLevelA> getProbabilityDistribution(Integer boxId);

    TtOrnamentsA ornamentsInfo(@Param("boxId") Integer boxId,@Param("ornamentId") String ornamentId);
}
