package com.ruoyi.playingmethod.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.roll.GetRollOpenPrizeParam;
import com.ruoyi.domain.dto.roll.GetRollPlayersParam;
import com.ruoyi.domain.dto.roll.GetRollPrizePool;
import com.ruoyi.domain.entity.roll.TtRoll;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.vo.RollDetailsDataVO;
import com.ruoyi.domain.vo.RollListDataVO;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import com.ruoyi.domain.vo.roll.RollJackpotOrnamentsVO;
import com.ruoyi.domain.vo.roll.RollUserVO;
import com.ruoyi.playingmethod.controller.ApiRollController.GetRollListParam;
import com.ruoyi.playingmethod.controller.ApiRollController.JoinRollParam;

import java.util.List;

public interface ApiRollService extends IService<TtRoll> {

    R joinRoll(JoinRollParam param, TtUser player);

    R endROLL(Integer rollId);

    List<RollListDataVO> getRollList(GetRollListParam param);

    R<RollDetailsDataVO> getRollDetails(Integer rollId);

    R<List<RollUserVO>> getRollPlayers(GetRollPlayersParam param);

    R<List<RollJackpotOrnamentsVO>> getRollPrizePool(GetRollPrizePool param);

    R<List<TtBoxRecordsVO>> getRollOpenPrize(GetRollOpenPrizeParam param);
}
