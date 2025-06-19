package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.vo.FightBoxDataVO;
import com.ruoyi.domain.other.TtFightBody;

import java.util.List;

public interface TtFightService extends IService<TtFight> {

    List<TtFight> selectFightList(TtFightBody ttFightBody);

    List<FightBoxDataVO> selectFightBoxList(Integer fightId);

    int endFight(String fightId);
}
