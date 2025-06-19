package com.ruoyi.domain.vo.fight;

import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.fight.TtFight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class AudienceVO {

    private Integer currentRound;

    private List<Integer> winnerIds;

    private TtFight fight;

    // 开箱结果
    private List<TtBoxRecords> fightResult;

    // 宝箱详情
    private List<FightBoxVO> fightBoxVOList;

}
