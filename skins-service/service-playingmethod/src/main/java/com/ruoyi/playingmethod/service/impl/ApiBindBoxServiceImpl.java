package com.ruoyi.playingmethod.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.mapper.TtBoxOrnamentsMapper;
import com.ruoyi.admin.mapper.TtBoxTypeMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.admin.util.core.fight.LotteryMachine;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.common.constant.TtboxRecordSource;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.dto.queryCondition.OrnamentCondition;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.*;
import com.ruoyi.domain.vo.OpenBoxVO;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;
import com.ruoyi.playingmethod.entity.request.OpenBox2Request;
import com.ruoyi.playingmethod.mapper.ApiBindBoxMapper;
import com.ruoyi.playingmethod.mapper.ApiTtUserBlendErcashMapper;
import com.ruoyi.playingmethod.service.ApiBindBoxService;
import com.ruoyi.playingmethod.utils.customException.OrnamentNullException;
import com.ruoyi.playingmethod.websocket.WsBindBox;
import com.ruoyi.playingmethod.websocket.constant.SMsgKey;
import com.ruoyi.playingmethod.websocket.util.WsResult;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ruoyi.admin.config.RedisConstants.USER_PLAY_COMMON;
import static com.ruoyi.domain.common.constant.TtboxRecordSource.ROLL;
import static com.ruoyi.domain.common.constant.TtboxRecordStatus.IN_PACKSACK_ON;
import static java.math.BigDecimal.ROUND_HALF_UP;

@Service
@Slf4j
public class ApiBindBoxServiceImpl implements ApiBindBoxService {

    private final TtUserService userService;
    private final TtBoxService boxService;
    private final TtBoxTypeMapper boxTypeMapper;
    private final ApiBindBoxMapper bindBoxMapper;
    private final TtBoxOrnamentsMapper boxOrnamentsMapper;
    private final TtBoxRecordsService boxRecordsService;
    private final RedisCache redisCache;
    private final ThreadPoolExecutor customThreadPoolExecutor;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private LotteryMachine lotteryMachine;

    @Autowired
    private ApiTtUserBlendErcashMapper apiTtUserBlendErcashMapper;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private TtOrnamentService ornamentService;

    public ApiBindBoxServiceImpl(TtUserService userService,
                                 TtBoxService boxService,
                                 TtBoxTypeMapper boxTypeMapper,
                                 ApiBindBoxMapper bindBoxMapper,
                                 TtBoxOrnamentsMapper boxOrnamentsMapper,
                                 TtBoxRecordsService boxRecordsService,
                                 RedisCache redisCache,
                                 ThreadPoolExecutor customThreadPoolExecutor) {
        this.userService = userService;
        this.boxService = boxService;
        this.boxTypeMapper = boxTypeMapper;
        this.bindBoxMapper = bindBoxMapper;
        this.boxOrnamentsMapper = boxOrnamentsMapper;
        this.boxRecordsService = boxRecordsService;
        this.redisCache = redisCache;
        this.customThreadPoolExecutor = customThreadPoolExecutor;
    }

    //@PostConstruct
    public void postConstruct() {
        List<Integer> boxIds = boxOrnamentsMapper.selectBoxIdList();
        if (ObjectUtils.isEmpty(boxIds)) return;
        for (Integer boxId : boxIds) {
            boxService.isReplenishment(boxId);
        }
    }

