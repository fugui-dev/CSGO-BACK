package com.ruoyi.admin.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.TtFightMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtFightService;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.other.TtFightResult;
import com.ruoyi.admin.mapper.TtFightResultMapper;
import com.ruoyi.admin.service.TtFightResultService;
import com.ruoyi.domain.vo.FightResultDataVO;
import com.ruoyi.domain.vo.fight.FightBoxVO;
import com.ruoyi.domain.vo.fight.FightResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TtFightResultServiceImpl extends ServiceImpl<TtFightResultMapper, TtFightResult> implements TtFightResultService {

    private final TtFightMapper fightMapper;

    public TtFightResultServiceImpl(TtFightMapper fightMapper) {
        this.fightMapper = fightMapper;
    }

    @Autowired
    private TtFightService ttFightService;

    @Autowired
    private TtBoxRecordsService boxRecordsService;

    // @Override
    // public FightResultDataVO getFightResult(Integer fightId) {
    //
    //     // new LambdaUpdateChainWrapper<>(fightMapper).eq(TtFight::getId, fightId).set(TtFight::getStatus, "2")
    //     //         .set(TtFight::getUpdateTime, new Date()).update();
    //     // TtFightResult fightResult = new LambdaQueryChainWrapper<>(baseMapper).eq(TtFightResult::getFightId, fightId).one();
    //
    //     LambdaUpdateWrapper<TtFight> ttFightUpdate = new LambdaUpdateWrapper<>();
    //     ttFightUpdate
    //             .eq(TtFight::getId, fightId).set(TtFight::getStatus, "2")
    //             .set(TtFight::getUpdateTime, new Date());
    //     ttFightService.update(ttFightUpdate);
    //
    //     LambdaQueryWrapper<TtFightResult> ttFightResultQuery = new LambdaQueryWrapper<>();
    //     ttFightResultQuery.eq(TtFightResult::getFightId, fightId);
    //     TtFightResult fightResult = getOne(ttFightResultQuery);
    //
    //     log.info("fightId="+fightId);
    //     String fightResultStr = fightResult.getFightResult();
    //     return JSONObject.parseObject(fightResultStr, FightResultDataVO.class);
    // }

    @Override
    public FightResultVO getFightResult(Integer fightId) {
        LambdaQueryWrapper<TtFight> fightQuery = new LambdaQueryWrapper<>();
        fightQuery
                .eq(TtFight::getId, fightId);
        TtFight fight = ttFightService.getOne(fightQuery);

        LambdaQueryWrapper<TtBoxRecords> boxRecordsQuery = new LambdaQueryWrapper<>();
        boxRecordsQuery
                .eq(TtBoxRecords::getFightId, fightId);
        List<TtBoxRecords> allBoxRecords = boxRecordsService.list(boxRecordsQuery);

        Map<String, FightBoxVO> boxData = fight.getBoxDataMap();
        ArrayList<FightBoxVO> fightBoxVOList = new ArrayList<>();
        boxData.keySet().forEach(boxId -> {
            fightBoxVOList.add(boxData.get(boxId));
        });

        FightResultVO resultVO = FightResultVO.builder()
                .fight(fight)
                .winnerIds(fight.getWinnerList())
                .fightResult(allBoxRecords)
                .fightBoxVOList(fightBoxVOList)
                .build();
        return resultVO;
    }
}
