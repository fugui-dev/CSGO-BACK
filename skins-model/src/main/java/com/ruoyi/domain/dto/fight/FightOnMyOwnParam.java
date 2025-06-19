package com.ruoyi.domain.dto.fight;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FightOnMyOwnParam {

    private Integer fightId;

    private Integer playerId;

    // 模式
    private String model;

    // 状态列表
    private List<Integer> statusList;

    private Integer page;

    private Integer size;
}
