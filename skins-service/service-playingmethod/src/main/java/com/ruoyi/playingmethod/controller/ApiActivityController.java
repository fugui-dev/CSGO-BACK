package com.ruoyi.playingmethod.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.service.TtBonusReceiveRecordService;
import com.ruoyi.admin.service.TtBonusService;
import com.ruoyi.admin.service.TtUserBlendErcashService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.admin.util.RandomUtils;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.domain.common.constant.BonusConditionType;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBonus;
import com.ruoyi.domain.other.TtBonusReceiveRecord;
import com.ruoyi.playingmethod.utils.DateScopeUtils;
import io.jsonwebtoken.lang.Assert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ruoyi.admin.config.RedisConstants.USER_PLAY_COMMON;

@RestController
@RequestMapping("/api/activity")
@Api("活动领取")
public class ApiActivityController extends BaseController {

    @Autowired
    private TtBonusService bonusService;

    @Autowired
    private TtBonusReceiveRecordService bonusReceiveRecordService;

    @Autowired
    private TtUserBlendErcashService userBlendErcashService;

    @Autowired
    private TtUserService userService;

    @Autowired
    private RedisLock redisLock;

    @ApiOperation("充值福利活动列表")
    @GetMapping("/list")
    public R<Map> list(){

        //所有充值福利
        ArrayList<String> typeList = new ArrayList<>();
        typeList.add(BonusConditionType.DAY.getCode().toString());
        typeList.add(BonusConditionType.WEEK.getCode().toString());
        typeList.add(BonusConditionType.MONTH.getCode().toString());

        List<TtBonus> bonusList = bonusService.list(Wrappers.lambdaQuery(TtBonus.class)
                .in(TtBonus::getConditionType, typeList)
                .eq(TtBonus::getStatus, "0")
                .orderByAsc(TtBonus::getConditionType, TtBonus::getRechargeThreshold));

        Map<String, List<TtBonus>> bonusMap = bonusList.stream().collect(Collectors.groupingBy(TtBonus::getConditionType));


        //返回的MAP中追加一下进度 rechargeProcess
        selectRechargeProcess(bonusMap);

        //已领取的福利封装
        for (String conditionType : bonusMap.keySet()) { //0日充值 1周充值 2月充值
            if (conditionType.equals("0")){
                List<TtBonusReceiveRecord> todayBounsReceiveRecordList = getTodayBounsReceiveRecordList();
                packGetStatusInfo(bonusMap, conditionType, todayBounsReceiveRecordList);

            }else if (conditionType.equals("1")){
                List<TtBonusReceiveRecord> weekBounsReceiveRecordList = getWeekBounsReceiveRecordList();
                packGetStatusInfo(bonusMap, conditionType, weekBounsReceiveRecordList);

            }else if (conditionType.equals("2")){
                List<TtBonusReceiveRecord> monthBounsReceiveRecordList = getMonthBounsReceiveRecordList();
                packGetStatusInfo(bonusMap, conditionType, monthBounsReceiveRecordList);

            }

        }


        return R.ok(bonusMap);
    }

    private static void packGetStatusInfo(Map<String, List<TtBonus>> bonusMap, String conditionType, List<TtBonusReceiveRecord> bonusReceiveRecordList) {
        if (!bonusReceiveRecordList.isEmpty()){
            Map<Integer, List<TtBonusReceiveRecord>> recordMap = bonusReceiveRecordList.stream().collect(Collectors.groupingBy(TtBonusReceiveRecord::getBonusId));

            for (TtBonus bonus : bonusMap.get(conditionType)) {
                if (recordMap.get(bonus.getId()) != null){
                    bonus.setGetStatus(1);
                }else {
                    bonus.setGetStatus(0);
                }
            }
        }
    }


    //查询充值进度
    private void selectRechargeProcess(Map ProcessMap) {
        List<TtUserBlendErcash> monthRechargeList = userBlendErcashService.list(Wrappers.lambdaQuery(TtUserBlendErcash.class)
                .eq(TtUserBlendErcash::getType, TtAccountRecordType.INPUT.getCode())
                .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.RECHARGE.getCode())
                .eq(TtUserBlendErcash::getUserId, getUserId())
                .between(TtUserBlendErcash::getCreateTime, DateScopeUtils.getMonthTimeStart(), DateScopeUtils.getNow()));
        //本月总充值
        BigDecimal monthTotalRecharge = monthRechargeList.stream()
                .map(TtUserBlendErcash::getAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        //本周总充值
        List<TtUserBlendErcash> weekRechargeList = userBlendErcashService.list(Wrappers.lambdaQuery(TtUserBlendErcash.class)
                .eq(TtUserBlendErcash::getType, TtAccountRecordType.INPUT.getCode())
                .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.RECHARGE.getCode())
                .eq(TtUserBlendErcash::getUserId, getUserId())
                .between(TtUserBlendErcash::getCreateTime, DateScopeUtils.getWeekTimeStart(), DateScopeUtils.getNow()));
        BigDecimal weekTotalRecharge = weekRechargeList.stream()
                .map(TtUserBlendErcash::getAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        //今日总充值
        List<TtUserBlendErcash> dayRechargeList = userBlendErcashService.list(Wrappers.lambdaQuery(TtUserBlendErcash.class)
                .eq(TtUserBlendErcash::getType, TtAccountRecordType.INPUT.getCode())
                .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.RECHARGE.getCode())
                .eq(TtUserBlendErcash::getUserId, getUserId())
                .between(TtUserBlendErcash::getCreateTime, DateScopeUtils.getTodayBegin(), DateScopeUtils.getNow()));
        BigDecimal dayTotalRecharge = dayRechargeList.stream()
                .map(TtUserBlendErcash::getAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);


