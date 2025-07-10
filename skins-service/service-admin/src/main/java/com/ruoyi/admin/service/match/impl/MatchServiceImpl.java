package com.ruoyi.admin.service.match.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.admin.mapper.match.MatchMapper;
import com.ruoyi.admin.mapper.match.StageMapper;
import com.ruoyi.admin.service.match.MatchService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.match.MatchCreateCmd;
import com.ruoyi.domain.entity.match.Match;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MatchServiceImpl implements MatchService {

    @Resource
    private MatchMapper matchMapper;

    @Resource
    private StageMapper stageMapper;

    @Override
    public R<Integer> createMatch(MatchCreateCmd matchCreateCmd) {


        String dayTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 1. 创建比赛
        LambdaQueryWrapper<Match> matchQueryWrapper = new LambdaQueryWrapper<>();
        matchQueryWrapper.eq(Match::getDayTime, dayTime);

        Match match = matchMapper.selectOne(matchQueryWrapper);
        if (match != null) {
            return R.fail("今天的比赛已经创建过了，请勿重复创建");
        }

        if (matchCreateCmd.getSignUpStartTime().isAfter(matchCreateCmd.getSignUpEndTime())) {
            return R.fail("报名开始时间不能晚于报名结束时间");
        }


        if (matchCreateCmd.getOpenTime().isAfter(matchCreateCmd.getSignUpEndTime())) {
            return R.fail("开放时间不能晚于报名结束时间");
        }

        if (matchCreateCmd.getStartTime().isAfter(matchCreateCmd.getSignUpEndTime())) {
            return R.fail("开始时间不能晚于报名结束时间");
        }


        match = new Match();
        match.setDayTime(dayTime);
        match.setName(matchCreateCmd.getName());
        match.setDescription(matchCreateCmd.getDescription());
        match.setStatus(0); // 初始状态为未开始
        match.setMaxTeamNum(16);
        match.setTeamSize(10);
        match.setOpenTime(matchCreateCmd.getOpenTime());
        match.setStartTime(matchCreateCmd.getStartTime());
        match.setSignUpEndTime(matchCreateCmd.getSignUpEndTime());
        match.setSignUpStartTime(matchCreateCmd.getSignUpStartTime());
        match.setCreateTime(LocalDateTime.now());

        matchMapper.insert(match);

        Integer matchId = match.getId();

        return R.ok(matchId, "比赛创建成功");

    }
}
