package com.ruoyi.playingmethod.mapper;

import com.ruoyi.domain.other.JackpotData;
import com.ruoyi.domain.vo.RollDetailsDataVO;
import com.ruoyi.domain.vo.RollListDataVO;
import com.ruoyi.playingmethod.controller.ApiRollController.GetRollListParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApiRollMapper {

    List<RollListDataVO> getRollList(GetRollListParam param);

    RollDetailsDataVO getRollDetails(Integer rollId);

    List<JackpotData> getJackpotData(Integer jackpotId);
}
