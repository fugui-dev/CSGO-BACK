package com.ruoyi.playingmethod.service.match.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.admin.mapper.match.*;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.match.*;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.playingmethod.model.match.*;
import com.ruoyi.playingmethod.service.match.ApiMatchService;
import com.ruoyi.playingmethod.service.match.message.MatchMessageUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ApiMatchServiceImpl implements ApiMatchService {

    @Resource
    private MatchTeamMapper matchTeamMapper;
    @Resource
    private MatchMapper matchMapper;
    @Resource
    private TtUserMapper ttUserMapper;
    @Resource
    private MatchUserMapper matchUserMapper;
    @Resource
    private MatchUserExamineMapper matchUserExamineMapper;
    @Resource
    private StageMapper stageMapper;
    @Resource
    private StageTeamMapper stageTeamMapper;
    @Resource
    private StageCheerMapper stageCheerMapper;
    @Resource
    private StageCheerConfigMapper stageCheerConfigMapper;
    @Resource
    private StageGroupMapper stageGroupMapper;
    @Resource
    private StageGroupFightMapper stageGroupFightMapper;
    @Resource
    private StageRecordMapper stageRecordMapper;
    @Resource
    private MatchMessageUtils matchMessageUtils;


    @Override
    public R<Integer> createMatchTeam(MatchTeamCreateCmd matchTeamCreateCmd) {

        LambdaQueryWrapper<TtUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(TtUser::getUserId, matchTeamCreateCmd.getUserId());
        userQueryWrapper.eq(TtUser::getStatus, 0); // 用户状态正常
        userQueryWrapper.eq(TtUser::getDelFlag, 0); // 用户未删除

        // 查询用户信息
        TtUser user = ttUserMapper.selectOne(userQueryWrapper);
        if (user == null) {
            return R.fail("用户不存在");
        }

        if (user.getUserType().equals("02")) {
            return R.fail("只能主播创建比赛队伍");
        }
        // 检查比赛是否存在且状态为未开始
        Match match = matchMapper.selectById(matchTeamCreateCmd.getMatchId());
        if (match == null) {
            return R.fail("比赛不存在");
        }
        if (match.getStatus() != 0) {
            return R.fail("比赛已开始或已结束，无法创建队伍");
        }

        // 检查是否已存在队伍
        LambdaQueryWrapper<MatchTeam> teamQueryWrapper = new LambdaQueryWrapper<>();
        teamQueryWrapper.eq(MatchTeam::getMatchId, matchTeamCreateCmd.getMatchId());

        Integer teamCount = matchTeamMapper.selectCount(teamQueryWrapper);
        if (teamCount >= match.getMaxTeamNum()) {
            return R.fail("比赛队伍已满，无法创建新队伍");
        }

        LambdaQueryWrapper<MatchUser> matchUserQueryWrapper = new LambdaQueryWrapper<>();
        matchUserQueryWrapper.eq(MatchUser::getMatchId, matchTeamCreateCmd.getMatchId())
                .eq(MatchUser::getUserId, matchTeamCreateCmd.getUserId());

        // 检查用户是否已在比赛队伍里
        Integer matchUserCount = matchUserMapper.selectCount(matchUserQueryWrapper);
        if (matchUserCount == 0) {
            return R.fail("用户已在比赛队伍中，无法创建队伍");
        }

        MatchTeam matchTeam = new MatchTeam();
        matchTeam.setMatchId(match.getId());
        matchTeam.setName(matchTeamCreateCmd.getName());
        matchTeam.setDescription(matchTeamCreateCmd.getDescription());
        matchTeam.setStatus(0); // 招募中
        matchTeam.setMemberCount(1); // 初始成员数量为1
        matchTeam.setMaxMemberCount(match.getTeamSize()); // 设置最大成员数量
        matchTeam.setCaptainUserId(user.getUserId()); // 队长为当前用户
        matchTeam.setTotalCheerAmount(BigDecimal.ZERO);
        matchTeam.setTotalScore(BigDecimal.ZERO);
        matchTeam.setWinCount(0);

        matchTeamMapper.insert(matchTeam);

        // 创建队伍后，添加用户到队伍中
        MatchUser matchUser = new MatchUser();
        matchUser.setMatchId(match.getId());
        matchUser.setUserId(user.getUserId());
        matchUser.setTeamId(matchTeam.getId());
        matchUser.setTotalScore(BigDecimal.ZERO);

        matchUserMapper.insert(matchUser);

        // 发送队伍创建消息
        Map<String, Object> teamInfo = new HashMap<>();
        teamInfo.put("teamId", matchTeam.getId());
        teamInfo.put("name", matchTeam.getName());
        teamInfo.put("captainUserId", matchTeam.getCaptainUserId());
        teamInfo.put("memberCount", matchTeam.getMemberCount());
        teamInfo.put("maxMemberCount", matchTeam.getMaxMemberCount());
        matchMessageUtils.sendTeamComplete(match.getId(), matchTeam.getId(), teamInfo);

        return R.ok(matchTeam.getId(), "比赛队伍创建成功");
    }

    @Override
    public R<Integer> joinMatchTeam(JoinMatchTeamCmd joinMatchTeamCmd) {

        LambdaQueryWrapper<TtUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(TtUser::getUserId, joinMatchTeamCmd.getUserId());
        userQueryWrapper.eq(TtUser::getStatus, 0); // 用户状态正常
        userQueryWrapper.eq(TtUser::getDelFlag, 0); // 用户未删除

        // 查询用户信息
        TtUser user = ttUserMapper.selectOne(userQueryWrapper);
        if (user == null) {
            return R.fail("用户不存在");
        }

        Match match = matchMapper.selectById(joinMatchTeamCmd.getMatchId());
        if (match == null) {
            return R.fail("比赛不存在");
        }

        if (match.getStatus() != 0) {
            return R.fail("比赛已开始或已结束，无法加入队伍");
        }

        // 检查报名时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(match.getSignUpStartTime()) || now.isAfter(match.getSignUpEndTime())) {
            return R.fail("当前不在报名时间内，无法加入队伍");
        }

        // 检查队伍是否存在
        MatchTeam matchTeam = matchTeamMapper.selectById(joinMatchTeamCmd.getTeamId());
        if (matchTeam == null) {
            return R.fail("队伍不存在");
        }

        // 检查队伍状态
        if (matchTeam.getStatus() != 0) {
            return R.fail("队伍已组建完成，无法加入");
        }

        // 检查用户是否已在比赛中
        LambdaQueryWrapper<MatchUser> matchUserQueryWrapper = new LambdaQueryWrapper<>();
        matchUserQueryWrapper.eq(MatchUser::getMatchId, joinMatchTeamCmd.getMatchId())
                .eq(MatchUser::getUserId, joinMatchTeamCmd.getUserId());

        Integer matchUserCount = matchUserMapper.selectCount(matchUserQueryWrapper);
        if (matchUserCount > 0) {
            return R.fail("用户已在比赛中，无法加入队伍");
        }

        //检查是否在待审核名单里
        LambdaQueryWrapper<MatchUserExamine> examineQueryWrapper = new LambdaQueryWrapper<>();
        examineQueryWrapper.eq(MatchUserExamine::getMatchId, joinMatchTeamCmd.getMatchId())
                .eq(MatchUserExamine::getUserId, joinMatchTeamCmd.getUserId())
                .eq(MatchUserExamine::getTeamId, joinMatchTeamCmd.getTeamId());

        MatchUserExamine existingExamine = matchUserExamineMapper.selectOne(examineQueryWrapper);
        if (existingExamine != null) {
            if (existingExamine.getStatus() == 0) {
                return R.fail("用户已在待审核名单中，请等待审核");
            } else if (existingExamine.getStatus() == 1) {
                return R.fail("用户已通过审核，无法重复加入");
            } else if (existingExamine.getStatus() == 2) {
                return R.fail("用户已被拒绝加入，请联系队长");
            }
        }

        existingExamine = new MatchUserExamine();
        existingExamine.setMatchId(joinMatchTeamCmd.getMatchId());
        existingExamine.setTeamId(joinMatchTeamCmd.getTeamId());
        existingExamine.setUserId(joinMatchTeamCmd.getUserId());
        existingExamine.setStatus(0); // 待审核
        existingExamine.setOpinion("");

        matchUserExamineMapper.insert(existingExamine);

        return R.ok(existingExamine.getId(), "用户已提交加入队伍申请，请等待队长审核");
    }

    @Override
    public R<Integer> examineMatchUser(MatchUserExamineCmd matchUserExamineCmd) {

        MatchUserExamine matchUserExamine = matchUserExamineMapper.selectById(matchUserExamineCmd.getId());
        if (matchUserExamine == null) {
            return R.fail("审核记录不存在");
        }

        // 检查当前用户是否为队长
        MatchTeam matchTeam = matchTeamMapper.selectById(matchUserExamine.getTeamId());
        if (matchTeam == null) {
            return R.fail("队伍不存在");
        }

        if (!matchTeam.getCaptainUserId().equals(matchUserExamineCmd.getUserId())) {
            return R.fail("只有队长才能审核用户");
        }

        // 检查比赛是否存在且状态为未开始
        Match match = matchMapper.selectById(matchUserExamine.getMatchId());
        if (match == null) {
            return R.fail("比赛不存在");
        }

        if (match.getStatus() != 0) {
            return R.fail("比赛已开始或已结束，无法审核用户");
        }

        // 检查用户是否已在比赛队伍里
        LambdaQueryWrapper<MatchUser> matchUserQueryWrapper = new LambdaQueryWrapper<>();
        matchUserQueryWrapper.eq(MatchUser::getMatchId, matchUserExamine.getMatchId())
                .eq(MatchUser::getUserId, matchUserExamine.getUserId());

        Integer matchUserCount = matchUserMapper.selectCount(matchUserQueryWrapper);
        if (matchUserCount > 0) {
            return R.fail("用户已在比赛队伍中，无法重复审核");
        }

        // 如果审核通过，要进行扣款
        if (matchUserExamineCmd.getStatus() == 1) {
            // 检查用户余额
            TtUser player = ttUserMapper.selectById(matchUserExamine.getUserId());
            if (player == null) {
                return R.fail("用户不存在");
            }

            if (player.getAccountAmount().add(player.getAccountCredits()).compareTo(match.getAmount()) < 0) {
                return R.fail("用户余额不足，无法加入比赛");
            }

            // 扣款
            deductMoney(match.getAmount(), player);

            // 添加用户到比赛队伍
            MatchUser matchUser = new MatchUser();
            matchUser.setMatchId(match.getId());
            matchUser.setUserId(player.getUserId());
            matchUser.setTeamId(matchTeam.getId());
            matchUser.setTotalScore(BigDecimal.ZERO);

            matchUserMapper.insert(matchUser);

            // 更新队伍成员数量
            matchTeam.setMemberCount(matchTeam.getMemberCount() + 1);

            // 更新队伍状态
            if (matchTeam.getMemberCount() >= matchTeam.getMaxMemberCount()) {
                matchTeam.setStatus(1); // 已组建
            }

            matchTeamMapper.updateById(matchTeam);
        }

        // 更新审核记录状态
        matchUserExamine.setStatus(matchUserExamineCmd.getStatus());
        matchUserExamine.setOpinion(matchUserExamineCmd.getOpinion());
        matchUserExamineMapper.updateById(matchUserExamine);

        // 发送审核结果消息
        Map<String, Object> examineInfo = new HashMap<>();
        examineInfo.put("userId", matchUserExamine.getUserId());
        examineInfo.put("teamId", matchUserExamine.getTeamId());
        examineInfo.put("status", matchUserExamine.getStatus());
        examineInfo.put("opinion", matchUserExamine.getOpinion());
        matchMessageUtils.sendMemberExamine(matchUserExamine.getMatchId(), matchUserExamine.getTeamId(), examineInfo);

        return R.ok(matchUserExamine.getId(), "用户审核成功");
    }

    @Override
    public R<Integer> cheerStage(StageCheerCmd stageCheerCmd) {

        LambdaQueryWrapper<TtUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(TtUser::getUserId, stageCheerCmd.getUserId());
        userQueryWrapper.eq(TtUser::getStatus, 0); // 用户状态正常
        userQueryWrapper.eq(TtUser::getDelFlag, 0); // 用户未删除

        TtUser player = ttUserMapper.selectOne(userQueryWrapper);
        if (player == null) {
            return R.fail("用户不存在或已被删除");
        }

        // 检查阶段是否已经开始
        Stage stage = stageMapper.selectById(stageCheerCmd.getStageId());
        if (stage == null) {
            return R.fail("阶段不存在");
        }

        if (stage.getStatus() != 0) { // 1表示阶段已开始
            return R.fail("阶段已开始或结束，无法进行助威");
        }

        // 检查队伍是否存在
        LambdaQueryWrapper<StageTeam> stageTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        stageTeamLambdaQueryWrapper.eq(StageTeam::getStageId,stageCheerCmd.getStageId());
        stageTeamLambdaQueryWrapper.eq(StageTeam::getTeamId,stageCheerCmd.getTeamId());

        StageTeam stageTeam = stageTeamMapper.selectOne(stageTeamLambdaQueryWrapper);
        if (stageTeam == null) {
            return R.fail("队伍不存在或不在该阶段");
        }

        // 检查用户余额
        if (player.getAccountAmount().add(player.getAccountCredits()).compareTo(stageCheerCmd.getAmount()) < 0) {
            return R.fail("用户余额不足，无法进行助威");
        }

        LambdaQueryWrapper<StageCheerConfig> stageCheerConfigQueryWrapper = new LambdaQueryWrapper<>();
        stageCheerConfigQueryWrapper.eq(StageCheerConfig::getType,stage.getType());

        StageCheerConfig stageCheerConfig = stageCheerConfigMapper.selectOne(stageCheerConfigQueryWrapper);
        if (stageCheerConfig == null) {
            return R.fail("助威配置不存在");
        }

        // 扣款
        deductMoney(stageCheerCmd.getAmount(), player);

        // 添加助威记录
        StageCheer stageCheer = new StageCheer();
        stageCheer.setStageId(stageCheerCmd.getStageId());
        stageCheer.setUserId(player.getUserId());
        stageCheer.setTeamId(stageCheerCmd.getTeamId());
        stageCheer.setAmount(stageCheerCmd.getAmount());
        stageCheer.setCreateTime(LocalDateTime.now());
        stageCheerMapper.insert(stageCheer);

        stageTeam.setInitialScore(stageTeam.getInitialScore().add(stageCheerCmd.getAmount().multiply(stageCheerConfig.getScoreProportion())));
        stageTeam.setCheerAmount(stageTeam.getCheerAmount().add(stageCheerCmd.getAmount()));
        stageTeamMapper.updateById(stageTeam);

        return R.ok(stageCheer.getId(),"助威成功");
    }

    @Override
    public R<Integer> stageGroup(StageGroupCmd stageGroupCmd) {
        // 检查阶段是否存在
        Stage stage = stageMapper.selectById(stageGroupCmd.getStageId());
        if (stage == null) {
            return R.fail("阶段不存在");
        }

        // 检查比赛信息
        Match match = matchMapper.selectById(stage.getMatchId());
        if (match == null) {
            return R.fail("比赛不存在");
        }

        // 检查比赛状态
        if (match.getStatus() != 0) {
            return R.fail("比赛已开始或已结束，无法进行分组");
        }

        // 检查时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(match.getSignUpEndTime())) {
            return R.fail("报名尚未结束，无法进行分组");
        }

        // 检查阶段状态
        if (stage.getStatus() != 0) {
            return R.fail("阶段已开始或已结束，无法进行分组");
        }

        // 检查是否已经分组
        LambdaQueryWrapper<StageGroup> existingGroupQuery = new LambdaQueryWrapper<>();
        existingGroupQuery.eq(StageGroup::getStageId, stageGroupCmd.getStageId());
        Integer existingGroupCount = stageGroupMapper.selectCount(existingGroupQuery);
        if (existingGroupCount > 0) {
            return R.fail("该阶段已经完成分组");
        }

        // 获取该阶段的所有队伍
        LambdaQueryWrapper<StageTeam> stageTeamQueryWrapper = new LambdaQueryWrapper<>();
        stageTeamQueryWrapper.eq(StageTeam::getStageId, stageGroupCmd.getStageId());
        List<StageTeam> stageTeams = stageTeamMapper.selectList(stageTeamQueryWrapper);

        // 根据阶段类型判断所需队伍数量和分组方式
        int requiredTeams;
        int teamsPerGroup;
        String[] groups;
        
        switch (stage.getType()) {
            case 1: // 小组赛
                requiredTeams = 16;
                teamsPerGroup = 4;
                groups = new String[]{"A", "B", "C", "D"};
                break;
            case 2: // 淘汰赛(8强)
                requiredTeams = 8;
                teamsPerGroup = 2;
                groups = new String[]{"A", "B", "C", "D"};
                break;
            case 3: // 淘汰赛(4强)
                requiredTeams = 4;
                teamsPerGroup = 2;
                groups = new String[]{"A", "B"};
                break;
            case 4: // 决赛
                requiredTeams = 2;
                teamsPerGroup = 2;
                groups = new String[]{"A"};
                break;
            default:
                return R.fail("未知的阶段类型");
        }

        // 检查队伍数量是否符合要求
        if (stageTeams.size() != requiredTeams) {
            return R.fail("队伍数量不符合要求，当前阶段需要" + requiredTeams + "支队伍");
        }

        // 随机打乱队伍顺序
        Collections.shuffle(stageTeams);

        // 进行分组并保存
        for (int i = 0; i < stageTeams.size(); i++) {
            StageTeam team = stageTeams.get(i);
            String groupName = groups[i / teamsPerGroup];

            // 保存分组信息
            StageGroup stageGroup = new StageGroup();
            stageGroup.setStageId(stage.getId());
            stageGroup.setGroupName(groupName);
            stageGroup.setTeamCount(teamsPerGroup);
            stageGroup.setTeamId(team.getTeamId());
            stageGroup.setCreateTime(LocalDateTime.now());
            stageGroup.setUpdateTime(LocalDateTime.now());
            stageGroupMapper.insert(stageGroup);

            // 发送分组创建消息
            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("teamId", team.getTeamId());
            groupInfo.put("groupName", groupName);
            matchMessageUtils.sendGroupCreated(stage.getMatchId(), groupName, groupInfo);
        }

        return R.ok(stage.getId(), "分组成功");
    }

    @Override
    public R<Void> startStage(Integer stageId) {
        // 检查阶段是否存在
        Stage stage = stageMapper.selectById(stageId);
        if (stage == null) {
            return R.fail("阶段不存在");
        }

        // 检查阶段状态
        if (stage.getStatus() != 0) {
            return R.fail("阶段已开始或已结束");
        }

        // 检查比赛状态
        Match match = matchMapper.selectById(stage.getMatchId());
        if (match == null) {
            return R.fail("比赛不存在");
        }
        if (match.getStatus() != 1) {
            return R.fail("比赛未开始或已结束");
        }

        // 检查是否已分组
        LambdaQueryWrapper<StageGroup> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(StageGroup::getStageId, stageId);
        Integer groupCount = stageGroupMapper.selectCount(groupQuery);
        if (groupCount == 0) {
            return R.fail("阶段未完成分组");
        }

        // 获取所有对战记录
        LambdaQueryWrapper<StageGroupFight> fightQuery = new LambdaQueryWrapper<>();
        fightQuery.eq(StageGroupFight::getStageId, stageId);
        List<StageGroupFight> fights = stageGroupFightMapper.selectList(fightQuery);
        if (fights.isEmpty()) {
            return R.fail("未找到对战记录");
        }

        // 初始化第一轮对战记录
        for (StageGroupFight fight : fights) {
            // 获取对战双方的队员信息
            LambdaQueryWrapper<MatchUser> teamAQuery = new LambdaQueryWrapper<>();
            teamAQuery.eq(MatchUser::getTeamId, fight.getTeamId());
            List<MatchUser> teamAUsers = matchUserMapper.selectList(teamAQuery);

            LambdaQueryWrapper<MatchUser> teamBQuery = new LambdaQueryWrapper<>();
            teamBQuery.eq(MatchUser::getTeamId, fight.getOpponentTeamId());
            List<MatchUser> teamBUsers = matchUserMapper.selectList(teamBQuery);

            // 创建第一轮对战记录
            for (MatchUser userA : teamAUsers) {
                StageRecord record = new StageRecord();
                record.setStageId(stageId);
                record.setTeamId(fight.getTeamId());
                record.setOpponentTeamId(fight.getOpponentTeamId());
                record.setUserId(userA.getUserId());
                record.setRound(1); // 第一轮
                record.setCreateTime(LocalDateTime.now());
                record.setUpdateTime(LocalDateTime.now());
                stageRecordMapper.insert(record);
            }

            for (MatchUser userB : teamBUsers) {
                StageRecord record = new StageRecord();
                record.setStageId(stageId);
                record.setTeamId(fight.getOpponentTeamId());
                record.setOpponentTeamId(fight.getTeamId());
                record.setUserId(userB.getUserId());
                record.setRound(1); // 第一轮
                record.setCreateTime(LocalDateTime.now());
                record.setUpdateTime(LocalDateTime.now());
                stageRecordMapper.insert(record);
            }
        }

        // 更新阶段状态为进行中
        stage.setStatus(1);
        stage.setStartTime(LocalDateTime.now());
        stage.setUpdateTime(LocalDateTime.now());
        stageMapper.updateById(stage);

        // 发送阶段开始消息
        Map<String, Object> stageInfo = new HashMap<>();
        stageInfo.put("stageId", stage.getId());
        stageInfo.put("type", stage.getType());
        stageInfo.put("startTime", stage.getStartTime());
        matchMessageUtils.sendStageStart(stage.getMatchId(), stageInfo);

        // 开始第一轮
        return startRound(stageId, 1);
    }

    @Override
    public R<Void> startRound(Integer stageId, Integer roundNum) {
        // 检查阶段是否存在
        Stage stage = stageMapper.selectById(stageId);
        if (stage == null) {
            return R.fail("阶段不存在");
        }

        // 检查阶段状态
        if (stage.getStatus() != 1) {
            return R.fail("阶段未开始或已结束");
        }

        // 检查回合数是否合法
        if (roundNum < 1) {
            return R.fail("回合数不合法");
        }

        // 检查上一轮是否已经结算（除了第一轮）
        if (roundNum > 1) {
            LambdaQueryWrapper<StageRecord> prevRoundQuery = new LambdaQueryWrapper<>();
            prevRoundQuery.eq(StageRecord::getStageId, stageId)
                    .eq(StageRecord::getRound, roundNum - 1)
                    .isNull(StageRecord::getScore); // 未结算的记录
            Integer unfinishedCount = stageRecordMapper.selectCount(prevRoundQuery);
            if (unfinishedCount > 0) {
                return R.fail("上一轮尚未结算完成");
            }
        }

        // 生成系统随机数(1-100)
        int systemNumber = new Random().nextInt(100) + 1;

        // 获取所有分组
        LambdaQueryWrapper<StageGroup> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(StageGroup::getStageId, stageId);
        List<StageGroup> groups = stageGroupMapper.selectList(groupQuery);

        // 更新当前轮次的所有记录，设置系统数字，并按分组发送消息
        for (StageGroup group : groups) {
            // 获取该分组的记录
            LambdaQueryWrapper<StageRecord> currentRoundQuery = new LambdaQueryWrapper<>();
            currentRoundQuery.eq(StageRecord::getStageId, stageId)
                    .eq(StageRecord::getRound, roundNum)
                    .eq(StageRecord::getTeamId, group.getTeamId());
            List<StageRecord> records = stageRecordMapper.selectList(currentRoundQuery);

            for (StageRecord record : records) {
                record.setResultData(systemNumber);
                record.setUpdateTime(LocalDateTime.now());
                stageRecordMapper.updateById(record);
            }

            // 发送回合开始消息
            Map<String, Object> roundInfo = new HashMap<>();
            roundInfo.put("roundNum", roundNum);
            roundInfo.put("systemNumber", systemNumber);
            matchMessageUtils.sendRoundStart(stage.getMatchId(), group.getGroupName(), roundInfo);
        }

        return R.ok();
    }

    @Override
    public R<Void> selectProbability(Integer stageId, Integer userId, Integer probability) {
        // 检查概率范围
        if (probability < 1 || probability > 100) {
            return R.fail("概率必须在1-100之间");
        }

        // 检查阶段是否存在且正在进行中
        Stage stage = stageMapper.selectById(stageId);
        if (stage == null) {
            return R.fail("阶段不存在");
        }
        if (stage.getStatus() != 1) {
            return R.fail("阶段未开始或已结束");
        }

        // 获取当前轮次的记录
        LambdaQueryWrapper<StageRecord> recordQuery = new LambdaQueryWrapper<>();
        recordQuery.eq(StageRecord::getStageId, stageId)
                .eq(StageRecord::getUserId, userId)
                .isNotNull(StageRecord::getResultData) // 确保系统数字已生成
                .isNull(StageRecord::getData) // 确保还未选择概率
                .orderByDesc(StageRecord::getRound)
                .last("LIMIT 1");

        StageRecord record = stageRecordMapper.selectOne(recordQuery);
        if (record == null) {
            return R.fail("未找到当前轮次记录或已选择概率");
        }

        // 更新玩家选择的概率
        record.setData(probability);
        record.setUpdateTime(LocalDateTime.now());
        stageRecordMapper.updateById(record);

        // 获取玩家所在分组
        StageGroup group = stageGroupMapper.selectOne(
            new LambdaQueryWrapper<StageGroup>()
                .eq(StageGroup::getStageId, stageId)
                .eq(StageGroup::getTeamId, record.getTeamId())
        );

        // 发送玩家选择消息
        Map<String, Object> selectInfo = new HashMap<>();
        selectInfo.put("userId", userId);
        selectInfo.put("probability", probability);
        selectInfo.put("roundNum", record.getRound());
        matchMessageUtils.sendPlayerSelect(stage.getMatchId(), group.getGroupName(), selectInfo);

        // 检查是否所有玩家都已选择概率
        LambdaQueryWrapper<StageRecord> unselectedQuery = new LambdaQueryWrapper<>();
        unselectedQuery.eq(StageRecord::getStageId, stageId)
                .eq(StageRecord::getRound, record.getRound())
                .isNull(StageRecord::getData); // 还未选择概率
        Integer unselectedCount = stageRecordMapper.selectCount(unselectedQuery);

        // 如果所有玩家都已选择，自动结算该轮
        if (unselectedCount == 0) {
            return settleRound(stageId, record.getRound());
        }

        return R.ok();
    }

    @Override
    public R<Void> settleRound(Integer stageId, Integer roundNum) {
        // 检查阶段是否存在
        Stage stage = stageMapper.selectById(stageId);
        if (stage == null) {
            return R.fail("阶段不存在");
        }

        // 检查阶段状态
        if (stage.getStatus() != 1) {
            return R.fail("阶段未开始或已结束");
        }

        // 检查回合数是否合法
        if (roundNum < 1) {
            return R.fail("回合数不合法");
        }

        // 获取当前轮次的所有记录
        LambdaQueryWrapper<StageRecord> roundQuery = new LambdaQueryWrapper<>();
        roundQuery.eq(StageRecord::getStageId, stageId)
                .eq(StageRecord::getRound, roundNum);
        List<StageRecord> records = stageRecordMapper.selectList(roundQuery);

        if (records.isEmpty()) {
            return R.fail("未找到当前轮次记录");
        }

        // 检查是否所有玩家都已选择概率
        for (StageRecord record : records) {
            if (record.getData() == null) {
                return R.fail("还有玩家未选择概率");
            }
        }

        // 计算每个玩家的得分
        for (StageRecord record : records) {
            // 检查是否为队长
            MatchTeam team = matchTeamMapper.selectById(record.getTeamId());
            boolean isCaptain = team != null && team.getCaptainUserId().equals(record.getUserId());
            
            // 计算得分
            R<Integer> scoreResult = calculateScore(record.getData(), record.getResultData(), isCaptain);
            if (!scoreResult.getCode().equals(200)) {
                return R.fail(scoreResult.getMsg());
            }
            
            record.setScore(BigDecimal.valueOf(scoreResult.getData()));
            record.setUpdateTime(LocalDateTime.now());
            stageRecordMapper.updateById(record);
        }

        // 更新队伍总分
        Map<Integer, BigDecimal> teamScores = new HashMap<>();
        for (StageRecord record : records) {
            teamScores.merge(record.getTeamId(), record.getScore(), BigDecimal::add);
        }

        // 更新队伍得分
        for (Map.Entry<Integer, BigDecimal> entry : teamScores.entrySet()) {
            R<Void> updateResult = updateTeamScore(entry.getKey(), stageId);
            if (!updateResult.getCode().equals(200)) {
                return updateResult;
            }
        }

        // 如果是小组赛的最后一轮，需要确定晋级队伍
        if (stage.getType() == 1) { // 小组赛
            int maxRounds = 3; // 小组赛3轮
            if (roundNum >= maxRounds) {
                R<List<Integer>> promotionResult = determinePromotedTeams(stageId);
                if (!promotionResult.getCode().equals(200)) {
                    return R.fail(promotionResult.getMsg());
                }

                // 创建下一阶段
                R<Integer> nextStageResult = startNextStage(stageId);
                if (!nextStageResult.getCode().equals(200)) {
                    return R.fail(nextStageResult.getMsg());
                }

                // 创建下一阶段的队伍
                R<Void> nextTeamsResult = createNextStageTeams(stageId, nextStageResult.getData(), promotionResult.getData());
                if (!nextTeamsResult.getCode().equals(200)) {
                    return nextTeamsResult;
                }
            } else {
                // 开始下一轮
                return startRound(stageId, roundNum + 1);
            }
        } else if (stage.getType() == 2 || stage.getType() == 3) { // 8强或4强
            // 确定获胜队伍并创建下一阶段
            R<List<Integer>> promotionResult = determinePromotedTeams(stageId);
            if (!promotionResult.getCode().equals(200) ) {
                return R.fail(promotionResult.getMsg());
            }

            // 创建下一阶段
            R<Integer> nextStageResult = startNextStage(stageId);
            if (!nextStageResult.getCode().equals(200)) {
                return R.fail(nextStageResult.getMsg());
            }

            // 创建下一阶段的队伍
            return createNextStageTeams(stageId, nextStageResult.getData(), promotionResult.getData());
        } else if (stage.getType() == 4) { // 决赛
            // 结束比赛并发放奖励
            Match match = matchMapper.selectById(stage.getMatchId());
            match.setStatus(2); // 比赛结束
            match.setUpdateTime(LocalDateTime.now());
            matchMapper.updateById(match);

            // 发放奖励
            return distributeRewards(match.getId());
        }

        return R.ok();
    }

    @Override
    public R<Integer> calculateScore(Integer probability, Integer systemNumber, Boolean isCaptain) {
        // 检查参数
        if (probability == null || systemNumber == null || isCaptain == null) {
            return R.fail("参数不能为空");
        }

        if (probability < 1 || probability > 100) {
            return R.fail("概率必须在1-100之间");
        }

        if (systemNumber < 1 || systemNumber > 100) {
            return R.fail("系统数字必须在1-100之间");
        }

        // 计算基础分数
        int baseScore = 0;
        if (probability > systemNumber) {
            // 命中，得分为(100 - 选择的概率)
            baseScore = 100 - probability;
        }

        // 如果是队长，额外增加20%的分数
        if (isCaptain) {
            baseScore = (int) (baseScore * 1.2);
        }

        return R.ok(baseScore);
    }


    // 扣款
    public R<Map<String, BigDecimal>> deductMoney(BigDecimal consumption, TtUser player) {

        // 再次检查余额
        player = ttUserMapper.selectById(player.getUserId());
        if (player.getAccountAmount().add(player.getAccountCredits()).compareTo(consumption) < 0) {
            return R.fail("余额不足");
        }

        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate.eq(TtUser::getUserId, player.getUserId());

        Map<String, BigDecimal> map;
        if (player.getAccountAmount().compareTo(consumption) >= 0) {
            userUpdate.set(TtUser::getAccountAmount, player.getAccountAmount().subtract(consumption));
            map = MapUtil.builder("Amount", consumption).map();
        } else {
            BigDecimal subtract = consumption.subtract(player.getAccountAmount());
            userUpdate
                    .set(TtUser::getAccountAmount, 0)
                    .set(TtUser::getAccountCredits, player.getAccountCredits().subtract(subtract));
            map = MapUtil.builder("Amount", player.getAccountAmount()).map();
            map.put("Credits", subtract);
        }
        map.put("total", consumption);

        ttUserMapper.update(player, userUpdate);

        return R.ok(map);
    }

    @Override
    public R<Void> updateTeamScore(Integer teamId, Integer stageId) {
        // 检查参数
        if (teamId == null || stageId == null) {
            return R.fail("参数不能为空");
        }

        // 检查队伍是否存在
        StageTeam stageTeam = stageTeamMapper.selectOne(
            new LambdaQueryWrapper<StageTeam>()
                .eq(StageTeam::getTeamId, teamId)
                .eq(StageTeam::getStageId, stageId)
        );
        if (stageTeam == null) {
            return R.fail("队伍不存在或不在该阶段");
        }

        // 获取队伍在该阶段的所有记录
        LambdaQueryWrapper<StageRecord> recordQuery = new LambdaQueryWrapper<>();
        recordQuery.eq(StageRecord::getTeamId, teamId)
                .eq(StageRecord::getStageId, stageId)
                .isNotNull(StageRecord::getScore);
        List<StageRecord> records = stageRecordMapper.selectList(recordQuery);

        // 计算总分
        BigDecimal totalScore = BigDecimal.ZERO;
        for (StageRecord record : records) {
            totalScore = totalScore.add(record.getScore());
        }

        // 更新队伍总分（包括初始分数）
        stageTeam.setTotalScore(stageTeam.getInitialScore().add(totalScore));
        stageTeam.setUpdateTime(LocalDateTime.now());
        stageTeamMapper.updateById(stageTeam);

        return R.ok();
    }

    @Override
    public R<List<Integer>> determinePromotedTeams(Integer stageId) {
        // 检查阶段是否存在
        Stage stage = stageMapper.selectById(stageId);
        if (stage == null) {
            return R.fail("阶段不存在");
        }

        // 获取所有分组
        LambdaQueryWrapper<StageGroup> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(StageGroup::getStageId, stageId);
        List<StageGroup> groups = stageGroupMapper.selectList(groupQuery);

        if (groups.isEmpty()) {
            return R.fail("未找到分组信息");
        }

        // 获取所有队伍的得分情况
        LambdaQueryWrapper<StageTeam> teamQuery = new LambdaQueryWrapper<>();
        teamQuery.eq(StageTeam::getStageId, stageId);
        List<StageTeam> teams = stageTeamMapper.selectList(teamQuery);

        // 计算每个队伍的胜场数
        Map<Integer, Integer> teamWins = new HashMap<>();
        for (StageTeam team : teams) {
            // 获取该队伍的所有对战记录
            LambdaQueryWrapper<StageGroupFight> fightQuery = new LambdaQueryWrapper<>();
            fightQuery.eq(StageGroupFight::getStageId, stageId)
                    .and(wrapper -> wrapper
                            .eq(StageGroupFight::getTeamId, team.getTeamId())
                            .or()
                            .eq(StageGroupFight::getOpponentTeamId, team.getTeamId()));
            List<StageGroupFight> fights = stageGroupFightMapper.selectList(fightQuery);

            int wins = 0;
            for (StageGroupFight fight : fights) {
                // 获取双方的总分
                BigDecimal teamScore = BigDecimal.ZERO;
                BigDecimal opponentScore = BigDecimal.ZERO;

                // 获取该场比赛的所有记录
                LambdaQueryWrapper<StageRecord> recordQuery = new LambdaQueryWrapper<>();
                recordQuery.eq(StageRecord::getStageId, stageId)
                        .and(wrapper -> wrapper
                                .eq(StageRecord::getTeamId, fight.getTeamId())
                                .or()
                                .eq(StageRecord::getTeamId, fight.getOpponentTeamId()));
                List<StageRecord> records = stageRecordMapper.selectList(recordQuery);

                // 计算双方总分
                for (StageRecord record : records) {
                    if (record.getTeamId().equals(fight.getTeamId())) {
                        teamScore = teamScore.add(record.getScore() != null ? record.getScore() : BigDecimal.ZERO);
                    } else {
                        opponentScore = opponentScore.add(record.getScore() != null ? record.getScore() : BigDecimal.ZERO);
                    }
                }

                // 判断胜负
                if (team.getTeamId().equals(fight.getTeamId())) {
                    if (teamScore.compareTo(opponentScore) > 0) {
                        wins++;
                    }
                } else {
                    if (opponentScore.compareTo(teamScore) > 0) {
                        wins++;
                    }
                }
            }
            teamWins.put(team.getTeamId(), wins);
        }

        // 按分组整理队伍
        Map<String, List<StageTeam>> groupTeams = new HashMap<>();
        for (StageTeam team : teams) {
            String groupName = groups.stream()
                    .filter(g -> g.getTeamId().equals(team.getTeamId()))
                    .findFirst()
                    .map(StageGroup::getGroupName)
                    .orElse(null);

            if (groupName != null) {
                groupTeams.computeIfAbsent(groupName, k -> new ArrayList<>()).add(team);
            }
        }

        List<Integer> promotedTeams = new ArrayList<>();

        switch (stage.getType()) {
            case 1: // 小组赛，每组前2名晋级
                for (List<StageTeam> groupTeamList : groupTeams.values()) {
                    // 按总分和胜场数排序
                    groupTeamList.sort((a, b) -> {
                        int scoreCompare = b.getTotalScore().compareTo(a.getTotalScore());
                        if (scoreCompare != 0) {
                            return scoreCompare;
                        }
                        // 总分相同时，比较胜场数
                        return teamWins.get(b.getTeamId()).compareTo(teamWins.get(a.getTeamId()));
                    });
                    // 取前两名
                    for (int i = 0; i < Math.min(2, groupTeamList.size()); i++) {
                        promotedTeams.add(groupTeamList.get(i).getTeamId());
                    }
                }
                break;

            case 2: // 8强，每组第一名晋级
            case 3: // 4强，每组第一名晋级
                for (List<StageTeam> groupTeamList : groupTeams.values()) {
                    // 按总分和胜场数排序
                    groupTeamList.sort((a, b) -> {
                        int scoreCompare = b.getTotalScore().compareTo(a.getTotalScore());
                        if (scoreCompare != 0) {
                            return scoreCompare;
                        }
                        // 总分相同时，比较胜场数
                        return teamWins.get(b.getTeamId()).compareTo(teamWins.get(a.getTeamId()));
                    });
                    // 取第一名
                    if (!groupTeamList.isEmpty()) {
                        promotedTeams.add(groupTeamList.get(0).getTeamId());
                    }
                }
                break;

            case 4: // 决赛，确定冠军
                List<StageTeam> finalists = teams.stream()
                        .sorted((a, b) -> {
                            int scoreCompare = b.getTotalScore().compareTo(a.getTotalScore());
                            if (scoreCompare != 0) {
                                return scoreCompare;
                            }
                            // 总分相同时，比较胜场数
                            return teamWins.get(b.getTeamId()).compareTo(teamWins.get(a.getTeamId()));
                        })
                        .collect(java.util.stream.Collectors.toList());
                if (!finalists.isEmpty()) {
                    promotedTeams.add(finalists.get(0).getTeamId());
                }
                break;

            default:
                return R.fail("未知的阶段类型");
        }

        if (promotedTeams.isEmpty()) {
            return R.fail("未找到晋级队伍");
        }

        // 发送晋级结果
        for (Map.Entry<String, List<StageTeam>> entry : groupTeams.entrySet()) {
            Map<String, Object> promotionInfo = new HashMap<>();
            promotionInfo.put("promotedTeams", promotedTeams);
            promotionInfo.put("standings", entry.getValue());
            matchMessageUtils.sendPromotionResult(stage.getMatchId(), entry.getKey(), promotionInfo);
        }

        return R.ok(promotedTeams);
    }

    @Override
    public R<Integer> startNextStage(Integer currentStageId) {
        // 检查当前阶段是否存在
        Stage currentStage = stageMapper.selectById(currentStageId);
        if (currentStage == null) {
            return R.fail("当前阶段不存在");
        }

        // 检查当前阶段是否已结束
        if (currentStage.getStatus() != 2) {
            return R.fail("当前阶段尚未结束");
        }

        // 确定下一个阶段类型
        int nextType;
        switch (currentStage.getType()) {
            case 1: // 小组赛 -> 8强
                nextType = 2;
                break;
            case 2: // 8强 -> 4强
                nextType = 3;
                break;
            case 3: // 4强 -> 决赛
                nextType = 4;
                break;
            case 4: // 决赛已是最后阶段
                return R.fail("当前已是最后阶段");
            default:
                return R.fail("未知的阶段类型");
        }

        // 创建新阶段
        Stage nextStage = new Stage();
        nextStage.setMatchId(currentStage.getMatchId());
        nextStage.setType(nextType);
        nextStage.setStatus(0); // 未开始
        nextStage.setCreateTime(LocalDateTime.now());
        nextStage.setUpdateTime(LocalDateTime.now());

        // 设置开始时间为当前时间加30分钟
        nextStage.setStartTime(LocalDateTime.now().plusMinutes(30));

        stageMapper.insert(nextStage);

        return R.ok(nextStage.getId());
    }

    @Override
    public R<Void> createNextStageTeams(Integer currentStageId, Integer nextStageId, List<Integer> promotedTeamIds) {
        // 参数检查
        if (currentStageId == null || nextStageId == null || promotedTeamIds == null || promotedTeamIds.isEmpty()) {
            return R.fail("参数不能为空");
        }

        // 检查当前阶段和下一阶段是否存在
        Stage currentStage = stageMapper.selectById(currentStageId);
        Stage nextStage = stageMapper.selectById(nextStageId);
        if (currentStage == null || nextStage == null) {
            return R.fail("阶段不存在");
        }

        // 检查晋级队伍数量是否正确
        int expectedTeams;
        switch (nextStage.getType()) {
            case 2: // 8强
                expectedTeams = 8;
                break;
            case 3: // 4强
                expectedTeams = 4;
                break;
            case 4: // 决赛
                expectedTeams = 2;
                break;
            default:
                return R.fail("未知的阶段类型");
        }

        if (promotedTeamIds.size() != expectedTeams) {
            return R.fail("晋级队伍数量不正确，期望" + expectedTeams + "支队伍");
        }

        // 获取当前阶段的队伍信息，用于复制初始分数
        LambdaQueryWrapper<StageTeam> currentTeamQuery = new LambdaQueryWrapper<>();
        currentTeamQuery.eq(StageTeam::getStageId, currentStageId)
                .in(StageTeam::getTeamId, promotedTeamIds);
        List<StageTeam> currentTeams = stageTeamMapper.selectList(currentTeamQuery);

        // 创建下一阶段的队伍
        for (Integer teamId : promotedTeamIds) {
            StageTeam nextTeam = new StageTeam();
            nextTeam.setStageId(nextStageId);
            nextTeam.setTeamId(teamId);
            
            // 复制当前阶段的初始分数和助威金额
            StageTeam currentTeam = currentTeams.stream()
                    .filter(t -> t.getTeamId().equals(teamId))
                    .findFirst()
                    .orElse(null);
            
            if (currentTeam != null) {
                nextTeam.setInitialScore(currentTeam.getTotalScore()); // 使用当前阶段的总分作为下一阶段的初始分
                nextTeam.setCheerAmount(currentTeam.getCheerAmount());
            } else {
                nextTeam.setInitialScore(BigDecimal.ZERO);
                nextTeam.setCheerAmount(BigDecimal.ZERO);
            }
            
            nextTeam.setTotalScore(nextTeam.getInitialScore()); // 初始总分等于初始分
            nextTeam.setCreateTime(LocalDateTime.now());
            nextTeam.setUpdateTime(LocalDateTime.now());
            
            stageTeamMapper.insert(nextTeam);
        }

        return R.ok();
    }

    @Override
    public R<Void> endStage(Integer stageId) {
        // 检查阶段是否存在
        Stage stage = stageMapper.selectById(stageId);
        if (stage == null) {
            return R.fail("阶段不存在");
        }

        // 检查阶段状态
        if (stage.getStatus() != 1) {
            return R.fail("阶段未开始或已结束");
        }

        // 检查是否所有记录都已结算
        LambdaQueryWrapper<StageRecord> unfinishedQuery = new LambdaQueryWrapper<>();
        unfinishedQuery.eq(StageRecord::getStageId, stageId)
                .isNull(StageRecord::getScore);
        Integer unfinishedCount = stageRecordMapper.selectCount(unfinishedQuery);
        if (unfinishedCount > 0) {
            return R.fail("还有未结算的记录");
        }

        // 更新阶段状态为已结束
        stage.setStatus(2);
        stage.setEndTime(LocalDateTime.now());
        stage.setUpdateTime(LocalDateTime.now());
        stageMapper.updateById(stage);

        // 发送阶段结束消息
        Map<String, Object> stageResult = new HashMap<>();
        stageResult.put("stageId", stage.getId());
        stageResult.put("type", stage.getType());
        stageResult.put("endTime", stage.getEndTime());
        matchMessageUtils.sendStageEnd(stage.getMatchId(), stageResult);

        // 如果不是决赛，自动开始下一阶段
        if (stage.getType() != 4) {
            // 确定晋级队伍
            R<List<Integer>> promotionResult = determinePromotedTeams(stageId);
            if (!promotionResult.getCode().equals(200)) {
                return R.fail(promotionResult.getMsg());
            }

            // 创建下一阶段
            R<Integer> nextStageResult = startNextStage(stageId);
            if (!nextStageResult.getCode().equals(200)) {
                return R.fail(nextStageResult.getMsg());
            }

            // 创建下一阶段的队伍
            R<Void> nextTeamsResult = createNextStageTeams(stageId, nextStageResult.getData(), promotionResult.getData());
            if (!nextTeamsResult.getCode().equals(200)) {
                return nextTeamsResult;
            }
        } else {
            // 如果是决赛，结束整个比赛并发放奖励
            Match match = matchMapper.selectById(stage.getMatchId());
            match.setStatus(2); // 比赛结束
            match.setUpdateTime(LocalDateTime.now());
            matchMapper.updateById(match);

            // 发放奖励
            return distributeRewards(match.getId());
        }

        return R.ok();
    }

    @Override
    public R<Void> distributeRewards(Integer matchId) {
        // 检查比赛是否存在
        Match match = matchMapper.selectById(matchId);
        if (match == null) {
            return R.fail("比赛不存在");
        }

        // 检查比赛状态
        if (match.getStatus() != 2) {
            return R.fail("比赛未结束");
        }

        // 获取最后一个阶段（决赛）
        LambdaQueryWrapper<Stage> stageQuery = new LambdaQueryWrapper<>();
        stageQuery.eq(Stage::getMatchId, matchId)
                .eq(Stage::getType, 4) // 决赛
                .eq(Stage::getStatus, 2); // 已结束
        Stage finalStage = stageMapper.selectOne(stageQuery);

        if (finalStage == null) {
            return R.fail("决赛阶段不存在或未结束");
        }

        // 获取决赛队伍
        LambdaQueryWrapper<StageTeam> teamQuery = new LambdaQueryWrapper<>();
        teamQuery.eq(StageTeam::getStageId, finalStage.getId())
                .orderByDesc(StageTeam::getTotalScore);
        List<StageTeam> teams = stageTeamMapper.selectList(teamQuery);

        if (teams.size() < 2) {
            return R.fail("决赛队伍数量不足");
        }

        // 获取冠军队伍的成员
        LambdaQueryWrapper<MatchUser> championQuery = new LambdaQueryWrapper<>();
        championQuery.eq(MatchUser::getTeamId, teams.get(0).getTeamId())
                .eq(MatchUser::getMatchId, matchId);
        List<MatchUser> championTeamMembers = matchUserMapper.selectList(championQuery);

        // 获取亚军队伍的成员
        LambdaQueryWrapper<MatchUser> runnerUpQuery = new LambdaQueryWrapper<>();
        runnerUpQuery.eq(MatchUser::getTeamId, teams.get(1).getTeamId())
                .eq(MatchUser::getMatchId, matchId);
        List<MatchUser> runnerUpTeamMembers = matchUserMapper.selectList(runnerUpQuery);

        // 计算奖金池
        BigDecimal totalPrize = match.getAmount().multiply(BigDecimal.valueOf(match.getMaxTeamNum() * match.getTeamSize()));

        // 分配奖励
        // 冠军队伍获得60%的奖金池
        BigDecimal championPrize = totalPrize.multiply(BigDecimal.valueOf(0.6));
        BigDecimal championMemberPrize = championPrize.divide(BigDecimal.valueOf(championTeamMembers.size()), 2, BigDecimal.ROUND_DOWN);

        // 亚军队伍获得40%的奖金池
        BigDecimal runnerUpPrize = totalPrize.multiply(BigDecimal.valueOf(0.4));
        BigDecimal runnerUpMemberPrize = runnerUpPrize.divide(BigDecimal.valueOf(runnerUpTeamMembers.size()), 2, BigDecimal.ROUND_DOWN);

        // 发放奖励给冠军队员
        for (MatchUser member : championTeamMembers) {
            TtUser user = ttUserMapper.selectById(member.getUserId());
            if (user != null) {
                user.setAccountAmount(user.getAccountAmount().add(championMemberPrize));
                ttUserMapper.updateById(user);

                // TODO: 创建冠军宝箱
                // 这里需要调用创建宝箱的方法
            }
        }

        // 发放奖励给亚军队员
        for (MatchUser member : runnerUpTeamMembers) {
            TtUser user = ttUserMapper.selectById(member.getUserId());
            if (user != null) {
                user.setAccountAmount(user.getAccountAmount().add(runnerUpMemberPrize));
                ttUserMapper.updateById(user);
            }
        }

        // 发送比赛结束消息
        Map<String, Object> matchResult = new HashMap<>();
        matchResult.put("championTeam", teams.get(0));
        matchResult.put("runnerUpTeam", teams.get(1));
        matchResult.put("championPrize", championPrize);
        matchResult.put("runnerUpPrize", runnerUpPrize);
        matchMessageUtils.sendMatchEnd(matchId, matchResult);

        return R.ok();
    }

    @Override
    public R<Void> openChampionBox(Integer userId, Integer boxId) {
        // TODO: 实现开启冠军宝箱的逻辑
        // 这个功能需要更多的业务需求细节，比如：
        // 1. 宝箱中可能包含什么物品
        // 2. 物品的稀有度和概率
        // 3. 是否需要消耗钥匙
        // 4. 是否有开启次数限制
        // 等等
        return R.fail("功能待实现");
    }
}
