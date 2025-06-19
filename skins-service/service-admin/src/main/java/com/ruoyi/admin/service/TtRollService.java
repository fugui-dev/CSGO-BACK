package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.roll.GetRollPrizePool;
import com.ruoyi.domain.dto.roll.InviteRollUser;
import com.ruoyi.domain.entity.roll.TtRoll;
import com.ruoyi.domain.entity.roll.TtRollBody;
import com.ruoyi.domain.vo.TtRollPrizeDataVO;
import com.ruoyi.domain.vo.roll.RollJackpotOrnamentsByPageVO;
import com.ruoyi.domain.vo.roll.RollJackpotOrnamentsVO;

import java.util.List;

public interface TtRollService extends IService<TtRoll> {

    List<TtRoll> queryList(TtRollBody ttRollBody);

    AjaxResult createRoll(TtRoll ttRoll);

    AjaxResult updateRollById(TtRoll ttRoll);

    List<TtRollPrizeDataVO> getRollPrizeList(Integer rollId);

    AjaxResult namedWinner(TtRollPrizeDataVO param);

    AjaxResult getRollUsers(Integer rollId);

    R cancelNamedWinner(List<Integer> rollUserPrizeIds);

    R<RollJackpotOrnamentsByPageVO> getRollPrizePool(GetRollPrizePool param);

    R inviteRollUser(InviteRollUser inviteRollUser);
}
