package com.ruoyi.playingmethod.service.match;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.playingmethod.model.match.*;

import java.util.List;

public interface ApiMatchService {

    /**
     * 创建比赛队伍
     */
    R<Integer> createMatchTeam(MatchTeamCreateCmd matchTeamCreateCmd);

    /**
     * 加入比赛队伍
     */
    R<Integer> joinMatchTeam(JoinMatchTeamCmd joinMatchTeamCmd);

    /**
     * 审核比赛用户
     */
    R<Integer> examineMatchUser(MatchUserExamineCmd matchUserExamineCmd);

    /**
     * 助威比赛阶段
     */
    R<Integer> cheerStage(StageCheerCmd stageCheerCmd);

    /**
     * 阶段队伍分组
     */
    R<Integer> stageGroup(StageGroupCmd stageGroupCmd);

    /**
     * 开始比赛阶段
     * @param stageId 阶段ID
     */
    R<Void> startStage(Integer stageId);

    /**
     * 结束比赛阶段
     * @param stageId 阶段ID
     */
    R<Void> endStage(Integer stageId);

    /**
     * 开始下一个阶段
     * @param currentStageId 当前阶段ID
     */
    R<Integer> startNextStage(Integer currentStageId);

    /**
     * 开始新回合
     * @param stageId 阶段ID
     * @param roundNum 回合数
     */
    R<Void> startRound(Integer stageId, Integer roundNum);

    /**
     * 玩家选择概率
     * @param stageId 阶段ID
     * @param userId 用户ID
     * @param probability 选择的概率(1-100)
     */
    R<Void> selectProbability(Integer stageId, Integer userId, Integer probability);

    /**
     * 回合结算
     * @param stageId 阶段ID
     * @param roundNum 回合数
     */
    R<Void> settleRound(Integer stageId, Integer roundNum);

    /**
     * 计算玩家得分
     * @param probability 选择的概率
     * @param systemNumber 系统数字
     * @param isCaptain 是否队长
     */
    R<Integer> calculateScore(Integer probability, Integer systemNumber, Boolean isCaptain);

    /**
     * 更新队伍积分
     * @param teamId 队伍ID
     * @param stageId 阶段ID
     */
    R<Void> updateTeamScore(Integer teamId, Integer stageId);

    /**
     * 确定晋级队伍
     * @param stageId 阶段ID
     * @return 晋级队伍ID列表
     */
    R<List<Integer>> determinePromotedTeams(Integer stageId);

    /**
     * 创建新阶段队伍
     * @param currentStageId 当前阶段ID
     * @param nextStageId 下一阶段ID
     * @param promotedTeamIds 晋级队伍ID列表
     */
    R<Void> createNextStageTeams(Integer currentStageId, Integer nextStageId, List<Integer> promotedTeamIds);

    /**
     * 发放比赛奖励
     * @param matchId 比赛ID
     */
    R<Void> distributeRewards(Integer matchId);

    /**
     * 开启冠军宝箱
     * @param userId 用户ID
     * @param boxId 宝箱ID
     */
    R<Void> openChampionBox(Integer userId, Integer boxId);
}
