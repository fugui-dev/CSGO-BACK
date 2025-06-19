package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtFightResult;
import com.ruoyi.domain.vo.FightResultDataVO;
import com.ruoyi.domain.vo.fight.FightResultVO;

public interface TtFightResultService extends IService<TtFightResult> {
    // FightResultDataVO getFightResult(Integer fightId);
    FightResultVO getFightResult(Integer fightId);
}
