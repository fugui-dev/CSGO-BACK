package com.ruoyi.playingmethod.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.fight.FightDetailParam;
import com.ruoyi.domain.dto.fight.FightOnMyOwnParam;
import com.ruoyi.domain.other.CreateFightBody;
import com.ruoyi.domain.other.FightBoutData;
import com.ruoyi.domain.other.TtBoxVO;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.vo.ApiFightListDataVO;
import com.ruoyi.domain.vo.FightResultDataVO;
import com.ruoyi.domain.vo.fight.FightResultVO;
import com.ruoyi.domain.vo.fight.TtFightVO;
import com.ruoyi.playingmethod.model.vo.ApiFightRankingVO;

import java.util.Date;
import java.util.List;

public interface ApiFightService extends IService<TtFight> {

    R<Object> createFight(CreateFightBody createFightParamVO, TtUser ttUser);

    R<Object> joinFight(Integer fightId, TtUser player);

    List<TtBoxVO> getFightBoxList(Integer boxTypeId);

    List<ApiFightListDataVO> getFightList(String model, String status, Integer userId, Integer fightId);

    FightResultDataVO getFightRecord(Integer fightId, Integer round, Integer rounds);

    R fightBegin(Integer fightId, TtUser player);

    R audience(Integer fightId);

    R fightEnd(Integer fightId);

    R seatrReady(Integer fightId, TtUser player);

    R fightRoomExit(Integer fightId, TtUser player);

    List<ApiFightListDataVO> getFightList(FightOnMyOwnParam param);

    R fightDetail(FightDetailParam param);

    R earlierHistory(FightDetailParam param);

    // AjaxResult joinFightRoom(Integer userId,Integer fightId);

    List<ApiFightRankingVO> getFightRankingByDate(String date);

    R<Boolean> saveFightBoutData(FightBoutData fightBoutData);

    R<Integer> getFightBoutNum(Integer fightId);

}