    @Override
    public TtBoxA getBoxData(Integer boxId) {

        TtBoxA boxData = bindBoxMapper.getBoxData(boxId);
        if (ObjectUtils.isEmpty(boxData)) return null;

        List<TtOrnamentsA> boxOrnamentsList = bindBoxMapper.getBoxOrnamentsList(boxId);
        if (ObjectUtils.isEmpty(boxOrnamentsList)) return null;

        // 计算概率
        BigDecimal sum = new BigDecimal(boxOrnamentsList.stream().mapToInt(TtOrnamentsA::getOdds).sum());
        BigDecimal oneHundred = new BigDecimal("100.00000000");
        BigDecimal max = BigDecimal.ZERO;
        Long maxId = null;
        BigDecimal add = BigDecimal.ZERO;
        for (TtOrnamentsA vo : boxOrnamentsList) {
            Integer odds = vo.getOdds();
            BigDecimal oddsResult = new BigDecimal(odds).divide(sum, 8, RoundingMode.HALF_UP).multiply(oneHundred);
            add = oddsResult.add(add);
            if (max.compareTo(oddsResult) <= 0) {
                max = oddsResult;
                maxId = vo.getOrnamentId();
            }
            vo.setOddsResult(oddsResult);
        }

        // 平差
        BigDecimal erroValue = oneHundred.subtract(add);
        for (TtOrnamentsA vo : boxOrnamentsList) {
            if (vo.getOrnamentId().equals(maxId)) {
                vo.setOddsResult(vo.getOddsResult().add(erroValue));
                break;
            }
        }

        if (ObjectUtils.isEmpty(boxOrnamentsList)) return null;
        boxOrnamentsList = boxOrnamentsList.stream().peek(boxOrnamentsDataVO -> boxOrnamentsDataVO.setOdds(null)).collect(Collectors.toList());
        List<TtBoxLevelA> probabilityDistribution = bindBoxMapper.getProbabilityDistribution(boxId);
        boxData.setBoxOrnamentsList(boxOrnamentsList);
        boxData.setProbabilityDistribution(probabilityDistribution);

        return boxData;
    }

    @Override
    public List<TtBoxVO> getBoxList(Integer boxTypeId, String homeFlag, Integer isFight) {

        List<TtBoxA> boxList = bindBoxMapper.getBoxList(boxTypeId, homeFlag, isFight);

        // 过滤空箱子
        Set<Integer> set = boxList.stream().map(TtBoxA::getBoxId).collect(Collectors.toSet());
        List<TtBoxOrnaments> boxOrnamentsList = new LambdaQueryChainWrapper<>(boxOrnamentsMapper).in(TtBoxOrnaments::getBoxId, set).list();
        Map<Integer, List<TtBoxOrnaments>> groupMap = boxOrnamentsList.stream().collect(Collectors.groupingBy(TtBoxOrnaments::getBoxId));

        List<TtBoxA> collect = boxList.stream().filter(item -> {

//            List<TtBoxOrnaments> list = new LambdaQueryChainWrapper<>(boxOrnamentsMapper)
//                    .eq(TtBoxOrnaments::getBoxId, item.getBoxId())
//                    .list();
            List<TtBoxOrnaments> list = groupMap.get(item.getBoxId());
            if (ObjectUtil.isEmpty(list) || list.isEmpty()) return false;

            boolean flag = false;
            for (TtBoxOrnaments ornaments : list) {
                if (ornaments.getRealOdds() > 0) {
                    return !flag;
                }
            }
            return flag;

        }).collect(Collectors.toList());

        return groupByBoxType(collect, isFight);
    }

    @Override
    public List<TtBoxVO> groupByBoxType(List<TtBoxA> boxData, Integer isFight) {
        if (boxData.isEmpty()){
            return Collections.emptyList();
        }

        Map<Integer, List<TtBoxA>> collect = boxData.stream().collect(Collectors.groupingBy(TtBoxA::getBoxTypeId));
        List<TtBoxVO> resultList = new ArrayList<>();

        //查询所有id集合
        List<TtBoxType> boxTypeList = boxTypeMapper.selectList(Wrappers.lambdaQuery(TtBoxType.class)
                .in(TtBoxType::getId, collect.keySet())
//                .eq(TtBoxType::getIsFightType, isFight)
        );
        Map<Integer, TtBoxType> typeMap = boxTypeList.stream().collect(Collectors.toMap(TtBoxType::getId, Function.identity()));

        for (Map.Entry<Integer, List<TtBoxA>> entry : collect.entrySet()) {
            Integer key = entry.getKey();
            List<TtBoxA> value = entry.getValue();
//            TtBoxType ttBoxType = boxTypeMapper.selectById(key);
            TtBoxType ttBoxType = typeMap.get(key);
            if (StringUtils.isNull(ttBoxType)) continue;
            TtBoxVO boxTypeA = TtBoxVO.builder().build();
            boxTypeA.setBoxTypeId(ttBoxType.getId());
            boxTypeA.setBoxTypeName(ttBoxType.getName());
            boxTypeA.setIcon(ttBoxType.getIcon());
            boxTypeA.setBoxList(value);
            resultList.add(boxTypeA);
        }
        return resultList;
    }

