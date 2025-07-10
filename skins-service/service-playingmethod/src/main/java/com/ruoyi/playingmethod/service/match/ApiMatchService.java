package com.ruoyi.playingmethod.service.match;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.playingmethod.model.match.JoinMatchTeamCmd;
import com.ruoyi.playingmethod.model.match.MatchTeamCreateCmd;
import com.ruoyi.playingmethod.model.match.MatchUserExamineCmd;

public interface ApiMatchService {

    /**
     * 创建比赛队伍
     * @param matchTeamCreateCmd
     * @return
     */
    R<Integer> createMatchTeam(MatchTeamCreateCmd matchTeamCreateCmd);

    /**
     * 加入比赛队伍
     * @param joinMatchTeamCmd
     * @return
     */
    R<Integer> joinMatchTeam(JoinMatchTeamCmd joinMatchTeamCmd);


    /**
     * 审核比赛用户
     * @param matchUserExamineCmd
     * @return
     */
    R<Integer> examineMatchUser(MatchUserExamineCmd matchUserExamineCmd);
}