        Map<String, BigDecimal> rechargeProcess = new HashMap<>();
        rechargeProcess.put("dayTotalRecharge", dayTotalRecharge);
        rechargeProcess.put("weekTotalRecharge", weekTotalRecharge);
        rechargeProcess.put("monthTotalRecharge", monthTotalRecharge);

        ProcessMap.put("rechargeProcess", rechargeProcess);
    }

    private List<TtBonusReceiveRecord> getTodayBounsReceiveRecordList() {
        List<TtBonusReceiveRecord> list = bonusReceiveRecordService.list(Wrappers.lambdaQuery(TtBonusReceiveRecord.class)
                .eq(TtBonusReceiveRecord::getUserId, getUserId())
                .eq(TtBonusReceiveRecord::getType, BonusConditionType.DAY.getCode())
                .eq(TtBonusReceiveRecord::getStatus, 1)
                .between(TtBonusReceiveRecord::getCreateTime, DateScopeUtils.getTodayBegin(), DateScopeUtils.getTodayEnd()));
        return list;
    }

    private List<TtBonusReceiveRecord> getWeekBounsReceiveRecordList() {
        return bonusReceiveRecordService.list(Wrappers.lambdaQuery(TtBonusReceiveRecord.class)
                .eq(TtBonusReceiveRecord::getUserId, getUserId())
                .eq(TtBonusReceiveRecord::getType, BonusConditionType.WEEK.getCode())
                .eq(TtBonusReceiveRecord::getStatus, 1)
                .between(TtBonusReceiveRecord::getCreateTime, DateScopeUtils.getWeekTimeStart(), DateScopeUtils.getNow()));
    }

    private List<TtBonusReceiveRecord> getMonthBounsReceiveRecordList() {
        return bonusReceiveRecordService.list(Wrappers.lambdaQuery(TtBonusReceiveRecord.class)
                .eq(TtBonusReceiveRecord::getUserId, getUserId())
                .eq(TtBonusReceiveRecord::getType, BonusConditionType.MONTH.getCode())
                .eq(TtBonusReceiveRecord::getStatus, 1)
                .between(TtBonusReceiveRecord::getCreateTime, DateScopeUtils.getMonthTimeStart(), DateScopeUtils.getNow()));
    }

    @ApiOperation("领取充值福利")
    @GetMapping("/getRechargeGift")
    public R getRechargeGift(@ApiParam("要领取的福利ID") @Param("bonusId") Long bonusId){


        String lockKey = USER_PLAY_COMMON + "user_id:" + getUserId();
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }


        //判断福利是否存在
        TtBonus bonus = bonusService.getById(bonusId);
        Assert.isTrue(bonus != null  && bonus.getStatus().equals("0"), "福利已下线！");