    @Override
    public List<TtOrnamentsA> openBox(TtBox ttBox, Integer num, TtUser ttUser) {
        return null;
    }

    @Override
    public List<TtBoxRecords> addBoxRecord(TtUser ttUser, TtBox ttBox, Integer num) {
        return null;
    }

    @Override
    public List<TtBoxRecords> openBoxArithmetic(Integer fightId, TtUser ttUser, TtBox ttBox, Integer num) {
        return null;
    }

    // @Override
    // public List<TtOrnamentsA> openBox(TtBox ttBox, Integer num, TtUser ttUser) {
    //
    //     List<TtOrnamentsA> resultOrnamentsList = new ArrayList<>();
    //
    //     // 计算结果并保存结果
    //     List<TtBoxRecords> boxRecordsList = addBoxRecord(ttUser, ttBox, num);
    //
    //     // 补充结果信息
    //     for (TtBoxRecords ttBoxRecords : boxRecordsList) {
    //         ttBoxRecords.setSource("0");
    //         ttBoxRecords.setHolderUserId(ttUser.getUserId());
    //         TtOrnamentsA ornamentsData = bindBoxMapper.getOrnamentsData(ttBox.getBoxId(), ttBoxRecords.getOrnamentsId(), ttBoxRecords.getId());
    //         resultOrnamentsList.add(ornamentsData);
    //     }
    //     boxRecordsService.updateBatchById(boxRecordsList, 1);
    //
    //     // 更新宝箱开启次数
    //     CompletableFuture.runAsync(() -> {
    //         ttBox.setOpenNum(ttBox.getOpenNum() + num);
    //         if (boxService.updateById(ttBox)) log.info("更新宝箱开启次数成功！");
    //     }, customThreadPoolExecutor);
    //
    //     // 更新用户账户
    //     BigDecimal openBoxBeanTotal = ttBox.getPrice().multiply(new BigDecimal(num));
    //     ttUser.setAccountAmount(ttUser.getAccountAmount().subtract(openBoxBeanTotal));
    //     // 消费多少金币，赠送多少弹药
    //     // ttUser.setAccountCredits(ttUser.getAccountCredits().add(openBoxBeanTotal));
    //     userService.updateById(ttUser);
    //
    //     // 账户变更记录
    //     CompletableFuture.runAsync(() -> {
    //
    //         // 统计推广福利
    //         pWelfareMQData msgDate = pWelfareMQData.builder()
    //                 .userId(ttUser.getUserId())
    //                 .account(openBoxBeanTotal)
    //                 .createTime(new Timestamp(System.currentTimeMillis()))
    //                 .build();
    //
    //         TtUser user = userService.getById(msgDate.getUserId());
    //         TtUser parent = userService.getById(user.getParentId());
    //         if (ObjectUtil.isNotEmpty(parent)){
    //             // 推广人奖励
    //             BigDecimal credits = msgDate.getAccount().multiply(new BigDecimal("0.03"));
    //             LambdaUpdateWrapper<TtUser> wrapper = new LambdaUpdateWrapper<>();
    //             wrapper
    //                     .eq(TtUser::getUserId,parent.getUserId())
    //                     .set(TtUser::getAccountCredits,parent.getAccountCredits().add(credits));    //奖励弹药
    //                     // .set(TtUser::getAccountAmount,parent.getAccountAmount().add(credits));   //奖励金币
    //             userService.update(wrapper);
    //             // 弹药日志
    //             userService.insertUserCreditsRecords(parent.getUserId(),
    //                     TtAccountRecordType.INPUT,
    //                     TtAccountRecordSource.P_WELFARE,
    //                     credits,
    //                     parent.getAccountCredits().add(credits),
    //                     user.getUserId(),
    //                     user.getNickName(),
    //                     msgDate.getAccount());
    //             // 金币日志
    //             // userService.insertUserAmountRecords(parent.getUserId(),
    //             //         "1",
    //             //         "13",
    //             //         credits,
    //             //         parent.getAccountAmount().add(credits),
    //             //         user.getUserId(),
    //             //         user.getNickName(),
    //             //         msgDate.getAccount());
    //
    //             log.info("【统计推广福利】成功。");
    //         }
    //
    //         // 账户变更日志
    //         userService.insertUserAmountRecords(ttUser.getUserId(), TtAccountRecordType.OUTPUT, TtAccountRecordSource.GAME_TYPE_01, openBoxBeanTotal.negate(), ttUser.getAccountAmount());
    //         // userService.insertUserCreditsRecords(ttUser.getUserId(), "2", "1", openBoxBeanTotal, ttUser.getAccountCredits());
    //
    //     }, customThreadPoolExecutor);
    //
    //     return resultOrnamentsList;
    // }

