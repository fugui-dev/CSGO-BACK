package com.ruoyi.admin.task;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.admin.mapper.TtRechargeRecordMapper;
import com.ruoyi.admin.mapper.TtRollJackpotOrnamentsMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtTimeRollService;
import com.ruoyi.admin.service.TtTimeRollUserService;
import com.ruoyi.admin.util.RandomUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.roll.TtRollJackpotOrnaments;
import com.ruoyi.domain.entity.roll.TtTimeRoll;
import com.ruoyi.domain.entity.roll.TtTimeRollUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.ruoyi.domain.common.constant.TtboxRecordSource.ROLL;

@Slf4j
@Component("TimeRollTask")
public class TimeRollTask {

    private final TtOrnamentMapper ornamentsMapper;
    private final TtBoxRecordsService boxRecordsService;
    private final TtTimeRollService timeRollService;
    private final TtTimeRollUserService timeRollUserService;
    private final TtRechargeRecordMapper rechargeRecordMapper;
    private final TtRollJackpotOrnamentsMapper rollJackpotOrnamentsMapper;

    public TimeRollTask(TtOrnamentMapper ornamentsMapper,
                        TtBoxRecordsService boxRecordsService,
                        TtTimeRollService timeRollService,
                        TtTimeRollUserService timeRollUserService,
                        TtRechargeRecordMapper rechargeRecordMapper,
                        TtRollJackpotOrnamentsMapper rollJackpotOrnamentsMapper) {
        this.ornamentsMapper = ornamentsMapper;
        this.boxRecordsService = boxRecordsService;
        this.timeRollService = timeRollService;
        this.timeRollUserService = timeRollUserService;
        this.rechargeRecordMapper = rechargeRecordMapper;
        this.rollJackpotOrnamentsMapper = rollJackpotOrnamentsMapper;
    }

    public void timeRollSettle(Integer id){
        TtTimeRoll timeRoll = timeRollService.getById(id);
        String rechargeCondition = timeRoll.getRechargeCondition();
        BigDecimal minRecharge = timeRoll.getMinRecharge();
        List<Integer> userIds = null;
        if ("0".equals(rechargeCondition)) {
            userIds = rechargeRecordMapper.getLastHourRechargeUserIds(minRecharge);
        } else if ("1".equals(rechargeCondition)) {
            userIds = rechargeRecordMapper.getLastDayRechargeUserIds(minRecharge);
        } else if ("2".equals(rechargeCondition)) {
            userIds = rechargeRecordMapper.getLastWeekRechargeUserIds(minRecharge);
        } else if ("3".equals(rechargeCondition)) {
            userIds = rechargeRecordMapper.getLastMonthRechargeUserIds(minRecharge);
        }
        if (StringUtils.isNull(userIds) || userIds.isEmpty()) return;
        List<TtTimeRollUser> allocatedTimeRollUsers = new LambdaQueryChainWrapper<>(timeRollUserService.getBaseMapper())
                .eq(TtTimeRollUser::getTimeRollId, id).eq(TtTimeRollUser::getStatus, "2").eq(TtTimeRollUser::getEndStatus, "0").list();
        List<Integer> assignedUserIds = allocatedTimeRollUsers.stream().map(TtTimeRollUser::getUserId)
                .collect(Collectors.toList());
        userIds.removeAll(assignedUserIds);
        if (userIds.isEmpty()) return;
        List<TtTimeRollUser> timeRollUserList = new ArrayList<>();
        for (Integer userId : userIds) {
            TtTimeRollUser timeRollUser = TtTimeRollUser.builder().build();
            timeRollUser.setTimeRollId(id);
            timeRollUser.setUserId(userId);
            timeRollUser.setJoinTime(DateUtils.getNowDate());
            timeRollUserList.add(timeRollUser);
        }
        if (timeRollUserService.saveBatch(timeRollUserList, 1)) {
            List<Long> surplusOrnamentsList = getSurplusOrnamentsList(id, timeRoll.getJackpotId(), allocatedTimeRollUsers);
            if (!surplusOrnamentsList.isEmpty()) {
                List<TtTimeRollUser> timeRollUsers = new LambdaQueryChainWrapper<>(timeRollUserService.getBaseMapper())
                        .eq(TtTimeRollUser::getTimeRollId, id).eq(TtTimeRollUser::getStatus, "0").eq(TtTimeRollUser::getEndStatus, "0").list();
                Collections.shuffle(timeRollUsers);
                int index = 0;
                Map<Long, TtRollJackpotOrnaments> map = new HashMap<>();
                if (surplusOrnamentsList.size() > timeRollUsers.size()) {
                    for (TtTimeRollUser ttTimeRollUser : timeRollUsers) {
                        Long ornamentsId = surplusOrnamentsList.get(index);
                        TtRollJackpotOrnaments rollJackpotOrnaments = getRollJackpotOrnaments(ornamentsId, map, timeRoll.getJackpotId());
                        TtBoxRecords boxRecords = addBoxRecordsData(id, ornamentsId, ttTimeRollUser.getUserId(), rollJackpotOrnaments);
                        boxRecordsService.save(boxRecords);
                        ttTimeRollUser.setJackpotOrnamentsId(rollJackpotOrnaments.getId());
                        ttTimeRollUser.setOrnamentsId(ornamentsId);
                        ttTimeRollUser.setBoxRecordId(boxRecords.getId());
                        ttTimeRollUser.setStatus("1");
                        ttTimeRollUser.setDesignatedBy("系统随机分配");
                        ttTimeRollUser.setUpdateTime(DateUtils.getNowDate());
                        ttTimeRollUser.setEndStatus("1");
                        index++;
                    }
                } else {
                    for (Long ornamentsId : surplusOrnamentsList) {
                        TtRollJackpotOrnaments rollJackpotOrnaments = getRollJackpotOrnaments(ornamentsId, map, timeRoll.getJackpotId());
                        TtBoxRecords boxRecords = addBoxRecordsData(id, ornamentsId, timeRollUsers.get(index).getUserId(), rollJackpotOrnaments);
                        boxRecordsService.save(boxRecords);
                        timeRollUsers.get(index).setJackpotOrnamentsId(rollJackpotOrnaments.getId());
                        timeRollUsers.get(index).setOrnamentsId(ornamentsId);
                        timeRollUsers.get(index).setBoxRecordId(boxRecords.getId());
                        timeRollUsers.get(index).setStatus("1");
                        timeRollUsers.get(index).setDesignatedBy("系统随机分配");
                        timeRollUsers.get(index).setUpdateTime(DateUtils.getNowDate());
                        timeRollUsers.get(index).setEndStatus("1");
                        index++;
                    }
                }
                timeRollUserService.updateBatchById(timeRollUsers, 1);
            }
            List<TtBoxRecords> boxRecordsList = new LambdaQueryChainWrapper<>(boxRecordsService.getBaseMapper())
                    .eq(TtBoxRecords::getRollId, id)
                    .eq(TtBoxRecords::getStatus, "7")
                    .eq(TtBoxRecords::getSource, "7")
                    .list();
            boxRecordsList = boxRecordsList.stream().peek(ttBoxRecords -> {
                ttBoxRecords.setStatus(TtboxRecordStatus.IN_PACKSACK_ON.getCode());
                ttBoxRecords.setUpdateTime(DateUtils.getNowDate());
            }).collect(Collectors.toList());
            boxRecordsService.updateBatchById(boxRecordsList, 1);
        }
    }

