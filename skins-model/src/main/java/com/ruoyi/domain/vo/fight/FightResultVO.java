package com.ruoyi.domain.vo.fight;

import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.fight.TtFight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FightResultVO {

    private Long currentRound;

    private List<Integer> winnerIds;

    private TtFight fight;

    // 开箱结果
    private List<TtBoxRecords> fightResult;

    // 宝箱详情
    private List<FightBoxVO> fightBoxVOList;
}