    // 开箱算法
    // @Override
    // public List<TtBoxRecords> openBoxArithmetic(Integer fightId,TtUser player, TtBox ttBox, Integer num) {
    //
    //     List<Integer> openBoxResultList;
    //
    //     // 获取爆率
    //     if ("02".equals(player.getUserType())) {
    //         openBoxResultList = getOpenBoxResultList(ttBox.getBoxId(), false, num);
    //     }else {
    //         openBoxResultList = getOpenBoxResultList(ttBox.getBoxId(), true, num);
    //     }
    //
    //     // 保存开箱记录
    //     List<TtBoxRecords> boxRecordsList = new ArrayList<>();
    //     if (!StringUtils.isNull(openBoxResultList) && !openBoxResultList.isEmpty()) {
    //         for (Integer ornamentsId : openBoxResultList) {
    //             TtOrnamentsA ornamentsData = bindBoxMapper.getOrnamentsData(ttBox.getBoxId(), ornamentsId, null);
    //             TtBoxRecords boxRecords = TtBoxRecords.builder().build();
    //             boxRecords.setUserId(player.getUserId());
    //             boxRecords.setBoxId(ttBox.getBoxId());
    //             boxRecords.setBoxName(ttBox.getBoxName());
    //             boxRecords.setBoxPrice(ttBox.getPrice());
    //             boxRecords.setOrnamentsId(Integer.valueOf(ornamentsData.getOrnamentId()));
    //             boxRecords.setOrnamentsPrice(ornamentsData.getUsePrice());
    //             boxRecords.setOrnamentsLevelId(ornamentsData.getOrnamentsLevelId());
    //             boxRecords.setCreateTime(DateUtils.getNowDate());
    //             boxRecords.setHolderUserId(player.getUserId());
    //             boxRecords.setSource("0");// TODO: 2024/3/14 source
    //             boxRecords.setStatus("0");// TODO: 2024/3/14
    //             boxRecords.setIsShow(0);
    //             boxRecords.setFightId(fightId);
    //             boxRecordsList.add(boxRecords);
    //         }
    //         boxRecordsService.saveBatch(boxRecordsList, 1);
    //     }
    //
    //     // 更新宝箱开启次数
    //     CompletableFuture.runAsync(() -> {
    //         ttBox.setOpenNum(ttBox.getOpenNum() + num);
    //         if (boxService.updateById(ttBox)) log.info("更新宝箱开启次数成功！");
    //     }, customThreadPoolExecutor);
    //
    //     return boxRecordsList;
    // }

    // @Override
    // public List<TtBoxRecords> addBoxRecord(TtUser ttUser, TtBox ttBox, Integer num) {
    //     List<Integer> openBoxResultList;
    //
    //     if ("02".equals(ttUser.getUserType())) {
    //         openBoxResultList = getOpenBoxResultList(ttBox.getBoxId(), false, num);
    //     }else {
    //         openBoxResultList = getOpenBoxResultList(ttBox.getBoxId(), true, num);
    //     }
    //
    //     List<TtBoxRecords> boxRecordsList = new ArrayList<>();
    //     if (!StringUtils.isNull(openBoxResultList) && !openBoxResultList.isEmpty()) {
    //         for (Integer ornamentsId : openBoxResultList) {
    //             TtOrnamentsA ornamentsData = bindBoxMapper.getOrnamentsData(ttBox.getBoxId(), ornamentsId, null);
    //             TtBoxRecords boxRecords = TtBoxRecords.builder().build();
    //             boxRecords.setUserId(ttUser.getUserId());
    //             boxRecords.setBoxId(ttBox.getBoxId());
    //             boxRecords.setBoxName(ttBox.getBoxName());
    //             boxRecords.setBoxPrice(ttBox.getPrice());
    //             boxRecords.setOrnamentsId(Integer.valueOf(ornamentsData.getOrnamentId()));
    //             boxRecords.setOrnamentsPrice(ornamentsData.getUsePrice());
    //             boxRecords.setOrnamentsLevelId(ornamentsData.getOrnamentsLevelId());
    //             boxRecords.setCreateTime(DateUtils.getNowDate());
    //             boxRecordsList.add(boxRecords);
    //         }
    //         boxRecordsService.saveBatch(boxRecordsList, 1);
    //     }
    //     return boxRecordsList;
    // }