    private TtBoxRecords addBoxRecordsData(Integer timeRollId, Long ornamentsId, Integer userId, TtRollJackpotOrnaments rollJackpotOrnaments) {
        TtOrnament ttOrnament = new LambdaQueryChainWrapper<>(ornamentsMapper).eq(TtOrnament::getId, ornamentsId).one();
        TtBoxRecords boxRecords = TtBoxRecords.builder().build();
        boxRecords.setUserId(userId);
        boxRecords.setOrnamentId(ornamentsId);
        boxRecords.setOrnamentsPrice(ttOrnament.getUsePrice());
        boxRecords.setOrnamentsLevelId(rollJackpotOrnaments.getOrnamentLevelId());
        boxRecords.setStatus(TtboxRecordStatus.IN_PACKSACK_ON.getCode());
        boxRecords.setCreateTime(DateUtils.getNowDate());
        boxRecords.setSource(ROLL.getCode());
        boxRecords.setRollId(timeRollId);
        boxRecords.setHolderUserId(userId);
        return boxRecords;
    }

    private TtRollJackpotOrnaments getRollJackpotOrnaments(Long ornamentsId, Map<Long, TtRollJackpotOrnaments> map, Integer jackpotId) {
        TtRollJackpotOrnaments ttRollJackpotOrnaments = map.get(ornamentsId);
        if (ttRollJackpotOrnaments == null) {
            ttRollJackpotOrnaments = new LambdaQueryChainWrapper<>(rollJackpotOrnamentsMapper).eq(TtRollJackpotOrnaments::getJackpotId, jackpotId)
                    .eq(TtRollJackpotOrnaments::getOrnamentsId, ornamentsId).one();
            map.put(ornamentsId, ttRollJackpotOrnaments);
        }
        return ttRollJackpotOrnaments;
    }

    private List<Long> getSurplusOrnamentsList(Integer id, Integer jackpotId, List<TtTimeRollUser> allocatedTimeRollUsers) {
        List<TtRollJackpotOrnaments> ornamentsList =
                new LambdaQueryChainWrapper<>(rollJackpotOrnamentsMapper).eq(TtRollJackpotOrnaments::getJackpotId, jackpotId).list();
        Map<Long, Integer> data = new HashMap<>();
        for (TtRollJackpotOrnaments ornaments : ornamentsList) {
            data.put(ornaments.getOrnamentsId(), ornaments.getOrnamentsNum());
        }
        List<Long> surplusOrnamentsList = RandomUtils.toList(data);
        List<Long> allocated = allocatedTimeRollUsers.stream().map(TtTimeRollUser::getOrnamentsId).collect(Collectors.toList());
        for (Long i : allocated) {
            Iterator<Long> iterator = surplusOrnamentsList.iterator();
            while (iterator.hasNext()) {
                Long next = iterator.next();
                if (Objects.equals(next, i)) {
                    iterator.remove();
                    break;
                }
            }
        }
        return surplusOrnamentsList;
    }
}
