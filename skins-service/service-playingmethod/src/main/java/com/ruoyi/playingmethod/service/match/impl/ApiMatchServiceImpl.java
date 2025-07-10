package com.ruoyi.playingmethod.service.match.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.admin.mapper.match.MatchMapper;
import com.ruoyi.admin.mapper.match.MatchTeamMapper;
import com.ruoyi.admin.mapper.match.MatchUserExamineMapper;
import com.ruoyi.admin.mapper.match.MatchUserMapper;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.match.Match;
import com.ruoyi.domain.entity.match.MatchTeam;
import com.ruoyi.domain.entity.match.MatchUser;
import com.ruoyi.domain.entity.match.MatchUserExamine;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.playingmethod.model.match.JoinMatchTeamCmd;
import com.ruoyi.playingmethod.model.match.MatchTeamCreateCmd;
import com.ruoyi.playingmethod.model.match.MatchUserExamineCmd;
import com.ruoyi.playingmethod.service.match.ApiMatchService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

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

        return R.ok(matchUserExamine.getId(), "用户审核成功");
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
}