    // @Override
    // public List<TtBoxUser> getBindBoxHistory(ApiBindBoxController.boxHistoryParam param) {
    //     return bindBoxMapper.getBindBoxHistory(param);
    // }


    @Override
    @Transactional
    public R blindBox(TtUser user, TtBox box, Integer num) {

        String lockKey = USER_PLAY_COMMON + "user_id:" + user.getUserId();
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        BigDecimal openBoxBeanTotal = box.getPrice().multiply(new BigDecimal(num));
        if (user.getAccountAmount().compareTo(openBoxBeanTotal) < 0) return R.fail("您的账户游戏币不足！");

        // 同步扣款
        BigDecimal totalmoney = box.getPrice().multiply(new BigDecimal(num));
        R<Map<String, BigDecimal>> mapR = upgradeAccounting(totalmoney, user);
        if (!mapR.getCode().equals(200)) {
            return mapR;
        }

        Map<String, BigDecimal> moneyMap = mapR.getData();
        moneyMap.put("total", totalmoney);

        // 抽奖
        List<TtBoxRecords> boxRecords = new ArrayList<>();
        for (int i = 0; i < num; i++) {

            String ornamentId = lotteryMachine.singleLottery(user, box);

            if (StringUtils.isBlank(ornamentId)) {
                throw new OrnamentNullException("单次抽奖无结果#"+box.getBoxId()+"#"+ornamentId);
                //return R.fail("系统繁忙，请稍后重试。");
            }

            // 宝箱详细信息
            TtOrnamentsA ornamentsData = bindBoxMapper.ornamentsInfo(box.getBoxId(), ornamentId);

            if (ObjectUtil.isEmpty(ornamentsData)){
                throw new OrnamentNullException("没有符合条件的宝箱信息#"+box.getBoxId()+"#"+ornamentId);
            }

            // 抽奖成功，构建开箱记录数据
            TtBoxRecords boxRecord = initBoxRecord(user, box, ornamentId, ornamentsData);

            boxRecords.add(boxRecord);
        }

        // 同步保存游戏记录
        boxRecordsService.saveBatch(boxRecords);

        // 返回值
        List<OpenBoxVO> openBoxVOs = boxRecords.stream().map(item -> {
            OpenBoxVO openBoxVO = new OpenBoxVO();
            BeanUtil.copyProperties(item, openBoxVO);
            openBoxVO.setUsePrice(openBoxVO.getOrnamentsPrice());
            openBoxVO.setLevelImg(item.getOrnamentLevelImg());
            openBoxVO.setOrnamenName(item.getOrnamentName());
            return openBoxVO;
        }).collect(Collectors.toList());

        // 异步任务 更新账户信息
        bindBoxAccounting(user, box, moneyMap);
        // 异步任务 广播开箱消息
        broadcastToBoxRoom(boxRecords);

        redisLock.unlock(lockKey);

        return R.ok(openBoxVOs);
    }

