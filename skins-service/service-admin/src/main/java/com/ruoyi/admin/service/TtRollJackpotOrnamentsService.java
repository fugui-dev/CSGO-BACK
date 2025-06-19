package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.dto.rollJackpotOrnament.RollJOEdit;
import com.ruoyi.domain.entity.roll.TtRollJackpotOrnaments;
import com.ruoyi.domain.entity.roll.TtRollJackpotOrnamentsBody;
import com.ruoyi.domain.vo.TtRollJackpotOrnamentsDataVO;

import java.util.List;

public interface TtRollJackpotOrnamentsService extends IService<TtRollJackpotOrnaments> {

    List<TtRollJackpotOrnamentsDataVO> queryList(TtRollJackpotOrnamentsBody rollJackpotOrnamentsBody);

    String insertRollJackpotOrnaments(TtRollJackpotOrnaments ttRollJackpotOrnaments);

    String updateRollJackpotOrnamentsById(TtRollJackpotOrnaments rollJOEdit);

    String batchAdd(Integer rollJackpotId, List<Long> OrnamentsIds);
}
