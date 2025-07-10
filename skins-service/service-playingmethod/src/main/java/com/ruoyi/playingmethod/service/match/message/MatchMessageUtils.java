package com.ruoyi.playingmethod.service.match.message;

import org.springframework.stereotype.Component;

@Component
public class MatchMessageUtils {

    // 比赛全局消息类型
    public static final String MATCH_START = "MATCH_START";
    public static final String MATCH_END = "MATCH_END";
    public static final String STAGE_START = "STAGE_START";
    public static final String STAGE_END = "STAGE_END";
    
    // 分组消息类型
    public static final String GROUP_CREATED = "GROUP_CREATED";
    public static final String BATTLE_START = "BATTLE_START";
    public static final String ROUND_START = "ROUND_START";
    public static final String ROUND_END = "ROUND_END";
    public static final String PLAYER_SELECT = "PLAYER_SELECT";
    public static final String ROUND_RESULT = "ROUND_RESULT";
    public static final String GROUP_STANDINGS = "GROUP_STANDINGS";
    public static final String PROMOTION_RESULT = "PROMOTION_RESULT";
    
    // 队伍消息类型
    public static final String TEAM_COMPLETE = "TEAM_COMPLETE";
    public static final String MEMBER_JOIN = "MEMBER_JOIN";
    public static final String MEMBER_EXAMINE = "MEMBER_EXAMINE";

    // 发送比赛开始消息
    public void sendMatchStart(Integer matchId, Object matchInfo) {
        MatchWebSocketUsers.broadcastToMatch(matchId, MATCH_START, matchInfo);
    }

    // 发送比赛结束消息
    public void sendMatchEnd(Integer matchId, Object matchResult) {
        MatchWebSocketUsers.broadcastToMatch(matchId, MATCH_END, matchResult);
    }

    // 发送阶段开始消息
    public void sendStageStart(Integer matchId, Object stageInfo) {
        MatchWebSocketUsers.broadcastToMatch(matchId, STAGE_START, stageInfo);
    }

    // 发送阶段结束消息
    public void sendStageEnd(Integer matchId, Object stageResult) {
        MatchWebSocketUsers.broadcastToMatch(matchId, STAGE_END, stageResult);
    }

    // 发送分组创建消息
    public void sendGroupCreated(Integer matchId, String groupName, Object groupInfo) {
        MatchWebSocketUsers.broadcastToGroup(matchId, groupName, GROUP_CREATED, groupInfo);
    }

    // 发送对战开始消息
    public void sendBattleStart(Integer matchId, String groupName, Object battleInfo) {
        MatchWebSocketUsers.broadcastToGroup(matchId, groupName, BATTLE_START, battleInfo);
    }

    // 发送回合开始消息
    public void sendRoundStart(Integer matchId, String groupName, Object roundInfo) {
        MatchWebSocketUsers.broadcastToGroup(matchId, groupName, ROUND_START, roundInfo);
    }

    // 发送回合结束消息
    public void sendRoundEnd(Integer matchId, String groupName, Object roundResult) {
        MatchWebSocketUsers.broadcastToGroup(matchId, groupName, ROUND_END, roundResult);
    }

    // 发送玩家选择消息
    public void sendPlayerSelect(Integer matchId, String groupName, Object selectInfo) {
        MatchWebSocketUsers.broadcastToGroup(matchId, groupName, PLAYER_SELECT, selectInfo);
    }

    // 发送回合结果消息
    public void sendRoundResult(Integer matchId, String groupName, Object result) {
        MatchWebSocketUsers.broadcastToGroup(matchId, groupName, ROUND_RESULT, result);
    }

    // 发送分组排名消息
    public void sendGroupStandings(Integer matchId, String groupName, Object standings) {
        MatchWebSocketUsers.broadcastToGroup(matchId, groupName, GROUP_STANDINGS, standings);
    }

    // 发送晋级结果消息
    public void sendPromotionResult(Integer matchId, String groupName, Object promotionInfo) {
        MatchWebSocketUsers.broadcastToGroup(matchId, groupName, PROMOTION_RESULT, promotionInfo);
    }

    // 发送队伍组建完成消息
    public void sendTeamComplete(Integer matchId, Integer teamId, Object teamInfo) {
        MatchWebSocketUsers.broadcastToMatch(matchId, TEAM_COMPLETE, teamInfo);
    }

    // 发送成员加入消息
    public void sendMemberJoin(Integer matchId, Integer teamId, Object memberInfo) {
        MatchWebSocketUsers.broadcastToMatch(matchId, MEMBER_JOIN, memberInfo);
    }

    // 发送成员审核消息
    public void sendMemberExamine(Integer matchId, Integer teamId, Object examineInfo) {
        MatchWebSocketUsers.broadcastToMatch(matchId, MEMBER_EXAMINE, examineInfo);
    }
} 