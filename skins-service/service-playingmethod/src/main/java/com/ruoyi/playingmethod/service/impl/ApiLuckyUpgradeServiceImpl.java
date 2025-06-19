package com.ruoyi.playingmethod.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.config.RedisConstants;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.*;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.*;
import com.ruoyi.domain.common.constant.LockKey.UpgradeLock;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.*;
import com.ruoyi.domain.task.DTO.pWelfareMQData;
import com.ruoyi.domain.vo.ApiLuckyOrnamentsDataVO;
import com.ruoyi.domain.vo.UpgradeResultDataVOA;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;
import com.ruoyi.playingmethod.mapper.ApiLuckyUpgradeMapper;
import com.ruoyi.playingmethod.mapper.ApiTtUserBlendErcashMapper;
import com.ruoyi.playingmethod.service.ApiLuckyUpgradeService;
import com.ruoyi.system.service.ISysConfigService;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApiLuckyUpgradeServiceImpl implements ApiLuckyUpgradeService {

    @Value("${mkcsgo.upgrade.defaultRequired}")
    private Integer defaultRequired;

    @Value("${mkcsgo.upgrade.anchorDefaultRequired}")
    private Integer anchorDefaultRequired;

    @Autowired
    private ThreadPoolExecutor customThreadPoolExecutor;

    @Autowired
    private ApiTtUserBlendErcashMapper apiTtUserBlendErcashMapper;

    private final TtUserService userService;
    private final ISysConfigService configService;
    private final ApiLuckyUpgradeMapper apiLuckyUpgradeMapper;
    private final TtUpgradeOrnamentsMapper upgradeOrnamentsMapper;
    private final TtUpgradeFailOrnamentsMapper upgradeFailOrnamentsMapper;
    private final TtUpgradeRecordMapper upgradeRecordMapper;
    private final TtOrnamentMapper ornamentsMapper;
    private final TtBoxRecordsMapper boxRecordsMapper;
    private final TtOrnamentsLevelMapper ornamentsLevelMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private RedisCache redisCache;

    public ApiLuckyUpgradeServiceImpl(TtUserService userService,
                                      ISysConfigService configService,
                                      ApiLuckyUpgradeMapper apiLuckyUpgradeMapper,
                                      TtUpgradeOrnamentsMapper upgradeOrnamentsMapper,
                                      TtUpgradeFailOrnamentsMapper upgradeFailOrnamentsMapper,
                                      TtUpgradeRecordMapper upgradeRecordMapper,
                                      TtOrnamentMapper ornamentsMapper,
                                      TtBoxRecordsMapper boxRecordsMapper,
                                      TtOrnamentsLevelMapper ornamentsLevelMapper) {
        this.userService = userService;
        this.configService = configService;
        this.apiLuckyUpgradeMapper = apiLuckyUpgradeMapper;
        this.upgradeOrnamentsMapper = upgradeOrnamentsMapper;
        this.upgradeFailOrnamentsMapper = upgradeFailOrnamentsMapper;
        this.upgradeRecordMapper = upgradeRecordMapper;
        this.ornamentsMapper = ornamentsMapper;
        this.boxRecordsMapper = boxRecordsMapper;
        this.ornamentsLevelMapper = ornamentsLevelMapper;
    }

    @Autowired
    private TtUpgradeOrnamentsService ttUpgradeOrnamentsService;

    @Autowired
    private TtUpgradeOrnamentsMapper ttUpgradeOrnamentsMapper;

    @Autowired
    private TtBoxRecordsService ttBoxRecordsService;

    @Autowired
    private TtUpgradeRecordService ttUpgradeRecordService;

    @Override
    public List<ApiLuckyOrnamentsDataVO> getOrnamentsList(ApiLuckyUpgradeBody apiLuckyUpgradeBody) {
        return apiLuckyUpgradeMapper.getOrnamentsList(apiLuckyUpgradeBody);
    }

    public R upgradeCheck(TtUser player, UpgradeBodyA param, TtUpgradeOrnaments upgradeOrnament) {

        if (player.getAccountAmount().add(player.getAccountCredits()).compareTo(param.getPrice()) <= 0) {
            return R.fail("余额不足");
        }

        if (!player.getUserType().equals(UserType.ANCHOR.getCode()) && !player.getUserType().equals(UserType.COMMON_USER.getCode())) {
            return R.fail("非法的用户类型");
        }

        BigDecimal price = upgradeOrnament.getOrnamentPrice().multiply(new BigDecimal(param.getProbability()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        if (param.getPrice().compareTo(price) != 0){
            return R.fail("无效幸运增量比例！");
        }

        return R.ok();
    }

    public R upgradeCheck2(TtUser player, UpgradeBodyA param, TtUpgradeOrnaments upgradeOrnament, TtBoxRecords userPackBoxRecord) {

        Assert.isTrue(userPackBoxRecord.getStatus() == TtboxRecordStatus.IN_PACKSACK_ON.getCode(), "该饰品已不存在背包中！");

        if (player.getAccountAmount().compareTo(param.getPrice()) < 0) {
            return R.fail("余额不足");
        }

        if (!player.getUserType().equals(UserType.ANCHOR.getCode()) && !player.getUserType().equals(UserType.COMMON_USER.getCode())) {
            return R.fail("非法的用户类型");
        }

        Long ornamentsId = upgradeOrnament.getOrnamentsId();
        TtOrnament ttOrnament = ornamentsMapper.selectOrnamentById(ornamentsId);
        if (ttOrnament.getUsePrice().compareTo(BigDecimal.ZERO) == 0){
            return R.fail("该物品已下架！");
        }


        //金币比例 + 投入饰品的价值比例 == 目标饰品价值 * 幸运增量
        BigDecimal totalPrice = userPackBoxRecord.getOrnamentsPrice().add(param.getPrice());

        //因为前端可能计算经度低，允许出现10的范围误差
        BigDecimal calcProbability = totalPrice.divide(ttOrnament.getUsePrice(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        int calcProbabilityInteger = calcProbability.intValue();


        if (param.getProbability() > calcProbabilityInteger - 5 && param.getProbability() < calcProbabilityInteger + 2){
            return R.ok();

        }else if (calcProbabilityInteger > param.getProbability()){
            return R.ok();

        }else {
            return R.fail("无效幸运增量比例！");
        }

//        return R.ok();
    }

    // @Override
    // @Transactional
    // public R upgrade(TtUser ttUser, UpgradeBodyA upgradeParam) {
    //
    //     // 检查
    //     R check = upgradeCheck(ttUser, upgradeParam);
    //     if (!check.getCode().equals(200)) return check;
    //
    //     Long ornamentId = upgradeParam.getOrnamentId();
    //
    //     // 1 获取已有的区间信息
    //     TtUpgradeOrnaments upgradeOrnament = new LambdaQueryChainWrapper<>(upgradeOrnamentsMapper)
    //             .eq(TtUpgradeOrnaments::getOrnamentsId, ornamentId)
    //             // .eq(TtUpgradeOrnaments::getId, id)
    //             .eq(TtUpgradeOrnaments::getStatus, "0")
    //             .one();
    //     if (ObjectUtil.isNull(upgradeOrnament)) {
    //         return R.fail("该饰品未在幸运升级开放。");
    //     }
    //
    //     // 2 尝试获取锁
    //     Boolean lock = false;
    //     for (int t = 0; t < 2; t++) {
    //
    //         lock = redisLock.tryLock(UpgradeLock.UPGRADE_LOCK.getLock() + ornamentId, 2L, 7L, TimeUnit.SECONDS);
    //
    //         if (!lock) {
    //
    //         } else {
    //             break;
    //         }
    //     }
    //
    //     if (!lock) return R.fail("系统繁忙，请稍后重试。");
    //
    //     // 3 扣款
    //     R<Map<String, BigDecimal>> mapR = upgradeAccounting(upgradeParam.getPrice(), ttUser);
    //     if (!mapR.getCode().equals(200)) {
    //         redisLock.unlock(UpgradeLock.UPGRADE_LOCK.getLock() + ornamentId);
    //         return mapR;
    //     }
    //
    //     // 4 计算游戏
    //     boolean isVictory;
    //     try {
    //
    //         isVictory = playing(ttUser, upgradeOrnament, upgradeParam);
    //
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return R.fail("系统繁忙，请稍后重试。");
    //     } finally {
    //         redisLock.unlock(UpgradeLock.UPGRADE_LOCK.getLock() + ornamentId);
    //     }
    //
    //     // 5 同步保存本次结果。
    //     TtUpgradeRecord upgradeRecord;  // 升级记录
    //     List<TtBoxRecords> boxRecords;  // 获得物品记录
    //     if (isVictory) {
    //
    //         // 胜
    //         List<SimpleOrnamentVO> prizeList = ornamentsMapper.simpleOrnamentInfo(Arrays.asList(ornamentId));
    //         SimpleOrnamentVO ornament = prizeList.get(0);
    //
    //         // 奖励集合 并统计奖励总价值
    //         // List<SimpleOrnamentVO> prizeList = Arrays.asList(ornament);
    //         BigDecimal prizeTotal = BigDecimal.ZERO;
    //         for (SimpleOrnamentVO vo : prizeList) prizeTotal = prizeTotal.add(vo.getOrnamentPrice());
    //
    //         // 构造升级记录
    //         upgradeRecord = TtUpgradeRecord.builder()
    //                 .userId(ttUser.getUserId())
    //                 .userType(ttUser.getUserType())
    //                 .nickName(ObjectUtil.isNotEmpty(ttUser.getNickName()) ? ttUser.getNickName() : ttUser.getUserName())
    //                 .isVictory(isVictory)
    //
    //                 .amountConsumed(upgradeParam.getPrice())
    //                 .probability(upgradeParam.getProbability())
    //
    //                 .targetUpgradeId(upgradeOrnament.getId())
    //                 .targetOrnamentId(ornamentId)
    //                 .targetOrnamentPrice(ornament.getOrnamentPrice())
    //
    //                 .gainOrnamentList(JSONUtil.toJsonStr(prizeList))
    //                 .gainOrnamentsPrice(prizeTotal)
    //
    //                 .openTime(new Date())
    //                 .build();
    //
    //         // 构造获得物品记录
    //         boxRecords = Arrays.asList(TtBoxRecords.builder()
    //                 .source(TtboxRecordSource.UPGRADE.getCode())
    //                 .ornamentId(ornamentId)
    //                 .ornamentName(ornament.getOrnamentName())
    //                 .userId(ttUser.getUserId())
    //                 .holderUserId(ttUser.getUserId())
    //                 .imageUrl(ornament.getOrnamentImgUrl())
    //                 .ornamentsPrice(ornament.getOrnamentPrice())
    //                 .status(TtboxRecordStatus.IN_PACKSACK_ON.getCode())
    //                 .marketHashName(ornament.getOrnamentHashName())
    //                 .createTime(new Date())
    //                 .updateTime(new Date())
    //                 .build());
    //
    //     } else {
    //
    //         // 负
    //         // 奖励集合 并统计奖励总价值
    //         List<SimpleOrnamentVO> failPrize = upgradeFailOrnamentsMapper.ornamentInfoByUpgradeId(upgradeOrnament.getId());
    //
    //         BigDecimal prizeTotal = BigDecimal.ZERO;
    //         if (!failPrize.isEmpty()) {
    //             for (SimpleOrnamentVO item : failPrize) {
    //                 prizeTotal = prizeTotal.add(item.getOrnamentPrice().multiply(new BigDecimal(item.getOrnamentNumber())));
    //             }
    //         }
    //
    //         // 构造升级记录
    //         upgradeRecord = TtUpgradeRecord.builder()
    //                 .userId(ttUser.getUserId())
    //                 .userType(ttUser.getUserType())
    //                 .nickName(ObjectUtil.isNotEmpty(ttUser.getNickName()) ? ttUser.getNickName() : ttUser.getUserName())
    //
    //                 .amountConsumed(upgradeParam.getPrice())
    //                 .probability(upgradeParam.getProbability())
    //
    //                 .isVictory(isVictory)
    //
    //                 .targetUpgradeId(upgradeOrnament.getId())
    //                 .targetOrnamentId(upgradeOrnament.getOrnamentsId())
    //                 .targetOrnamentPrice(upgradeOrnament.getOrnamentPrice())
    //
    //                 .gainOrnamentList(failPrize.isEmpty() ? "" : JSONUtil.toJsonStr(failPrize))
    //                 .gainOrnamentsPrice(prizeTotal)
    //
    //                 .openTime(new Date())
    //                 .build();
    //
    //         // 构造获得物品记录
    //         boxRecords = failPrize.stream().map(item -> {
    //             return TtBoxRecords.builder()
    //                     .source(TtboxRecordSource.UPGRADE.getCode())
    //                     .ornamentId(item.getOrnamentId())
    //                     .ornamentName(item.getOrnamentName())
    //                     .userId(ttUser.getUserId())
    //                     .holderUserId(ttUser.getUserId())
    //                     .imageUrl(item.getOrnamentImgUrl())
    //                     .ornamentsPrice(item.getOrnamentPrice())
    //                     .status(TtboxRecordStatus.IN_PACKSACK_ON.getCode())
    //                     .marketHashName(item.getOrnamentHashName())
    //                     .createTime(new Date())
    //                     .updateTime(new Date())
    //                     .build();
    //         }).collect(Collectors.toList());
    //
    //     }
    //
    //     // 游戏记录
    //     ttUpgradeRecordService.save(upgradeRecord);
    //     ttBoxRecordsService.saveBatch(boxRecords);
    //
    //     // 异步任务统计业务数据
    //     Map<String, BigDecimal> moneyMap = mapR.getData();
    //     asynTask(ttUser, moneyMap);
    //
    //     return R.ok(upgradeRecord);
    //
    // }

    @Override
    public R upgrade(TtUser ttUser, UpgradeBodyA upgradeParam) {
        return upgradeProcess(ttUser, upgradeParam, false);
    }


    /**
     *
     * @param ttUser
     * @param upgradeParam
     * @param isConsumeOrnament 是否消耗饰品来升级？旧版本不消耗饰品。新玩法消耗饰品升级
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public R upgradeProcess(TtUser ttUser, UpgradeBodyA upgradeParam, Boolean isConsumeOrnament) {

        TtUpgradeOrnaments upgradeOrnament = new LambdaQueryChainWrapper<>(upgradeOrnamentsMapper)
                .eq(TtUpgradeOrnaments::getId, upgradeParam.getUpgradeOrnamentId())
                .eq(TtUpgradeOrnaments::getStatus, "0")
                .one();
        if (ObjectUtil.isNull(upgradeOrnament)) {
            return R.fail("该饰品未在幸运升级开放。");
        }

        TtBoxRecords userPackBoxRecord = null;

        // 检查
        if (isConsumeOrnament){
            //新玩法校验

            userPackBoxRecord = ttBoxRecordsService.getById(upgradeParam.getPackageOrnamentId());
            R check = upgradeCheck2(ttUser, upgradeParam, upgradeOrnament, userPackBoxRecord);
            if (!check.getCode().equals(200)) return check;

            //设置背包饰品已分解
            userPackBoxRecord.setStatus(TtboxRecordStatus.RESOLVE.getCode());
            ttBoxRecordsService.updateById(userPackBoxRecord);

        }else {
            //旧玩法
            R check = upgradeCheck(ttUser, upgradeParam, upgradeOrnament);
            if (!check.getCode().equals(200)) return check;
        }

        // lock_key
        Long upgradeOrnamentId = upgradeParam.getUpgradeOrnamentId();
        String userType = ttUser.getUserType();
        String upgradeLock = UpgradeLock.UPGRADE_LOCK.getLock() + upgradeOrnamentId + ":" + userType;

        // 2 尝试获取锁
        Boolean lock = false;
        for (int t = 0; t < 2; t++) {

            lock = redisLock.tryLock(upgradeLock, 2L, 7L, TimeUnit.SECONDS);

            if (!lock) {

            } else {
                break;
            }
        }

        if (!lock) return R.fail("系统繁忙，请稍后重试。");

        // 3 扣款
        R<Map<String, BigDecimal>> mapR = null;
        if (isConsumeOrnament){
            mapR = upgradeAccounting2(upgradeParam.getPrice(), ttUser);

        }else {
            mapR = upgradeAccounting(upgradeParam.getPrice(), ttUser);

        }
        if (!mapR.getCode().equals(200)) {
            redisLock.unlock(upgradeLock);
            return mapR;
        }

        // 4 计算游戏
        boolean isVictory;
        try {

            // 升级核心
            isVictory = playing(ttUser, upgradeOrnament, upgradeParam);

        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("系统繁忙，请稍后重试。");
        } finally {
            redisLock.unlock(upgradeLock);
        }

        // 5 同步保存本次结果。
        TtUpgradeRecord upgradeRecord;  // 升级记录
        List<TtBoxRecords> boxRecords;  // 获得物品记录
        if (isVictory) {

            Long ornamentId = upgradeOrnament.getOrnamentsId();

            // 胜
            List<SimpleOrnamentVO> prizeList = ornamentsMapper.simpleOrnamentInfo(Arrays.asList(ornamentId));
            SimpleOrnamentVO ornament = prizeList.get(0);

            // 奖励集合 并统计奖励总价值
            // List<SimpleOrnamentVO> prizeList = Arrays.asList(ornament);
            BigDecimal prizeTotal = BigDecimal.ZERO;
            for (SimpleOrnamentVO vo : prizeList) prizeTotal = prizeTotal.add(vo.getOrnamentPrice());

            // 构造升级记录
            upgradeRecord = TtUpgradeRecord.builder()
                    .userId(ttUser.getUserId())
                    .userType(ttUser.getUserType())
                    .nickName(ObjectUtil.isNotEmpty(ttUser.getNickName()) ? ttUser.getNickName() : ttUser.getUserName())
                    .isVictory(isVictory)

                    .amountConsumed(upgradeParam.getPrice())
                    .probability(upgradeParam.getProbability())

                    .targetUpgradeId(upgradeOrnament.getId())
                    .targetOrnamentId(ornamentId)
                    .targetOrnamentPrice(ornament.getOrnamentPrice())

                    .gainOrnamentList(JSONUtil.toJsonStr(prizeList))
                    .gainOrnamentsPrice(prizeTotal)

                    .openTime(new Date())
                    .build();

            // 构造获得物品记录
            boxRecords = Arrays.asList(TtBoxRecords.builder()
                    .source(TtboxRecordSource.UPGRADE.getCode())
                    .ornamentId(ornamentId)
                    .ornamentName(ornament.getOrnamentName())
                    .userId(ttUser.getUserId())
                    .holderUserId(ttUser.getUserId())
                    .imageUrl(ornament.getOrnamentImgUrl())
                    .ornamentsPrice(ornament.getOrnamentPrice())
                    .status(TtboxRecordStatus.IN_PACKSACK_ON.getCode())
                    .marketHashName(ornament.getOrnamentHashName())
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build());

        } else {

            // 负
            // 奖励集合 并统计奖励总价值
            List<SimpleOrnamentVO> failPrize = upgradeFailOrnamentsMapper.ornamentInfoByUpgradeId(upgradeOrnament.getId());

            BigDecimal prizeTotal = BigDecimal.ZERO;
            if (!failPrize.isEmpty()) {
                for (SimpleOrnamentVO item : failPrize) {
                    prizeTotal = prizeTotal.add(item.getOrnamentPrice().multiply(new BigDecimal(item.getOrnamentNumber())));
                }
            }

            // 构造升级记录
            upgradeRecord = TtUpgradeRecord.builder()
                    .userId(ttUser.getUserId())
                    .userType(ttUser.getUserType())
                    .nickName(ObjectUtil.isNotEmpty(ttUser.getNickName()) ? ttUser.getNickName() : ttUser.getUserName())

                    .amountConsumed(upgradeParam.getPrice())
                    .probability(upgradeParam.getProbability())

                    .isVictory(isVictory)

                    .targetUpgradeId(upgradeOrnament.getId())
                    .targetOrnamentId(upgradeOrnament.getOrnamentsId())
                    .targetOrnamentPrice(upgradeOrnament.getOrnamentPrice())

                    .gainOrnamentList(failPrize.isEmpty() ? "" : JSONUtil.toJsonStr(failPrize))
                    .gainOrnamentsPrice(prizeTotal)

                    .openTime(new Date())
                    .build();

            // 构造获得物品记录
            boxRecords = failPrize.stream().map(item -> {
                return TtBoxRecords.builder()
                        .source(TtboxRecordSource.UPGRADE.getCode())
                        .ornamentId(item.getOrnamentId())
                        .ornamentName(item.getOrnamentName())
                        .userId(ttUser.getUserId())
                        .holderUserId(ttUser.getUserId())
                        .imageUrl(item.getOrnamentImgUrl())
                        .ornamentsPrice(item.getOrnamentPrice())
                        .status(TtboxRecordStatus.IN_PACKSACK_ON.getCode())
                        .marketHashName(item.getOrnamentHashName())
                        .createTime(new Date())
                        .updateTime(new Date())
                        .build();
            }).collect(Collectors.toList());

        }

        // 游戏记录
        ttUpgradeRecordService.save(upgradeRecord);
        ttBoxRecordsService.saveBatch(boxRecords);

        // 异步任务统计业务数据
        Map<String, BigDecimal> moneyMap = mapR.getData();
        asynTask(ttUser, moneyMap, upgradeOrnament, upgradeParam);

        return R.ok(upgradeRecord);

    }

    @Override
    public R upgrade2(TtUser ttUser, UpgradeBodyA upgradeParam) {
        return upgradeProcess(ttUser, upgradeParam, true);
    }

    public Boolean playing(TtUser ttUser, TtUpgradeOrnaments upgradeOrnament, UpgradeBodyA upgradeParam) {

        Long upgradeOrnamentId = upgradeOrnament.getId();
        String userType = ttUser.getUserType();

        String rangeFixedKey = RedisConstants.UPGRADE_RANGE_FIXED + upgradeOrnamentId + ":" + userType;
        String rangeFloatKey = RedisConstants.UPGRADE_RANGE + upgradeOrnamentId + ":" + userType;

        // 读redis的固定概率 [x,y] x进度，y空间大小
        Object rangeFixedObj = redisCache.getCacheObject(rangeFixedKey);
        String rangeFixedStr = JSONUtil.toJsonStr(rangeFixedObj);
        Integer[] rangeFixed = JSONUtil.parseArray(rangeFixedStr).toArray(new Integer[2]);
        // 读redis的饰品概率区间 [begin,end]
        Object rangeFloatObj = redisCache.getCacheObject(rangeFloatKey);
        String rangeFloatStr = JSONUtil.toJsonStr(rangeFloatObj);
        Integer[] rangeFloat = JSONUtil.parseArray(rangeFloatStr).toArray(new Integer[2]);

        if (ObjectUtil.isNotEmpty(rangeFixed)) {
            // 抽奖
            return doUpgrade(
                    ttUser,
                    upgradeOrnament,
                    rangeFixed,
                    upgradeParam,
                    rangeFixedKey,
                    rangeFloatKey,
                    rangeFloat);
        }

        // 没有，使用redis的概率区间新建，
        if (ObjectUtil.isNotEmpty(rangeFloat)) {

            // 创建固定概率，
            Integer[] newRangeFixed = new Integer[]{0, null};
            for (int i = 0; i < 5; i++) {
                Random random = new Random();
                int r = random.nextInt(rangeFloat[1] - rangeFloat[0]) + rangeFloat[0];
                newRangeFixed[1] = r;
            }

            // 抽奖
            return doUpgrade(
                    ttUser,
                    upgradeOrnament,
                    newRangeFixed,
                    upgradeParam,
                    rangeFixedKey,
                    rangeFloatKey,
                    rangeFloat);
        }

        // 也没有概率区间，用mysql的数据，创建固定概率，抽奖
        if (userType.equals(UserType.ANCHOR.getCode())) {
            String anchorLuckSection = upgradeOrnament.getAnchorLuckSection();
            List<Integer> list = JSONUtil.toList(anchorLuckSection, Integer.class);
            rangeFloat = list.toArray(new Integer[2]);
        } else if (userType.equals(UserType.COMMON_USER.getCode())) {
            String luckSection = upgradeOrnament.getLuckSection();
            List<Integer> list = JSONUtil.toList(luckSection, Integer.class);
            rangeFloat = list.toArray(new Integer[2]);
        }

        rangeFixed = createRangeFixed(rangeFloat, 0);

        // 缓存固定概率
        // redisCache.setCacheList(rangeFixedKey, Arrays.asList(rangeFixed));
        // redisCache.setCacheObject(rangeFixedKey, Arrays.asList(rangeFixed));
        // 缓存概率区间
        redisCache.setCacheObject(rangeFloatKey, rangeFloat);

        // 抽奖
        return doUpgrade(
                ttUser,
                upgradeOrnament,
                rangeFixed,
                upgradeParam,
                rangeFixedKey,
                rangeFloatKey,
                rangeFloat);

    }

    /**
     * @param rangeFixed    本次的固定概率
     * @param param         http请求参数
     * @param rangeFixedKey redis固定概率key
     * @param rangeFloat    升级饰品的概率区间
     * @return
     */
    public Boolean doUpgrade(TtUser ttUser,
                             TtUpgradeOrnaments upgradeOrnament,
                             Integer[] rangeFixed,
                             UpgradeBodyA param,
                             String rangeFixedKey,
                             String rangeFloatKey,
                             Integer[] rangeFloat) {

        // 计算进度
        Integer currentV = rangeFixed[0];
        Integer fullV = rangeFixed[1];

        int sub = fullV - (param.getProbability() + currentV);

        if (sub > 0) {
            // 失败，记录进度
            int i = param.getProbability() + currentV;
            rangeFixed[0] = i;
            // redisCache.setCacheList(rangeFixedKey, Arrays.asList(rangeFixed));
            redisCache.setCacheObject(rangeFixedKey, rangeFixed);
            return false;
        }

        // 成功
        // 如果没有概率区间，新建
        if (ObjectUtil.isNotEmpty(rangeFloat)) {

            if (ttUser.getUserType().equals(UserType.ANCHOR.getCode())) {
                String anchorLuckSection = upgradeOrnament.getAnchorLuckSection();
                List<Integer> list = JSONUtil.toList(anchorLuckSection, Integer.class);
                rangeFloat = list.toArray(new Integer[2]);
            } else if (ttUser.getUserType().equals(UserType.COMMON_USER.getCode())) {
                String luckSection = upgradeOrnament.getLuckSection();
                List<Integer> list = JSONUtil.toList(luckSection, Integer.class);
                rangeFloat = list.toArray(new Integer[2]);
            }

            // 缓存概率区间
            redisCache.setCacheObject(rangeFloatKey, rangeFloat);

            // rangeFixed = createRangeFixed(rangeFloat,0);

        }

        // 成功,随机创建新的固定概率
        Integer[] newRangeFixed = createRangeFixed(rangeFloat, Math.abs(sub));

        // 保存新的固定概率
        // redisCache.setCacheList(rangeFixedKey, Arrays.asList(newRangeFixed));
        redisCache.setCacheObject(rangeFixedKey, newRangeFixed);
        return true;

    }


    public Integer[] createRangeFixed(Integer[] rangeFloat, Integer initVal) {

        // 创建固定概率，
        Integer[] newRangeFixed = new Integer[2];
        newRangeFixed[0] = initVal;
        int r = -1;
        for (int i = 0; i < 5; i++) {
            Random random = new Random();
            int f1 = rangeFloat[1] - rangeFloat[0];
            if (f1 < 1) f1 = 1; //确保nexInt()中的值不能小于0
            r = random.nextInt(f1) + rangeFloat[0];
        }
        newRangeFixed[1] = r + 100; //随机加200，确保至少一轮

        return newRangeFixed;
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
        map.put("total", consumption);

        userService.update(userUpdate);

        return R.ok(map);
    }

    // 扣款(只扣金币)
    public R<Map<String, BigDecimal>> upgradeAccounting2(BigDecimal consumption, TtUser player) {

        // 再次检查余额
        player = userService.getById(player.getUserId());
        if (player.getAccountAmount().compareTo(consumption) < 0) {
            return R.fail("余额不足");
        }

        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate.eq(TtUser::getUserId, player.getUserId());

        Map<String, BigDecimal> map;

        userUpdate.set(TtUser::getAccountAmount, player.getAccountAmount().subtract(consumption));
        map = MapUtil.builder("Amount", consumption).map();

        map.put("total", consumption);

        userService.update(userUpdate);

        return R.ok(map);
    }

    // 异步统计业务
    private void asynTask(TtUser ttUser, Map<String, BigDecimal> moneyMap, TtUpgradeOrnaments upgradeOrnament, UpgradeBodyA upgradeParam) {

        // 异步写日志和推广福利
        CompletableFuture.runAsync(() -> {

            // 保存升级饰品的进度
            // LambdaUpdateWrapper<TtUpgradeOrnaments> uoWrapper = new LambdaUpdateWrapper<>();
            // uoWrapper
            //         .eq(TtUpgradeOrnaments::getId, upgradeOrnament.getId())
            //         .eq(TtUpgradeOrnaments::getStatus, 0)
            //         .set(TtUpgradeOrnaments::getUpdateTime, new Date())
            //         .setSql("total_input = total_input + " + upgradeParam.getPrice());
            // boolean update = ttUpgradeOrnamentsService.update(uoWrapper);
            // System.out.println(update);
            // new LambdaUpdateChainWrapper<>(ttUpgradeOrnamentsMapper)
            //         .eq(TtUpgradeOrnaments::getId, upgradeOrnament.getId())
            //         .eq(TtUpgradeOrnaments::getStatus, 0)
            //         .set(TtUpgradeOrnaments::getUpdateTime, new Date())
            //         .setSql("total_input = total_input + " + upgradeParam.getPrice())
            //         .update();

            BigDecimal Amount = moneyMap.get("Amount");
            BigDecimal Credits = moneyMap.get("Credits");
            BigDecimal total = moneyMap.get("total");


            TtUser userById = userService.getById(ttUser.getUserId());

            // 综合消费日志
            TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                    .userId(userById.getUserId())

                    .amount(ObjectUtil.isNotEmpty(Amount) ? Amount.negate() : null)
                    .finalAmount(ObjectUtil.isNotEmpty(Amount) ? userById.getAccountAmount().subtract(Amount):null)

                    .credits(ObjectUtil.isNotEmpty(Credits) ? Credits.negate() : null)
                    .finalCredits(ObjectUtil.isNotEmpty(Credits) ? userById.getAccountCredits().subtract(Credits):null)

                    .total(total.negate())  //收支合计

                    .type(TtAccountRecordType.OUTPUT.getCode())
                    .source(TtAccountRecordSource.GAME_TYPE_04.getCode())
                    .remark(TtAccountRecordSource.GAME_TYPE_04.getMsg())

                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()))
                    .build();

            if (blendErcash.getFinalAmount() != null && BigDecimal.ZERO.compareTo(blendErcash.getFinalAmount()) > 0){
                blendErcash.setFinalAmount(BigDecimal.ZERO);
            }
            if (blendErcash.getFinalCredits() != null && BigDecimal.ZERO.compareTo(blendErcash.getFinalCredits()) > 0){
                blendErcash.setFinalCredits(BigDecimal.ZERO);
            }

            int insert = apiTtUserBlendErcashMapper.insert(blendErcash);

        }, customThreadPoolExecutor);
    }

    // 统计推广福利
    public void pWelfare(pWelfareMQData msgDate) {

        TtUser user = msgDate.getUser();
        TtUser parent = userService.getById(user.getParentId());

        if (ObjectUtil.isNotEmpty(parent)) {
            BigDecimal credits = null;
            if ("01".equals(parent.getUserType())) {
                credits = msgDate.getAccount().multiply(new BigDecimal("0.045"));
            } else {
                credits = msgDate.getAccount().multiply(new BigDecimal("0.01"));
            }
            LambdaUpdateWrapper<TtUser> wrapper = new LambdaUpdateWrapper<>();
            wrapper
                    .eq(TtUser::getUserId, parent.getUserId())
                    .set(TtUser::getAccountCredits, parent.getAccountCredits().add(credits));
            // .set(TtUser::getAccountAmount, parent.getAccountAmount().add(credits));
            userService.update(wrapper);
            // userService.insertUserCreditsRecords(parent.getUserId(),
            //         TtAccountRecordType.INPUT,
            //         TtAccountRecordSource.P_WELFARE,
            //         credits,
            //         parent.getAccountCredits().add(credits),
            //         user.getUserId(),
            //         user.getNickName(),
            //         msgDate.getAccount());
            userService.insertUserAmountRecords(parent.getUserId(),
                    TtAccountRecordType.INPUT,
                    TtAccountRecordSource.P_WELFARE,
                    credits,
                    parent.getAccountCredits().add(credits),
                    user.getUserId(),
                    user.getNickName(),
                    msgDate.getAccount());

            log.info("【统计推广福利】成功。");
        }

    }


    private UpgradeResultDataVOA insertRecordData(TtUser ttUser, BigDecimal price, TtOrnament targetOrnaments, TtOrnament gainOrnaments,
                                                  TtUpgradeOrnaments ttUpgradeOrnaments, TtUpgradeFailOrnaments ttUpgradeFailOrnaments, boolean isFlag) {
        TtUpgradeRecord ttUpgradeRecord = TtUpgradeRecord.builder().build();
        ttUpgradeRecord.setUserId(ttUser.getUserId());
        ttUpgradeRecord.setAmountConsumed(price);
        ttUpgradeRecord.setTargetUpgradeId(ttUpgradeOrnaments.getId());
        ttUpgradeRecord.setTargetOrnamentId(ttUpgradeOrnaments.getOrnamentsId());
        ttUpgradeRecord.setTargetOrnamentPrice(targetOrnaments.getUsePrice());
        ttUpgradeRecord.setGainOrnamentsPrice(gainOrnaments.getUsePrice());
        ttUpgradeRecord.setOpenTime(DateUtils.getNowDate());
        TtBoxRecords boxRecords = TtBoxRecords.builder().build();
        boxRecords.setUserId(ttUser.getUserId());
        boxRecords.setOrnamentsPrice(gainOrnaments.getUsePrice());
        boxRecords.setCreateTime(DateUtils.getNowDate());
        boxRecords.setSource(TtboxRecordSource.UPGRADE.getCode());
        boxRecords.setHolderUserId(ttUser.getUserId());
        if (!isFlag) {
            // ttUpgradeRecord.setGainUpgradeFailId(ttUpgradeFailOrnaments.getId());
            // ttUpgradeRecord.setGainOrnamentsId(ttUpgradeFailOrnaments.getOrnamentsId());
            boxRecords.setOrnamentId(ttUpgradeFailOrnaments.getOrnamentId());
            boxRecords.setOrnamentsLevelId(ttUpgradeFailOrnaments.getOrnamentLevelId());
        } else {
            // ttUpgradeRecord.setGainUpgradeFailId(0);
            // ttUpgradeRecord.setGainOrnamentsId(ttUpgradeOrnaments.getOrnamentsId());
            boxRecords.setOrnamentId(ttUpgradeOrnaments.getOrnamentsId());
            boxRecords.setOrnamentsLevelId(ttUpgradeOrnaments.getOrnamentsLevelId());
        }
        upgradeRecordMapper.insert(ttUpgradeRecord);
        if (boxRecordsMapper.insert(boxRecords) > 0) {
            UpgradeResultDataVOA result = UpgradeResultDataVOA.builder().build();
            result.setId(boxRecords.getId());
            result.setOrnamentsPrice(gainOrnaments.getUsePrice());
            result.setItemName(gainOrnaments.getName());
            result.setShortName(gainOrnaments.getShortName());
            result.setImageUrl(gainOrnaments.getImageUrl());
            result.setExteriorName(gainOrnaments.getExteriorName());
            TtOrnamentsLevel ornamentsLevel;
            if (!isFlag) ornamentsLevel = ornamentsLevelMapper.selectById(ttUpgradeFailOrnaments.getOrnamentLevelId());
            else ornamentsLevel = ornamentsLevelMapper.selectById(ttUpgradeOrnaments.getOrnamentsLevelId());
            if (StringUtils.isNotNull(ornamentsLevel)) result.setLevelImg(ornamentsLevel.getLevelImg());
            return result;
        }
        return null;
    }
}
