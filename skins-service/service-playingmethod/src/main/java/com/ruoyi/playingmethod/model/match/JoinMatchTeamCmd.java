package com.ruoyi.playingmethod.model.match;

import lombok.Data;

@Data
public class JoinMatchTeamCmd {

    /**
     * 比赛ID
     */
    private Integer matchId;

    /**
     * 队伍ID
     */
    private Integer teamId;

    /**
     * 用户ID
     */
    private Integer userId;
}