//        HashMap totalMap = new HashMap<>();
//        selectRechargeProcess(totalMap);

        //是否已经领取过
        String conditionType = bonus.getConditionType();
        //日充值
        if (conditionType.equals(BonusConditionType.DAY.getCode().toString())){
            List<TtBonusReceiveRecord> dayGetRecordList = getTodayBounsReceiveRecordList();
//            Assert.isTrue(dayGetRecordList.isEmpty(), "今日充值福利已领取！");
            if (!dayGetRecordList.isEmpty()){
                for (TtBonusReceiveRecord receiveRecord : dayGetRecordList) {
                    if (receiveRecord.getBonusId().equals(bonusId.intValue())) return R.fail("当前福利已领取！");
                }
            }


            List<TtUserBlendErcash> userBlendErcashList = userBlendErcashService.list(Wrappers.lambdaQuery(TtUserBlendErcash.class)
                    .eq(TtUserBlendErcash::getType, TtAccountRecordType.INPUT.getCode())
                    .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.RECHARGE.getCode())
                    .eq(TtUserBlendErcash::getUserId, getUserId())
                    .between(TtUserBlendErcash::getCreateTime, DateScopeUtils.getTodayBegin(), DateScopeUtils.getTodayEnd()));
            Assert.isTrue(!userBlendErcashList.isEmpty(), "您未完成充值！");

            //今日总充值
            BigDecimal totalRecharge = userBlendErcashList.stream()
                    .map(TtUserBlendErcash::getAmount)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            Assert.isTrue(totalRecharge.compareTo(bonus.getRechargeThreshold()) >= 0, "您未达成福利门槛！");


            //周充值
        }else if (conditionType.equals(BonusConditionType.WEEK.getCode().toString())){
            List<TtBonusReceiveRecord> weekBounsReceiveRecordList = getWeekBounsReceiveRecordList();
//            if (!weekBounsReceiveRecordList.isEmpty()) return R.fail("本周充值福利已领取！");
            if (!weekBounsReceiveRecordList.isEmpty()){
                for (TtBonusReceiveRecord receiveRecord : weekBounsReceiveRecordList) {
                    if (receiveRecord.getBonusId().equals(bonusId.intValue())) return R.fail("当前福利已领取！");
                }
            }


            List<TtUserBlendErcash> userBlendErcashList = userBlendErcashService.list(Wrappers.lambdaQuery(TtUserBlendErcash.class)
                    .eq(TtUserBlendErcash::getType, TtAccountRecordType.INPUT.getCode())
                    .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.RECHARGE.getCode())
                    .eq(TtUserBlendErcash::getUserId, getUserId())
                    .between(TtUserBlendErcash::getCreateTime, DateScopeUtils.getWeekTimeStart(), DateScopeUtils.getNow()));
            if (userBlendErcashList.isEmpty()) return R.fail("您未完成充值！");

            //本周总充值
            BigDecimal totalRecharge = userBlendErcashList.stream()
                    .map(TtUserBlendErcash::getAmount)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            Assert.isTrue(totalRecharge.compareTo(bonus.getRechargeThreshold()) >= 0, "您未达成周福利门槛！");


            //月充值
        }else if (conditionType.equals(BonusConditionType.MONTH.getCode().toString())){
            List<TtBonusReceiveRecord> monthBounsReceiveRecordList = getMonthBounsReceiveRecordList();
//            if (!monthBounsReceiveRecordList.isEmpty()) return R.fail("本月充值福利已领取！");
            if (!monthBounsReceiveRecordList.isEmpty()){
                for (TtBonusReceiveRecord receiveRecord : monthBounsReceiveRecordList) {
                    if (receiveRecord.getBonusId().equals(bonusId.intValue())) return R.fail("当前福利已领取！");
                }
            }

            List<TtUserBlendErcash> userBlendErcashList = userBlendErcashService.list(Wrappers.lambdaQuery(TtUserBlendErcash.class)
                    .eq(TtUserBlendErcash::getType, TtAccountRecordType.INPUT.getCode())
                    .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.RECHARGE.getCode())
                    .eq(TtUserBlendErcash::getUserId, getUserId())
                    .between(TtUserBlendErcash::getCreateTime, DateScopeUtils.getMonthTimeStart(), DateScopeUtils.getNow()));
            if (userBlendErcashList.isEmpty()) return R.fail("您未完成充值！");

            //今日总充值
            BigDecimal totalRecharge = userBlendErcashList.stream()
                    .map(TtUserBlendErcash::getAmount)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            Assert.isTrue(totalRecharge.compareTo(bonus.getRechargeThreshold()) >= 0, "您未达成周福利门槛！");


        }else {
            return R.fail("目前只开启日/周/月充值福利！！");

        }

        //领取成功？

        BigDecimal awardMoney = RandomUtils.getRandomPrice(bonus.getAwardSection());
//        String section = currentBonus.getAwardSection();
        Long userId = getUserId();
        TtUser user = userService.getById(userId);
        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate
                .eq(TtUser::getUserId, getUserId())
                .setSql("account_amount = account_amount + " + awardMoney);
        boolean update = userService.update(userUpdate);

        //写入记录
        TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                .userId(user.getUserId())
                .amount(awardMoney.compareTo(BigDecimal.ZERO) > 0 ? awardMoney : null)
                .finalAmount(awardMoney.compareTo(BigDecimal.ZERO) > 0 ? user.getAccountAmount().add(awardMoney) : null)
                .total(awardMoney)
                .type(TtAccountRecordType.INPUT.getCode())
                .source(TtAccountRecordSource.BONUS.getCode())
                .remark(TtAccountRecordSource.BONUS.getMsg())
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        boolean save = userBlendErcashService.save(blendErcash);

        //写入领取记录
        TtBonusReceiveRecord receiveRecord = new TtBonusReceiveRecord();
        receiveRecord.setType(bonus.getConditionType()); //充值福利类型
        receiveRecord.setBonusId(bonusId.intValue());
        receiveRecord.setUserId(user.getUserId());
        receiveRecord.setAwardType("0"); //金币
        receiveRecord.setAwardId(null); //金币
        receiveRecord.setAwardPrice(awardMoney);
        receiveRecord.setStatus("1");
        Date now = new Date();
        receiveRecord.setReceiveTime(now);
        receiveRecord.setCreateTime(now);
        boolean save1 = bonusReceiveRecordService.save(receiveRecord);


        Assert.isTrue(update && save && save1, "人数太多，请重试！");

        redisLock.unlock(lockKey);

        return R.ok(awardMoney, "领取成功！");
    }




