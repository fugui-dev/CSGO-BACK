package com.ruoyi.playingmethod.model.match;

import lombok.Data;

@Data
public class MatchTeamCreateCmd {

    /**
     * 比赛id
     */
    private Integer matchId;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 用户id
     */
    private Integer userId;
}