    //机器人开箱流程（不扣库存，不扣余额，保存开箱记录）
    @Override
    public R blindBoxReboot(TtUser user, TtBox box, Integer num) {

        // 抽奖
        List<TtBoxRecords> boxRecords = new ArrayList<>();
        for (int i = 0; i < num; i++) {

            String ornamentId = lotteryMachine.singleLottery(user, box, true);

            if (StringUtils.isBlank(ornamentId)) {
                log.info("机器人开箱单次抽奖无结果，停止开箱...");
                return R.fail();
            }

            // 宝箱详细信息
            TtOrnamentsA ornamentsData = bindBoxMapper.ornamentsInfo(box.getBoxId(), ornamentId);

            if (ObjectUtil.isEmpty(ornamentsData)){
                log.info("机器人开箱没有符合条件的宝箱信息【{}】，停止开箱...", box.getBoxId()+"#"+ornamentId);
                return R.fail();
            }

            // 抽奖成功，构建开箱记录数据
            TtBoxRecords boxRecord = initBoxRecord(user, box, ornamentId, ornamentsData);
            boxRecords.add(boxRecord);
        }

        // 同步保存游戏记录
        boxRecordsService.saveBatch(boxRecords);

        // 异步任务 广播开箱消息
        broadcastToBoxRoom(boxRecords);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<List<OpenBoxVO>> openBox2(OpenBox2Request openBox2Request) {
        TtUser user = userService.getById(SecurityUtils.getUserId());

        //1.查看当前背包饰品状态
        Set<Long> packageIds = openBox2Request.getPackageIds();
        List<TtBoxRecords> boxRecordList = boxRecordsService.listByIds(packageIds);

        BigDecimal packOrnPriceTotal = new BigDecimal(0); //背包饰品总价值
        for (TtBoxRecords ttBoxRecord : boxRecordList) {
            //如果饰品不在背包了，直接异常
            if (ttBoxRecord.getStatus() != 0){
                return R.fail("饰品已不在背包，请刷新背包！");
            }
            packOrnPriceTotal = packOrnPriceTotal.add(ttBoxRecord.getOrnamentsPrice()); //累加饰品总价值
            ttBoxRecord.setStatus(TtboxRecordStatus.RESOLVE.getCode());
        }

        //2.计算价值是否匹配（注入饰品价值+金币价值）
        TtBox box = boxService.getById(openBox2Request.getBoxId());
        BigDecimal subtract = packOrnPriceTotal.subtract(box.getPrice().multiply(new BigDecimal(openBox2Request.getNum()))); //背包饰品注入的差额
        Assert.isTrue(subtract.compareTo(BigDecimal.ZERO) >= 0, "注入饰品不足！");
        Assert.isTrue(user.getAccountCredits().compareTo(box.getPrice()) >= 0, "弹药不足！");

        //3.开箱
        String lockKey = USER_PLAY_COMMON + "user_id:" + user.getUserId();
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        //分解背包饰品+第二套货币扣款
        BigDecimal totalmoney = box.getPrice().multiply(new BigDecimal(openBox2Request.getNum()));
        R<Map<String, BigDecimal>> mapR = upgradeAccounting2(totalmoney, user);
        if (!mapR.getCode().equals(200)) {
            return R.fail(mapR.getMsg());
        }
        Map<String, BigDecimal> moneyMap = mapR.getData();
        moneyMap.put("total", totalmoney);

        boolean saveBatch = boxRecordsService.updateBatchById(boxRecordList);//更新背包饰品状态
        Assert.isTrue(saveBatch, "消费饰品失败！");

        // 抽奖
        List<TtBoxRecords> boxRecords = new ArrayList<>();
        for (int i = 0; i < openBox2Request.getNum(); i++) {

            String ornamentId = lotteryMachine.singleLottery(user, box);

            if (StringUtils.isBlank(ornamentId)) {
                throw new OrnamentNullException("单次抽奖无结果#"+box.getBoxId()+"#"+ornamentId);
                //return R.fail("系统繁忙，请稍后重试。");
            }

            // 宝箱详细信息
            TtOrnamentsA ornamentsData = bindBoxMapper.ornamentsInfo(box.getBoxId(), ornamentId);

            if (ObjectUtil.isEmpty(ornamentsData)){
                throw new OrnamentNullException("没有符合条件的宝箱信息#"+box.getBoxId()+"#"+ornamentId);
            }

            // 抽奖成功，构建开箱记录数据
            TtBoxRecords boxRecord = initBoxRecord(user, box, ornamentId, ornamentsData);

            boxRecords.add(boxRecord);
        }

        //4.如果存在差价，还要补差额80~100随机赠送一个物品
        if (subtract.compareTo(BigDecimal.ZERO) > 0){
            BigDecimal minPrice = subtract.multiply(new BigDecimal("0.90"));
            BigDecimal maxPrice = subtract.multiply(new BigDecimal("1"));

            //查询饰品中处于该价值区间的饰品，随机一个
            List<SimpleOrnamentVO> list = ornamentService.byCondition2(new OrnamentCondition(minPrice, maxPrice));
            if (!list.isEmpty()){
                int index = new Random().nextInt(list.size());
                SimpleOrnamentVO ornamentVO = list.get(index);

                addGiftOrnament(user, box, boxRecords, ornamentVO);
            }
        }

        // 开箱结果
        List<OpenBoxVO> openBoxVOs = boxRecords.stream().map(item -> {
            OpenBoxVO openBoxVO = new OpenBoxVO();
            BeanUtil.copyProperties(item, openBoxVO);
            openBoxVO.setUsePrice(openBoxVO.getOrnamentsPrice());
            openBoxVO.setLevelImg(item.getOrnamentLevelImg());
            openBoxVO.setOrnamenName(item.getOrnamentName());
            openBoxVO.setOpenBox2Gift(item.getIsOpenBox2Gift());
            return openBoxVO;
        }).collect(Collectors.toList());

        // 同步保存游戏记录(库存)
        boxRecordsService.saveBatch(boxRecords);

        // 异步任务 更新账户信息
        bindBoxAccounting(user, box, moneyMap);
        // 异步任务 广播开箱消息
        broadcastToBoxRoom(boxRecords);

        redisLock.unlock(lockKey); //释放锁

        return R.ok(openBoxVOs);
    }

    private void addGiftOrnament(TtUser user, TtBox box, List<TtBoxRecords> boxRecords, SimpleOrnamentVO ornamentVO) {
        TtBoxRecords boxRecord1 = new TtBoxRecords();
        boxRecord1.setUserId(user.getUserId());
        boxRecord1.setBoxId(box.getBoxId());
        boxRecord1.setBoxName(box.getBoxName());
        boxRecord1.setBoxPrice(box.getPrice());
        boxRecord1.setOrnamentId(ornamentVO.getOrnamentId());
        boxRecord1.setOrnamentName(ornamentVO.getOrnamentName());
        boxRecord1.setOrnamentsPrice(ornamentVO.getOrnamentPrice());
        boxRecord1.setImageUrl(ornamentVO.getOrnamentImgUrl());
        boxRecord1.setOrnamentsLevelId(ornamentVO.getOrnamentLevel() == null ? null : Integer.valueOf(ornamentVO.getOrnamentLevel()));
        boxRecord1.setOrnamentLevelImg(ornamentVO.getOrnamentLevelImg() == null ? null : ornamentVO.getOrnamentLevelImg());
        boxRecord1.setStatus(IN_PACKSACK_ON.getCode());
        boxRecord1.setCreateTime(new Date());
        boxRecord1.setSource(TtboxRecordSource.BLIND_BOX.getCode());
        boxRecord1.setHolderUserId(user.getUserId());
        boxRecord1.setIsOpenBox2Gift(true);

        boxRecords.add(boxRecord1);
    }

    private TtBoxRecords initBoxRecord(TtUser user, TtBox box, String ornamentId, TtOrnamentsA ornamentsData) {
        TtBoxRecords boxRecord = TtBoxRecords.builder()
                .userId(user.getUserId())

                .boxId(box.getBoxId())
                .boxName(box.getBoxName())
                .boxPrice(box.getPrice())

                .ornamentId(Long.valueOf(ornamentId))
                // .marketHashName(ornamentsData.)
                .ornamentName(ObjectUtil.isNotEmpty(ornamentsData.getName()) ? ornamentsData.getName() : ObjectUtil.isNotEmpty(ornamentsData.getShortName()) ? ornamentsData.getShortName() : "无名称")
                .imageUrl(ornamentsData.getImageUrl())
                .ornamentsPrice(ornamentsData.getUsePrice())
                .ornamentsLevelId(ornamentsData.getOrnamentsLevelId())
                .ornamentLevelImg(ornamentsData.getLevelImg())

                .holderUserId(user.getUserId())

                .source(TtboxRecordSource.BLIND_BOX.getCode())
                .status(IN_PACKSACK_ON.getCode())
                .isOpenBox2Gift(false)

                .createTime(new Timestamp(System.currentTimeMillis()))
                .updateTime(new Timestamp(System.currentTimeMillis()))
                .build();
        return boxRecord;
    }

    // 扣款
    public R<Map<String, BigDecimal>> upgradeAccounting(BigDecimal consumption, TtUser player) {

        // 再次检查余额
        player = userService.getById(player.getUserId());
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

        userService.update(userUpdate);

        return R.ok(map);
    }
    // 扣款2(只扣除第二套货币，也就是弹药)
    public R<Map<String, BigDecimal>> upgradeAccounting2(BigDecimal consumption, TtUser player) {

        // 再次检查余额
        player = userService.getById(player.getUserId());
        if (player.getAccountCredits().compareTo(consumption) < 0) {
            return R.fail("弹药不足");
        }

        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate.eq(TtUser::getUserId, player.getUserId());

        userUpdate.set(TtUser::getAccountCredits, player.getAccountCredits().subtract(consumption));
        userService.update(userUpdate);

        Map<String, BigDecimal> map;
        map = MapUtil.builder("Credits", consumption).map();

        return R.ok(map);
    }

    // 异步更新账户信息
    public void bindBoxAccounting(TtUser player, TtBox box, Map<String, BigDecimal> moneyMap) {

        CompletableFuture.runAsync(() -> {

            BigDecimal Amount = moneyMap.get("Amount");
            BigDecimal Credits = moneyMap.get("Credits");
            BigDecimal total = moneyMap.get("total");

            TtUser userById = userService.getById(player.getUserId());

            // 综合消费日志
            TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                    .userId(player.getUserId())

                    .amount(ObjectUtil.isNotEmpty(Amount) ? Amount.negate() : null)
                    .finalAmount(ObjectUtil.isNotEmpty(Amount) ? userById.getAccountAmount().subtract(Amount) : null)

                    .credits(ObjectUtil.isNotEmpty(Credits) ? Credits.negate() : null)
                    .finalCredits(ObjectUtil.isNotEmpty(Credits) ? userById.getAccountCredits().subtract(Credits) : null)

                    .total(total.negate())  // 收支合计

                    .type(TtAccountRecordType.OUTPUT.getCode())
                    .source(TtAccountRecordSource.GAME_TYPE_01.getCode())
                    .remark(TtAccountRecordSource.GAME_TYPE_01.getMsg())

                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()))
                    .build();

            if (blendErcash.getFinalAmount() != null && BigDecimal.ZERO.compareTo(blendErcash.getFinalAmount()) > 0){
                blendErcash.setFinalAmount(BigDecimal.ZERO);
            }
            if (blendErcash.getFinalCredits() != null && BigDecimal.ZERO.compareTo(blendErcash.getFinalCredits()) > 0){
                blendErcash.setFinalCredits(BigDecimal.ZERO);
            }

            apiTtUserBlendErcashMapper.insert(blendErcash);

            log.info("【异步更新消费日志】成功。");

        }, customThreadPoolExecutor);

    }

    // 异步任务 广播开箱消息
    public void broadcastToBoxRoom(List<TtBoxRecords> boxRecords) {

        CompletableFuture.runAsync(() -> {

            // 数据按boxId排序
            Collections.sort(boxRecords, new Comparator<TtBoxRecords>() {
                @Override
                public int compare(TtBoxRecords o1, TtBoxRecords o2) {
                    return o1.getBoxId().compareTo(o2.getBoxId());
                }
            });

            // 缓冲区
            List<TtBoxRecords> cache = new ArrayList<>();

            Integer flagBoxId = -1;
            for (TtBoxRecords record : boxRecords) {

                if (cache.isEmpty()) {
                    // 缓冲区为空，直接加入
                    cache.add(record);
                    flagBoxId = record.getBoxId();
                    continue;
                } else {
                    // 不为空
                    if (record.getBoxId().equals(flagBoxId)) {
                        cache.add(record);
                    } else {
                        Integer bid = cache.get(0).getBoxId();
                        // 广播最新数据
                        WsBindBox.broadcastToBoxRoom(bid, WsResult.ok(SMsgKey.Blind_Box_Current_Data.name(), cache, "开盒子更新。"));
                        cache.clear();
                    }
                }
            }
            Integer bid = cache.get(0).getBoxId();
            // 广播最新数据
            WsBindBox.broadcastToBoxRoom(bid, WsResult.ok(SMsgKey.Blind_Box_Current_Data.name(), cache, "开盒子更新。"));

        }, customThreadPoolExecutor);
    }

}