//    @ApiOperation("领取注册福利")
//    @GetMapping("/one")
//    public R one(){
//
//
//        return R.ok(100);
//    }
//
//    @ApiOperation("领取每日福利")
//    @GetMapping("/two")
//    @Transactional(rollbackFor = Exception.class)
//    public R two(){
//
//        //这里需要加锁！
//        List<TtBonusReceiveRecord> list = getTodayBounsReceiveRecordList();
//        if (!list.isEmpty()) return R.fail("今日已领取福利！");
//
//        //计算今日多少充值可以领取
//        List<TtUserBlendErcash> userBlendErcashList = userBlendErcashService.list(Wrappers.lambdaQuery(TtUserBlendErcash.class)
//                .eq(TtUserBlendErcash::getType, TtAccountRecordType.INPUT.getCode())
//                .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.RECHARGE.getCode())
//                .eq(TtUserBlendErcash::getUserId, getUserId())
//                .between(TtUserBlendErcash::getCreateTime, DateScopeUtils.getTodayBegin(), DateScopeUtils.getTodayEnd()));
//        if (userBlendErcashList.isEmpty()) return R.fail("您未完成充值！");
//
//        BigDecimal totalRecharge = userBlendErcashList.stream()
//                .map(TtUserBlendErcash::getAmount)
//                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);//今日总充值
//
//        //计算充值达到了哪些标准
//        List<TtBonus> bonusList = bonusService.list(Wrappers.lambdaQuery(TtBonus.class)
//                .eq(TtBonus::getConditionType, BonusConditionType.DAY.getCode())
//                .eq(TtBonus::getStatus, "0")
//                .orderByAsc(TtBonus::getRechargeThreshold));
//
//        TtBonus currentBonus = null; //当前处于福利等级
//        for (TtBonus bonus : bonusList) {
//            if (totalRecharge.compareTo(bonus.getRechargeThreshold()) >= 0) currentBonus = bonus;
//        }
//        if (currentBonus == null) return R.fail("您未达成福利门槛！");
//
//        BigDecimal awardMoney = RandomUtils.getRandomPrice(currentBonus.getAwardSection());
////        String section = currentBonus.getAwardSection();
//        Long userId = getUserId();
//        TtUser user = userService.getById(userId);
//        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
//        userUpdate
//                .eq(TtUser::getUserId, getUserId())
//                .setSql("account_amount = account_amount + " + awardMoney);
//        boolean update = userService.update(userUpdate);
//
//        //写入记录
//        TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
//                .userId(user.getUserId())
//                .amount(awardMoney.compareTo(BigDecimal.ZERO) > 0 ? awardMoney : null)
//                .finalAmount(awardMoney.compareTo(BigDecimal.ZERO) > 0 ? user.getAccountAmount().add(awardMoney) : null)
//                .total(awardMoney)
//                .type(TtAccountRecordType.INPUT.getCode())
//                .source(TtAccountRecordSource.BONUS.getCode())
//                .remark(TtAccountRecordSource.BONUS.getMsg())
//                .createTime(new Timestamp(System.currentTimeMillis()))
//                .build();
//        boolean save = userBlendErcashService.save(blendErcash);
//
//        //写入领取记录
//        TtBonusReceiveRecord receiveRecord = new TtBonusReceiveRecord();
//        receiveRecord.setType("2"); //充值福利类型
//        receiveRecord.setBonusId(currentBonus.getId());
//        receiveRecord.setUserId(user.getUserId());
//        receiveRecord.setAwardType("金币");
//        receiveRecord.setAwardId(0); //金币
//        receiveRecord.setAwardPrice(awardMoney);
//        receiveRecord.setStatus("1");
//        Date now = new Date();
//        receiveRecord.setReceiveTime(now);
//        receiveRecord.setCreateTime(now);
//        boolean save1 = bonusReceiveRecordService.save(receiveRecord);
//
//
//        Assert.isTrue(update && save && save1, "人数太多，请重试！");
//
//        return R.ok(awardMoney, "领取成功！");
//    }
//
//    @ApiOperation("领取每月福利")
//    @GetMapping("/three")
//    public R three(){
//        return R.fail("您未达成月福利门槛！");
//    }

}