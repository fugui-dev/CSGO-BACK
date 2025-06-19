package com.ruoyi.domain.vo;

import com.ruoyi.domain.entity.TtOrnament;
import lombok.Data;

import java.util.List;

@Data
public class FightResultDataVO {

    private Integer fightId;
    private Integer round;
    private List<PlayerGainsOrnamentsDataVO> playerGainsOrnamentsData;
    private List<PlayerGainsOrnamentsDataVO> winnerGainsOrnamentsData;
    private List<TtOrnament> TtOrnament;

}
