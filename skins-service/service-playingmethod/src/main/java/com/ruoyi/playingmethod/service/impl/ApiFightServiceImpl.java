package com.ruoyi.playingmethod.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.core.type.TypeReference;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.*;
import com.ruoyi.admin.util.core.fight.LotteryMachine;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.common.constant.TtboxRecordSource;
import com.ruoyi.domain.dto.fight.FightDetailParam;
import com.ruoyi.domain.dto.fight.FightOnMyOwnParam;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.entity.fight.FightSeat;
import com.ruoyi.domain.vo.fight.AudienceVO;
import com.ruoyi.domain.vo.fight.FightBoxVO;
import com.ruoyi.domain.vo.fight.FightResultVO;
import com.ruoyi.domain.other.*;
import com.ruoyi.domain.vo.*;
import com.ruoyi.domain.vo.fight.TtFightVO;
import com.ruoyi.framework.websocket.WebSocketUsers;
import com.ruoyi.framework.websocket.pojo.ResultData;
import com.ruoyi.playingmethod.mapper.ApiBindBoxMapper;
import com.ruoyi.playingmethod.mapper.ApiFightMapper;
import com.ruoyi.playingmethod.mapper.ApiTtUserBlendErcashMapper;
import com.ruoyi.playingmethod.model.vo.ApiFightRankingVO;
import com.ruoyi.playingmethod.service.ApiBindBoxService;
import com.ruoyi.playingmethod.service.ApiFightService;
import com.ruoyi.playingmethod.service.ApiFightUserService;
import com.ruoyi.playingmethod.websocket.WsFightHall;
import com.ruoyi.playingmethod.websocket.WsFightRoom;
import com.ruoyi.playingmethod.websocket.util.WsResult;
import com.ruoyi.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.ruoyi.admin.config.RedisConstants.*;

import static com.ruoyi.admin.config.RedisConstants.JOIN_FIGHT_SEAT_READY_LOCK;
import static com.ruoyi.domain.common.constant.TtboxRecordStatus.*;
import static com.ruoyi.playingmethod.websocket.constant.SMsgKey.*;

@Service
@Slf4j
public class ApiFightServiceImpl extends ServiceImpl<TtFightMapper, TtFight> implements ApiFightService {


    @Value("${mkcsgo.fight.roundTime}")
    private final Integer fightRoundTime = null;


    @Autowired
    private TtBoxRecordsMapper boxRecordsMapper;

    @Autowired
    private TtBoxOrnamentsMapper boxOrnamentsMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ApiBindBoxMapper bindBoxMapper;

    @Autowired
    private LotteryMachine lotteryMachine;

    @Autowired
    private TtOrnamentMapper ttOrnamentMapper;

    @Autowired
    private TtOrnamentsYYMapper ttOrnamentsYYMapper;

    @Autowired
    private ApiTtUserBlendErcashMapper apiTtUserBlendErcashMapper;

    @Autowired
    private ApiFightUserService fightUserService;

    @Autowired
    private ISysConfigService configService;

    // @Autowired
    // private ApiBoxOrnamentsService boxOrnamentsService;

    private final TtBoxMapper boxMapper;
    private final TtBoxRecordsService ttBoxRecordsService;
    @Autowired
    private TtBoxRecordsMapper ttBoxRecordsMapper;
    private final TtUserService userService;

    private final RabbitTemplate rabbitTemplate;
    private final TtBoxRecordsService boxRecordsService;
    private final TtFightMapper fightMapper;
    private final ApiFightMapper apiFightMapper;
    private final TtFightUserMapper fightUserMapper;
    private final ApiBindBoxService apiBindBoxService;
    private final TtFightResultMapper fightResultMapper;
    private final RedisLock redisLock;
    private final ThreadPoolExecutor customThreadPoolExecutor;
    private final TtOrnamentService ttOrnamentsZBTService;


    private final TtOrnamentsLevelService ttOrnamentsLevelService;

    public ApiFightServiceImpl(TtBoxMapper boxMapper,
                               TtUserService userService,
                               TtBoxRecordsService ttBoxRecordsService,
                               TtBoxRecordsService boxRecordsService,
                               TtFightMapper fightMapper,
                               RabbitTemplate rabbitTemplate,
                               TtOrnamentsLevelService ttOrnamentsLevelService,
                               TtOrnamentService ttOrnamentsZBTService,
                               ApiFightMapper apiFightMapper,
                               TtFightUserMapper fightUserMapper,
                               ApiBindBoxService apiBindBoxService,
                               TtFightResultMapper fightResultMapper,
                               RedisLock redisLock,
                               ThreadPoolExecutor customThreadPoolExecutor) {
        this.boxMapper = boxMapper;
        this.ttOrnamentsLevelService = ttOrnamentsLevelService;
        this.userService = userService;
        this.ttBoxRecordsService = ttBoxRecordsService;
        this.boxRecordsService = boxRecordsService;
        this.fightMapper = fightMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.ttOrnamentsZBTService = ttOrnamentsZBTService;
        this.apiFightMapper = apiFightMapper;
        this.fightUserMapper = fightUserMapper;
        this.apiBindBoxService = apiBindBoxService;
        this.fightResultMapper = fightResultMapper;
        this.redisLock = redisLock;
        this.customThreadPoolExecutor = customThreadPoolExecutor;
    }


    private AjaxResult joinFightRoomCheck(Integer userId, Integer fightId) {

        TtUser player = userService.getById(userId);
        TtFight ttFight = new LambdaQueryChainWrapper<>(fightMapper)
                .eq(TtFight::getId, fightId)
                .eq(TtFight::getStatus, "0")
                .one();
        List<TtFightUser> roomPlayers = new LambdaQueryChainWrapper<>(fightUserMapper)
                .eq(TtFightUser::getFightId, fightId)
                .eq(TtFightUser::getUserId, player.getUserId())
                .list();

        if (player.getAccountAmount().compareTo(ttFight.getBoxPriceTotal()) < 0)
            return AjaxResult.error("加入房间失败，您的账户余额不足！");
        if (ObjectUtils.isEmpty(ttFight)) return AjaxResult.error("加入对战失败，该房间已结束！");
        if (roomPlayers.size() >= 1) return AjaxResult.error("您已成功加入该房间，请勿重复加入！");

        return AjaxResult.success();
    }


    @Override
    @Transactional
    public R<Object> createFight(CreateFightBody createFightParam, TtUser player) {

        // log.info("初始化对局---------------------------------------------------------------------- 1");
        // TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        // 检查数据
        R fightCheck = createFightCheck(createFightParam, player);
        if (!fightCheck.getCode().equals(200)) return fightCheck;

        // 初始化对局
        TtFight fight = (TtFight) fightCheck.getData();
        fight.setModel(createFightParam.getModel());
        // 直接加入
        List<FightSeat> seats = fight.getSeatList();

        FightSeat ready = seats.remove(0).sitDown(player.getUserId()).ready();
        // 补充用户信息
        ready.setAvatar(player.getAvatar());
        ready.setNickName(player.getNickName());

        seats.add(ready);

        Collections.reverse(seats);

        fight.setSeats(JSONUtil.toJsonStr(seats));

        save(fight);

        // 同步保存参与人员信息
        TtFightUser fightUser = TtFightUser.builder()
                .fightId(fight.getId())
                .userId(player.getUserId())
                .build();

        fightUserService.save(fightUser);

        // 账户结算
        readyFightAccounting(fight, player);


        // ws广播最新对局（异步）
        CompletableFuture.runAsync(() -> {
            WsFightHall.broadcast(WsResult.ok(ALL_FIGHT_ROOM.name(), Arrays.asList(fight), "对战房间最新信息"));
            log.info("用户{}【创建房间】{}", player.getUserId(), fight.getId());
        }, customThreadPoolExecutor);

        return R.ok(fight, "创建成功,可以建立 fightRoom ws");
    }

    public R createFightCheck(CreateFightBody createFightParam, TtUser player) {

        if (ObjectUtil.isEmpty(createFightParam.getModel())) {
            return R.fail("模式不能为空");
        }
        if (!createFightParam.getModel().equals("0") && !createFightParam.getModel().equals("1")) {
            return R.fail("非法的模式类型");
        }

        Map<Integer, Integer> boxIdAndNumber = createFightParam.getBoxIdAndNumber();

        // 本局箱子总数
        Integer boxNum = 0;

        LinkedHashMap<String, FightBoxVO> boxMap = new LinkedHashMap<>();
        // 计算整局游戏箱子总价
        BigDecimal boxTotalPrice = BigDecimal.ZERO;

        for (Integer boxId : boxIdAndNumber.keySet()) {

            TtBox box = new LambdaQueryChainWrapper<>(boxMapper)
                    .eq(TtBox::getBoxId, boxId)
                    .eq(TtBox::getIsFight, "0")
                    .eq(TtBox::getStatus, "0")
                    .one();

            if (ObjectUtils.isEmpty(box)) {
                return R.fail("不存在的箱子！");
            }

            boxMap.put(String.valueOf(boxId), new FightBoxVO(boxId, boxIdAndNumber.get(boxId), box.getBoxImg01(), box.getBoxImg02(), box.getPrice()));

            BigDecimal multiply = BigDecimal.valueOf(boxIdAndNumber.get(boxId)).multiply(box.getPrice());
            boxTotalPrice = boxTotalPrice.add(multiply);
            boxNum = boxNum + boxIdAndNumber.get(boxId);
        }

        // 回合数最多为15回合
        if (boxNum > 15 || boxNum <= 0) return R.fail("非法参数值 boxNum【" + boxNum + "】，宝箱数量范围1-15个！");

        if (player.getAccountAmount().add(player.getAccountCredits()).compareTo(boxTotalPrice) < 0)
            return R.fail("您的账户余额不足！");

        // 初始化座位信息
        ArrayList<FightSeat> seats = new ArrayList<>();

        for (int i = 0; i < createFightParam.getPlayerNumber(); i++) {
            FightSeat seat = FightSeat.builder()
                    .code(i)
                    .status(0)
                    .awardTotalPrices(BigDecimal.ZERO)
                    .build();
            seats.add(seat);
        }

        TtFight fight = TtFight.builder()
                .boxData(JSONUtil.toJsonStr(boxMap))
                .boxPriceTotal(boxTotalPrice)
                .status(0)
                .roundNumber(boxNum)
                .userId(player.getUserId())
                .playerNum(createFightParam.getPlayerNumber())
                .seats(JSONUtil.toJsonStr(seats))
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();

        return R.ok(fight);
    }

    private R joinFightCheck(Integer fightId, TtUser player) {

        ObjectMapper objectMapper = new ObjectMapper();

        // 查询对局信息（把mybatis一级缓存关掉）
        TtFight fight = null;
        fight = new LambdaQueryChainWrapper<>(fightMapper)
                .eq(TtFight::getId, fightId)
                .eq(TtFight::getStatus, "0")
                .one();

        if (ObjectUtils.isEmpty(fight)) return R.fail("此对局已结束！");

        if (fight.getUserId().equals(player.getUserId())) return R.fail("您是对战发起者，无法重复加入！");

        // 如果已报名，直接进入房间
        List<FightSeat> seats = fight.getSeatList();

        for (FightSeat seat : seats) {

            if (ObjectUtil.isNotEmpty(seat.getPlayerId()) && seat.getPlayerId().equals(player.getUserId())) {
                return R.ok(0, "已报名，再次进入房间。");
            }
        }

        // 检查余额
        if (player.getAccountAmount().add(player.getAccountCredits()).compareTo(fight.getBoxPriceTotal()) < 0)
            return R.fail("加入房间失败，您的账户余额不足！");


        boolean f = false;

        Integer joinIndex = null;

        for (int i = 0; i < seats.size(); i++) {

            FightSeat seat = objectMapper.convertValue(seats.get(i), FightSeat.class);

            seats.set(i, seat);

            if (seat.getStatus().equals(0) && !f) {

                seat.sitDown(player.getUserId());

                seat.setAvatar(player.getAvatar());

                seat.setNickName(player.getNickName());

                f = !f;

                joinIndex = i;

                continue;
            }

            if (ObjectUtil.isNotEmpty(seat.getPlayerId()) && seat.getPlayerId().equals(player.getUserId())) {

                if (Objects.nonNull(joinIndex)) seats.get(joinIndex).sitUp();

                return R.fail(2, "不可重复加入对局。");
            }
        }

        if (!f) return R.fail("对局人满。");

        // 返回新的座位信息
        return R.ok(seats);
    }

    // 加入房间
    @Override
    @Transactional
    public R<Object> joinFight(Integer fightId, TtUser player) {
        // 加入
        Boolean lock = false;
        for (int i = 0; i < 10; i++) {

            lock = redisLock.tryLock(JOIN_FIGHT_LOCK + fightId, 3L, 7L, TimeUnit.SECONDS);
            if (lock) {
                break;
            }
        }

        if (!lock) {

            return R.fail("服务器繁忙，稍后重试。");
        }

        try {
            // 复查
            R check = joinFightCheck(fightId, player);
            if (!check.getCode().equals(200)) {
                System.err.println("检查不通过");
                redisLock.unlock(JOIN_FIGHT_LOCK + fightId);
                return check;
            }
            if (ObjectUtil.isNotEmpty(check.getData()) && check.getData().equals(0)) {
                return check;
            }


            LambdaUpdateWrapper<TtFight> fightUpdate = new LambdaUpdateWrapper<>();

            fightUpdate
                    .eq(TtFight::getId, fightId)
                    .eq(TtFight::getStatus, 0)
                    .set(TtFight::getSeats, JSONUtil.toJsonStr(check.getData()));


            update(fightUpdate);

            log.info("玩家{}加入对局{}", player.getUserId(), fightId);

            TtFight fight = getById(fightId);

            // ws广播参加对局玩家信息（异步）
            CompletableFuture.runAsync(() -> {
                // 广播玩家加入消息
                WsFightRoom.broadcastFight(fight.getId(), WsResult.ok(PLAYER_JOIN.name(), player, "玩家加入房间"));
                WsFightRoom.broadcastFight(fight.getId(), WsResult.ok(FIGHT_ROOM_INFO.name(), fight, "对战房间最新信息"));
            }, customThreadPoolExecutor);

            if (seatsIsFull(fight.getSeatList())) {
                return R.ok(1, "加入对战成功。房间已满人");
            }
            return R.ok(0, "加入对战成功。");

        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("加入对战失败！");
        } finally {
            redisLock.unlock(JOIN_FIGHT_LOCK + fightId);
        }
    }

    public R fightBeginCheck(Integer fightId, TtUser player) {

        TtFight fight = new LambdaQueryChainWrapper<TtFight>(fightMapper)
                .eq(TtFight::getId, fightId)
                .one();

        if (ObjectUtil.isEmpty(fight)) return R.fail(null, "不存在的对局房间");

        if (fight.getStatus().equals(1)) return R.fail(fight, "对局已开始");

        if (fight.getStatus().equals(2) || fight.getStatus().equals(3)) return R.fail(fight, "对局已结束");


        for (FightSeat seat : fight.getSeatList()) {
            if (!seat.getStatus().equals(2)) return R.fail("用户" + seat.getPlayerId() + "未准备。");
        }

        return R.ok(fight);

    }

    // 开始游戏
    @Override
    public R fightBegin(Integer fightId, TtUser player) {


        Boolean lock = false;

        for (int i = 0; i < 4; i++) {

            lock = redisLock.tryLock(JOIN_FIGHT_BEGIN_LOCK + fightId, 2L, 7L, TimeUnit.SECONDS);

            if (lock) break;

        }

        ObjectMapper objectMapper = new ObjectMapper();

        // 多次尝试失败，检查一下房间状态
        if (!lock) {

            TtFight fight = new LambdaQueryChainWrapper<>(fightMapper)
                    .eq(TtFight::getId, fightId)
                    .one();
            if (fight.getStatus().equals(0)) return R.fail("系统繁忙，请稍后重试。");

            return R.ok(null, "游戏已经开始");
        }

        // 抢到锁
        try {

            // 检查对局信息
            R check = fightBeginCheck(fightId, player);

            if (!check.getCode().equals(200)) {

                if (ObjectUtil.isEmpty(check.getData())) return check;

                TtFight fight = objectMapper.convertValue(check.getData(), TtFight.class);

                if (fight.getStatus().equals(1)) return R.ok(fight, "对局已开始");

                if (fight.getStatus().equals(2) || fight.getStatus().equals(3)) return R.ok(fight, "对局已结束");

                return check;
            }

            TtFight fight = objectMapper.convertValue(check.getData(), TtFight.class);

            // 抽奖 计算开箱结果
            List<TtBoxRecords> fightResult = newComputerFight(fight);

            // 计算胜负
            List<Integer> winnerIds = computerWinner(fight.getModel(), fight.getSeatList(), fightResult);

            // 分配战利品
            int count = 0;

            for (TtBoxRecords item : fightResult) {

                if (ObjectUtil.isEmpty(item.getOrnamentId())) {
                    // 过滤掉抽空的情况
                    item.setHolderUserId(null);

                    continue;
                }

                if (winnerIds.contains(item.getUserId())) {

                    item.setHolderUserId(item.getUserId());

                } else {

                    int i = count % winnerIds.size();

                    item.setHolderUserId(winnerIds.get(i));

                    count++;
                }
            }

            // 分配失败者奖励
            List<TtBoxRecords> losePrize = loserPrize(fight, winnerIds);

            // 合并所有对战出货
            fightResult.addAll(losePrize);

            // 将对战结果临时存入Redis供进行中查询（写入数据库耗时长导致刚开始对战时进入的用户查询到空数据）
            String key = "fight_result:fight_" + fightId;
            redisCache.setCacheObject(key, fightResult, 3, TimeUnit.MINUTES);

            // 同步保存游戏出货结果
            boxRecordsService.saveBatch(fightResult);

            // 同步更新对局状态
            LambdaUpdateWrapper<TtFight> fightUpdate = new LambdaUpdateWrapper<>();
            fightUpdate
                    .eq(TtFight::getId, fightId)
                    .eq(TtFight::getStatus, 0)
                    .set(TtFight::getWinnerIds, JSON.toJSONString(winnerIds))
                    .set(TtFight::getStatus, 1)
                    .set(TtFight::getBeginTime, new Timestamp(System.currentTimeMillis()));
            update(fightUpdate);

            // transactionManager.commit(transaction);

            // 异步推送对局数据
            CompletableFuture.runAsync(() -> {

                // 构建结果集
                // 根据宝箱id查询关联的所有饰品
                Map<String, FightBoxVO> boxData = fight.getBoxDataMap();

                ArrayList<FightBoxVO> fightBoxVOList = new ArrayList<>();

                boxData.keySet().forEach(boxId -> {
                    List<TtBoxOrnamentsDataVO> boxOrnamentsVOS = boxOrnamentsMapper.selectTtBoxOrnamentsList(Integer.valueOf(boxId));
                    boxData.get(boxId).setOrnaments(boxOrnamentsVOS);
                    fightBoxVOList.add(boxData.get(boxId));
                });

                FightResultVO resultVO = FightResultVO.builder()
                        .winnerIds(winnerIds)
                        .fightResult(fightResult)
                        .fightBoxVOList(fightBoxVOList)
                        .build();

                // 推送对局结果数据
                TtFight newFight = new LambdaQueryChainWrapper<>(fightMapper)
                        .eq(TtFight::getId, fightId)
                        .eq(TtFight::getStatus, 1)
                        .one();

                resultVO.setFight(newFight);

                WsFightRoom.broadcastFight(fightId, WsResult.ok(FIGHT_RESULT.name(), resultVO));

                // 推送大厅最新的房间信息
                LambdaQueryWrapper<TtFight> fightQuery = new LambdaQueryWrapper<>();
                fightQuery
                        .eq(TtFight::getId, fightId)
                        .eq(TtFight::getStatus, 1);
                TtFight fight1 = this.getOne(fightQuery);

                WsFightHall.broadcast(WsResult.ok(ALL_FIGHT_ROOM.name(), Arrays.asList(fight1)));

                // 房间完成数据传输直接断开
                try {
                    Thread.sleep(200);
                    if (WsFightRoom.batchClose(newFight)) {
                        log.info("房间{}对战结果广播完成，已成功断开所有房间内连接。", newFight.getId());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }, customThreadPoolExecutor);

            return R.ok(null, "游戏开始。");

        } catch (Exception e) {
            e.printStackTrace();
            log.warn("开始游戏失败，对战房间号{} 异常信息：{}", fightId, e.getMessage());
            return R.fail("系统繁忙，请稍后重试。");
        } finally {
            redisLock.unlock(JOIN_FIGHT_BEGIN_LOCK + fightId);
        }
    }

    private List<TtBoxRecords> loserPrize(TtFight fight, List<Integer> winnerIds) {

        List<TtBoxRecords> loserBoxRecords = new ArrayList<>();

        List<Integer> losers = new ArrayList<>();

        for (FightSeat seat : fight.getSeatList()) {
            if (winnerIds.contains(seat.getPlayerId())) continue;
            losers.add(seat.getPlayerId());
        }

        // 获取失败奖励
        String loserPrize = configService.selectConfigByKey("fightLoserPrize");

        List<Integer> ornamentIds = JSONUtil.parseArray(loserPrize).toList(Integer.class);


        if (ornamentIds.isEmpty()) return loserBoxRecords;

        List<TtOrnament> loserPrizeList = new LambdaQueryChainWrapper<>(ttOrnamentMapper)
                .in(TtOrnament::getId, ornamentIds)
                .list();

        if (ObjectUtil.isEmpty(loserPrizeList) || loserPrizeList.isEmpty()) return loserBoxRecords;

        for (Integer loserId : losers) {

            List<TtBoxRecords> records = loserPrizeList.stream().map(ornament -> {
                return TtBoxRecords.builder()
                        .userId(loserId)

                        .ornamentId(ornament.getId())
                        .marketHashName(ornament.getMarketHashName())
                        .ornamentName(ornament.getShortName())
                        .imageUrl(ornament.getImageUrl())
                        .ornamentsPrice(ornament.getUsePrice())

                        .holderUserId(loserId)

                        .source(TtboxRecordSource.FIGHT.getCode())
                        .createTime(new Timestamp(System.currentTimeMillis()))
                        .updateTime(new Timestamp(System.currentTimeMillis()))
                        .fightId(fight.getId())
                        // .isShow(0)
                        .status(IN_PACKSACK_OFF.getCode())
                        .build();

            }).collect(Collectors.toList());

            loserBoxRecords.addAll(records);

        }

        return loserBoxRecords;
    }

    public List<Integer> computerWinner(String model, List<FightSeat> seats, List<TtBoxRecords> fightResult) {

        List<Integer> winnerIds = new ArrayList<>();

        if (model.equals("0")) {

            BigDecimal max = BigDecimal.ZERO;

            for (FightSeat seat : seats) {

                for (TtBoxRecords record : fightResult) {

                    if (record.getHolderUserId().equals(seat.getPlayerId())) {
                        seat.setAwardTotalPrices(seat.getAwardTotalPrices().add(record.getOrnamentsPrice()));
                    }
                }
                if (max.compareTo(seat.getAwardTotalPrices()) < 0) {
                    max = seat.getAwardTotalPrices();
                    winnerIds.clear();
                    winnerIds.add(seat.getPlayerId());
                } else if (max.compareTo(seat.getAwardTotalPrices()) == 0) {
                    winnerIds.add(seat.getPlayerId());
                }
            }
        } else if (model.equals("1")) {

            log.info("计算胜负，非酋模式。");

            BigDecimal min = null;
            for (FightSeat seat : seats) {
                for (TtBoxRecords record : fightResult) {
                    if (record.getHolderUserId().equals(seat.getPlayerId())) {
                        seat.setAwardTotalPrices(seat.getAwardTotalPrices()
                                .add(ObjectUtil.isNotEmpty(record.getOrnamentsPrice()) ? record.getOrnamentsPrice() : BigDecimal.ZERO));
                    }
                }
                if (ObjectUtil.isEmpty(min) || min.compareTo(BigDecimal.ZERO) == 0) {
                    min = seat.getAwardTotalPrices();
                }
                if (min.compareTo(seat.getAwardTotalPrices()) > 0) {
                    min = seat.getAwardTotalPrices();
                    winnerIds.clear();
                    winnerIds.add(seat.getPlayerId());
                } else if (min.compareTo(seat.getAwardTotalPrices()) == 0) {
                    winnerIds.add(seat.getPlayerId());
                }
            }

            log.info("赢家{}", winnerIds);

        } else {
            log.warn("非法的对战模式编号");
            return winnerIds;
        }

        return winnerIds;
    }

    // 观战，返回当前对局进行到了第几回个
    @Override
    public R audience(Integer fightId) {

        // TtFight fight = getById(fightId);
        TtFight fight = new LambdaQueryChainWrapper<>(fightMapper).eq(TtFight::getId, fightId).one();

        if (ObjectUtil.isNull(fight)) return R.fail("不存在的对局。");
        if (fight.getStatus().equals(0)) return R.fail(601, "对局尚未开始。");
        // if (!fight.getStatus().equals(2)) return R.fail(602,"对局已结束。");

        Long currentRound = -1L;

        if (!fight.getStatus().equals(2) && !fight.getStatus().equals(3)) {

            // 时间差 计算当前进行到第几回合
            LocalDateTime now = LocalDateTime.now();
            Timestamp beginTime = fight.getBeginTime();
            LocalDateTime beginTime1 = beginTime.toLocalDateTime();

            Duration duration = Duration.between(beginTime1, now);
            currentRound = duration.toMillis() / fightRoundTime; // 这个秒要和前端的 【每回合时间】 同步

        }

        // 游戏结果
        // 根据宝箱id查询关联的所有饰品
        ArrayList<FightBoxVO> fightBoxVOList = new ArrayList<>();

        Map<String, FightBoxVO> boxData = fight.getBoxDataMap();

        boxData.keySet().forEach(boxId -> {
            List<TtBoxOrnamentsDataVO> boxOrnamentsVOS = boxOrnamentsMapper.selectTtBoxOrnamentsList(Integer.valueOf(boxId));
            FightBoxVO fightBoxVO = JSONUtil.toBean(JSONUtil.toJsonStr(boxData.get(boxId)), FightBoxVO.class);
            fightBoxVO.setOrnaments(boxOrnamentsVOS);
            fightBoxVOList.add(fightBoxVO);
        });

        List<TtBoxRecords> list = new LambdaQueryChainWrapper<>(boxRecordsMapper)
                .eq(TtBoxRecords::getFightId, fightId)
                .list();

        AudienceVO result = AudienceVO.builder()
                .currentRound(currentRound.intValue())
                .winnerIds(fight.getWinnerList())
                .fightResult(list)
                .fight(fight)
                .fightBoxVOList(fightBoxVOList)
                .build();

        return R.ok(result, "房间游戏数据");
    }

    @Override
    public R fightEnd(Integer fightId) {

        TtFight fight = new LambdaQueryChainWrapper<>(fightMapper)
                .eq(TtFight::getId, fightId)
                .one();

        if (ObjectUtil.isEmpty(fight)) {
            return R.ok(null, "不存在的对局");
        }
        if (fight.getStatus().equals(0)) {
            return R.ok(null, "对局未开始");
        }
        if (fight.getStatus().equals(2) || fight.getStatus().equals(3)) {
            return R.ok(fight, "对局已结束");
        }

        Boolean lock = false;
        for (int i = 0; i < 4; i++) {

            lock = redisLock.tryLock(JOIN_FIGHT_END_LOCK + fightId, 2L, 7L, TimeUnit.SECONDS);

            if (!lock) {

                continue;
            }
            break;
        }

        if (!lock) return R.ok("系统繁忙。请稍后查看对战结果");

        try {
            // 校验时间
            LocalDateTime now = LocalDateTime.now();
            Timestamp beginTime = fight.getBeginTime();

            LocalDateTime beginTime1 = beginTime.toLocalDateTime();
            Duration duration = Duration.between(beginTime1, now);

            Long second = duration.toMillis() / 1000;
            Integer roundTime = fightRoundTime / 1000;

            if (fight.getRoundNumber() * roundTime > second.intValue()) return R.fail("对局尚未结束，请稍后重试。");

            // 更新游戏状态
            Timestamp endTime = new Timestamp(System.currentTimeMillis());
            new LambdaUpdateChainWrapper<>(fightMapper)
                    .eq(TtFight::getId, fightId)
                    .set(TtFight::getStatus, 2)
                    .set(TtFight::getEndTime, endTime)
                    .update();

            // 更新背包
            LambdaUpdateWrapper<TtBoxRecords> boxRecordsUpdate = new LambdaUpdateWrapper<>();
            boxRecordsUpdate
                    .eq(TtBoxRecords::getFightId, fightId)
                    .set(TtBoxRecords::getStatus, IN_PACKSACK_ON.getCode());

            boxRecordsService.update(boxRecordsUpdate);

            fight.setStatus(2);
            fight.setEndTime(endTime);

            // 异步更新对局数据
            CompletableFuture.runAsync(() -> {
                WsFightHall.broadcast(WsResult.ok(ALL_FIGHT_ROOM.name(), Arrays.asList(fight)));
            }, customThreadPoolExecutor);

            // 异步更新房间出货金额和倍差
            CompletableFuture.runAsync(() -> {

                List<TtBoxRecords> boxRecords = boxRecordsService.list(Wrappers.lambdaQuery(TtBoxRecords.class)
                        .eq(TtBoxRecords::getFightId, fightId));
                BigDecimal totalPrice = boxRecords.stream().map(TtBoxRecords::getOrnamentsPrice).reduce(BigDecimal.ZERO, BigDecimal::add); //总出货

                new LambdaUpdateChainWrapper<>(fightMapper)
                        .eq(TtFight::getId, fightId)
                        .set(TtFight::getOpenTotalPrice, totalPrice)
                        .update();
            }, customThreadPoolExecutor);

            return R.ok(fight);

        } catch (Exception e) {
            e.printStackTrace();
            log.warn("客户端【主动结束】游戏异常。");
            return R.ok(fight, "服务器繁忙。");
        } finally {
            redisLock.unlock(JOIN_FIGHT_END_LOCK + fightId);
        }
    }

    public R seatrReadyCheck(Integer fightId, TtUser player) {

        TtFight fight = new LambdaQueryChainWrapper<>(fightMapper)
                .eq(TtFight::getId, fightId)
                .one();
        if (ObjectUtil.isEmpty(fight)) return R.fail("不存在的对局。");
        if (fight.getStatus().equals(1)) return R.fail("对局进行中。");
        if (fight.getStatus().equals(2) || fight.getStatus().equals(3)) return R.fail("对局已结束。");

        return R.ok(fight);
    }

    // 玩家准备游戏
    @Transactional
    @Override
    public R seatrReady(Integer fightId, TtUser player) {

        // 检查
        R check = seatrReadyCheck(fightId, player);
        if (!check.getCode().equals(200)) return check;

        // fight
        ObjectMapper objectMapper = new ObjectMapper();
        TtFight fight = objectMapper.convertValue(check.getData(), TtFight.class);

        // 更新座位状态

        for (FightSeat seat : fight.getSeatList()) {

            //找到自己的座位
            if (seat.getPlayerId().equals(player.getUserId())) {

                String lock = JOIN_FIGHT_SEAT_READY_LOCK + fightId + ":" + seat.getCode();

                Boolean tryLock = false;
                for (int i = 0; i < 2; i++) {
                    tryLock = redisLock.tryLock(lock, 2L, 7L, TimeUnit.SECONDS);
                    if (tryLock) break;
                }
                if (!tryLock) return R.fail("系统繁忙，请稍后重试。");


                try {
                    //座位准备
                    if (seat.getStatus().equals(2)) return R.fail("已准备，请勿重复操作。");
                    if (ObjectUtil.isEmpty(seat.ready())) return R.fail("座位状态（0）异常。");
                    updateById(fight);

                    // 账户结算
                    R<Map<String, BigDecimal>> mapR = readyFightAccounting(fight, player);
                    if (!mapR.getCode().equals(200)) {
                        seat.readyCancel().sitUp();
                        updateById(fight);
                        log.warn("对战{},用户{}加入扣款异常，踢出座位{}。", fightId, player.getUserId(), seat.getCode());
                        return mapR;
                    }

                    // 同步保存参与人员信息
                    TtFightUser build = TtFightUser.builder()
                            .fightId(fightId)
                            .userId(player.getUserId())
                            .joinPrice(fight.getBoxPriceTotal())
                            .createTime(new Date())
                            .updateTime(new Date())
                            .build();
                    fightUserService.save(build);

                } catch (Exception e) {
                    seat.readyCancel().sitUp();
                    updateById(fight);
                    e.printStackTrace();
                } finally {
                    redisLock.unlock(lock);
                }


                // 广播房间消息
                CompletableFuture.runAsync(() -> {
                    // 广播玩家准备消息
                    WsFightRoom.broadcastFight(fightId, WsResult.ok(PLAYER_READY.name(), player, "玩家已准备"));
                    WsFightRoom.broadcastFight(fightId, WsResult.ok(FIGHT_ROOM_INFO.name(), fight));
                }, customThreadPoolExecutor);


                return R.ok();
            }
        }

        return R.fail("你不在此对局座位中");
    }

    @Override
    public R fightRoomExit(Integer fightId, TtUser player) {

        TtFight fight = new LambdaQueryChainWrapper<>(fightMapper)
                .eq(TtFight::getId, fightId)
                .eq(TtFight::getStatus, 0)
                .one();


        //排除观战
        AtomicBoolean gz = new AtomicBoolean(true);
        fight.getSeatList().forEach(seat -> {
            if (player.getUserId().equals(seat.getPlayerId())) {
                gz.set(false);
            }
        });

        if (gz.get()) {
            return R.ok("退出观战房间！");
        }

        //房间内对战玩家校验房间状态
        if (ObjectUtil.isEmpty(fight)) return R.fail("对局已开始，不能退出。");

        for (FightSeat seat : fight.getSeatList()) {

            Integer playerId = seat.getPlayerId();
            if (ObjectUtil.isEmpty(playerId)) {
                continue;
            }

            if (playerId.equals(player.getUserId())) {

                if (seat.getStatus().equals(2)) {

                    return R.ok("退出成功，消费不退回。");

                } else if (seat.getStatus().equals(1)) {

                    if (!seat.sitUp()) return R.fail("退出失败，请稍后重试。");

                    fight.setSeats(JSONUtil.toJsonStr(fight.getSeatList()));

                    updateById(fight);
                    // 广播房间消息
                    CompletableFuture.runAsync(() -> {
                        List<TtFight> list = new LambdaQueryChainWrapper<>(fightMapper)
                                .eq(TtFight::getStatus, 0)
                                .orderByDesc(TtFight::getCreateTime)
                                .list();
                        WsFightHall.broadcast(WsResult.ok(ALL_FIGHT_ROOM.name(), list, "对战房间最新信息"));
                        WsFightRoom.broadcast(WsResult.ok(FIGHT_ROOM_INFO.name(), fight));
                    }, customThreadPoolExecutor);

                    return R.ok("退出成功。");

                } else {
                    return R.fail("异常的座位状态0");
                }
            }
        }

        return R.fail("状态异常，你不在本对局中。");

    }

    @Override
    public List<ApiFightListDataVO> getFightList(FightOnMyOwnParam param) {

        Page<TtFight> pageInfo = new Page<>(param.getPage(), param.getSize());

        pageInfo.setOptimizeCountSql(false);

        LambdaQueryWrapper<TtFight> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(ObjectUtil.isNotEmpty(param.getFightId()), TtFight::getId, param.getFightId())
                .eq(ObjectUtil.isNotEmpty(param.getModel()), TtFight::getModel, param.getModel())
                .in(ObjectUtil.isNotEmpty(param.getStatusList()), TtFight::getStatus, param.getStatusList());

        // 我参与的
        if (ObjectUtil.isNotNull(param.getPlayerId())) {
            List<Integer> myOwnFights = fightUserMapper.myOwnFights(param.getPlayerId());
            if (myOwnFights.size() > 0) {
                wrapper.in(TtFight::getId, myOwnFights);
            } else {
                wrapper.eq(TtFight::getId, "non_existing_value");
            }
        }
        wrapper.orderByDesc(TtFight::getCreateTime);

        List<TtFight> page = this.page(pageInfo, wrapper).getRecords();

        return page.stream().map(fight -> {
            ApiFightListDataVO build = ApiFightListDataVO.builder().build();
            BeanUtil.copyProperties(fight, build);
            build.setBoxData(JSONUtil.toJsonStr(fight.getBoxData()));
            build.setSeats(fight.getSeatList());
            build.setRoundNumber(fight.getRoundNumber());
            return build;
        }).collect(Collectors.toList());

    }

    @Override
    public R fightDetail(FightDetailParam param) {

        TtFight fight = this.getById(param.getFightId());
        if (!fight.getStatus().equals(2) && !fight.getStatus().equals(3)) {
            return R.fail("对局未结束");
        }

        TtFightVO vo = new TtFightVO();
        BeanUtil.copyProperties(fight, vo);

        LambdaQueryWrapper<TtBoxRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TtBoxRecords::getFightId, param.getFightId());

        List<TtBoxRecords> list = ttBoxRecordsService.list(wrapper);
        vo.setFightResult(list);

        return R.ok(vo);
    }

    @Override
    public R earlierHistory(FightDetailParam param) {

        LambdaQueryWrapper<TtFight> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TtFight::getId, param.getFightId());

        TtFight one = this.getOne(queryWrapper);
        Date createTime = one.getCreateTime();

        Page<TtFight> pageInfo = new Page<>(param.getPage(), param.getSize());
        pageInfo.setOptimizeCountSql(false);
        queryWrapper.clear();
        queryWrapper
                .le(TtFight::getCreateTime, createTime)
                .eq(TtFight::getStatus, 0);

        pageInfo = this.page(pageInfo, queryWrapper);

        return R.ok(pageInfo.getRecords());
    }

    @Override
    public List<ApiFightRankingVO> getFightRankingByDate(String date) {
        return apiFightMapper.getFightRankingByDate(date);
    }

    @Override
    public R<Boolean> saveFightBoutData(FightBoutData fightBoutData) {
        String key = "fight_bout_data:fight_" + fightBoutData.getFightId();
        redisCache.setCacheObject(key, fightBoutData, fightBoutData.getExpirationTime(), TimeUnit.MILLISECONDS);
        return R.ok();
    }

    @Override
    public R<Integer> getFightBoutNum(Integer fightId) {
        String key = "fight_bout_data:fight_" + fightId;
        FightBoutData fightBoutData = redisCache.getCacheObject(key);
        if (StringUtils.isNotNull(fightBoutData)) {
            return R.ok(fightBoutData.getBoutNum());
        } else {
            return R.fail("对战回合不存在");
        }
    }

    // 加入对战账户结算
    public R<Map<String, BigDecimal>> readyFightAccounting(TtFight fight, TtUser player) {

        // 扣款
        BigDecimal totalMoney = fight.getBoxPriceTotal();

        R<Map<String, BigDecimal>> mapR = deductMoney(totalMoney, player);
        if (!mapR.getCode().equals(200)) {
            return mapR;
        }

        // 异步统计日志和推广福利
        CompletableFuture.runAsync(() -> {

            Map<String, BigDecimal> moneyMap = mapR.getData();
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
                    .source(TtAccountRecordSource.GAME_TYPE_02.getCode())
                    .remark(TtAccountRecordSource.GAME_TYPE_02.getMsg())

                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()))
                    .build();

            if (blendErcash.getFinalAmount() != null && BigDecimal.ZERO.compareTo(blendErcash.getFinalAmount()) > 0) {
                blendErcash.setFinalAmount(BigDecimal.ZERO);
            }
            if (blendErcash.getFinalCredits() != null && BigDecimal.ZERO.compareTo(blendErcash.getFinalCredits()) > 0) {
                blendErcash.setFinalCredits(BigDecimal.ZERO);
            }

            int insert = apiTtUserBlendErcashMapper.insert(blendErcash);

        }, customThreadPoolExecutor);

        return mapR;
    }

    // 扣款
    public R<Map<String, BigDecimal>> deductMoney(BigDecimal consumption, TtUser player) {

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

    public List<TtBoxRecords> computerFight(TtFight fight, TtBox box, Integer number) {

        // 计算结果
        ArrayList<TtBoxRecords> result = new ArrayList<>();

        for (FightSeat seat : fight.getSeatList()) {
            TtUser player = userService.getById(seat.getPlayerId());
            List<TtBoxRecords> ttBoxRecords = apiBindBoxService.openBoxArithmetic(fight.getId(), player, box, number);
            // 补充图片
            ttBoxRecords.stream().forEach(boxRecord -> {
                TtOrnament ornament = ttOrnamentsZBTService.getById(boxRecord.getOrnamentId());
                boxRecord.setImageUrl(ornament.getImageUrl());
            });
            result.addAll(ttBoxRecords);
        }
        return result;
    }


    // 新开箱算法
    public List<TtBoxRecords> newComputerFight(TtFight fight) {

        List<TtBoxRecords> result = new ArrayList<>();


        // 抽奖人列表
        ArrayList<Integer> userList = new ArrayList<>();


        for (FightSeat seat : fight.getSeatList()) {

            if (ObjectUtil.isEmpty(seat.getPlayerId())) continue;

            userList.add(seat.getPlayerId());
        }

        Integer round = 1;

        for (String boxId : fight.getBoxDataMap().keySet()) {
            // 获取宝箱详细信息
            TtBox box = boxMapper.selectById(boxId);

            for (int i = 0; i < fight.getBoxDataMap().get(boxId).getNumber(); i++) {

                // 一个宝箱一回合
                List<TtBoxRecords> roundResult = roundLottery(round, box, userList, fight);

                if (ObjectUtil.isEmpty(roundResult) || roundResult.isEmpty()) return null;

                result.addAll(roundResult);

                round++;
            }

            // 统计宝箱历史开箱数量
            new LambdaUpdateChainWrapper<>(boxMapper)
                    .eq(TtBox::getBoxId, boxId)
                    .set(TtBox::getOpenNum, box.getOpenNum() + fight.getBoxDataMap().get(boxId).getNumber())
                    .update();
        }

        // 补充信息
        result.stream().forEach(item -> {

            item.setFightId(fight.getId());

        });

        // 保存信息
        return result;
    }

    public boolean isAnyElementInListString(String fightRebootIds, List<Integer> userList) {
        // 去除字符串首尾的方括号
//        String trimmed = fightRebootIds.trim().substring(1, fightRebootIds.length() - 1);
        // 根据逗号分割字符串为数组
        String[] elements = fightRebootIds.replace("，", ",").split(",");
        // 将数组转换为 List 并去除每个元素前后的空格
        List<String> stringList = Arrays.stream(elements)
                .map(String::trim)
                .collect(Collectors.toList());

        // 遍历传入的 list，检查是否有元素存在于 stringList 中
        for (Integer uid : userList) {
            if (stringList.contains(String.valueOf(uid))) {
                return true;
            }
        }
        return false;
    }

    // 抽奖(一回合)
    public List<TtBoxRecords> roundLottery(Integer round, TtBox box, ArrayList<Integer> userList, TtFight fight) {

        ArrayList<TtBoxRecords> result = new ArrayList<>();

        //判断对局是否存在机器人
        String rebootIds = configService.selectConfigByKey("fightRebootIds");

        boolean isRebootFight = isAnyElementInListString(rebootIds, userList);

        // 循环所有人
        for (Integer uid : userList) {

            TtUser user = userService.getById(uid);

            //如果用户存在机器人并且是非酋模式（互换游戏类玩家的爆率，主播爆率变玩家，玩家爆率变主播）
            if (isRebootFight && fight.getModel().equals("1")) {
                if (user.getUserType().equals("01")) {
                    user.setUserType("02");
                } else if (user.getUserType().equals("02")) {
                    user.setUserType("01");
                }

            }

            // 抽奖
            String ornamentId = lotteryMachine.singleLottery(user, box);

            if (StringUtils.isBlank(ornamentId)) {

                log.warn("用户{}抽奖失败", user.getUserId());

                return null;
            }

            // 饰品详情
            // TtOrnamentsA ornamentsData = bindBoxMapper.getOrnamentsData(box.getBoxId(), Integer.valueOf(hashName), null);
            TtOrnamentsA ornamentsData = bindBoxMapper.ornamentsInfo(box.getBoxId(), ornamentId);

            // 抽奖成功，构建开箱记录数据
            TtBoxRecords boxRecord = TtBoxRecords.builder()
                    .userId(user.getUserId())
                    .boxId(box.getBoxId())
                    .boxName(box.getBoxName())
                    .boxPrice(box.getPrice())
                    .ornamentId(Long.valueOf(ornamentId))
                    // .marketHashName(ornamentsData.)
                    .ornamentName(ornamentsData.getShortName())
                    .imageUrl(ornamentsData.getImageUrl())
                    .ornamentsPrice(ornamentsData.getUsePrice())
                    .ornamentsLevelId(ornamentsData.getOrnamentsLevelId())
                    .ornamentLevelImg(ornamentsData.getLevelImg())
                    .holderUserId(user.getUserId())
                    .source(TtboxRecordSource.FIGHT.getCode())
                    .status(IN_PACKSACK_ON.getCode())
                    .fightRoundNumber(round)
                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()))
                    // .fightId()   后续补充
                    // .isShow(0)
                    .status(IN_PACKSACK_OFF.getCode())
                    .build();

            result.add(boxRecord);

        }

        return result;
    }

    // 抽奖方法(一次) 这个方法要加锁
    // public TtBoxRecords lottery(Integer round, TtBox box, TtUser user) {
    //
    //     String basekey = OPEN_BOX_GOODS_SPACE + box.getBoxId() + ":";
    //
    //     Map<String, Integer> boxSpace = null;
    //     Integer residue = null;
    //
    //     // 1 根据身份获取对应的奖品空间
    //     if (user.getUserType().equals("01")) {
    //
    //         String boxSpaceKey = basekey + ANCHOR_ODDS + ":ornaments:";
    //         String residueKey = basekey + ANCHOR_ODDS + ":residue:";
    //
    //         // 去redis找这个宝箱的奖品空间 map{ornamentId:number}
    //         boxSpace = redisCache.getCacheMap(boxSpaceKey);
    //         residue = (Integer) redisCache.getCacheObject(residueKey);
    //
    //         // 没有，从数据库加载奖品空间
    //         if (ObjectUtil.isEmpty(boxSpace) || ObjectUtil.isEmpty(residue) || residue == 0) {
    //
    //             List<TtBoxOrnaments> list = new LambdaQueryChainWrapper<TtBoxOrnaments>(boxOrnamentsMapper)
    //                     .eq(TtBoxOrnaments::getBoxId, box.getBoxId())
    //                     .list();
    //
    //             residue = 0;
    //             for (TtBoxOrnaments ornament : list) {
    //                 // HashMap<String, Integer> map = new HashMap<>();
    //                 // map.put(String.valueOf(ornament.getOrnamentsId()), ornament.getAnchorOdds());
    //                 // boxSpace = map;
    //                 boxSpace.put(String.valueOf(ornament.getOrnamentId()), ornament.getAnchorOdds());
    //                 // redisCache.setCacheMap(boxSpaceKey, map);
    //                 residue += ornament.getAnchorOdds();
    //             }
    //             // redisCache.setCacheObject(residueKey, residue);
    //
    //         }
    //
    //     } else if (user.getUserType().equals("02")) {
    //
    //         String boxSpaceKey = basekey + REAL_OPPS + ":ornaments:";
    //         String residueKey = basekey + REAL_OPPS + ":residue:";
    //
    //         // 去redis找这个宝箱的奖品空间 map{ornamentId:number}
    //         boxSpace = redisCache.getCacheMap(boxSpaceKey);
    //         residue = (Integer) redisCache.getCacheObject(residueKey);
    //
    //         // 没有，从数据库加载奖品空间
    //         if (ObjectUtil.isEmpty(residue) || residue == 0 || ObjectUtil.isEmpty(boxSpace)) {
    //
    //             List<TtBoxOrnaments> list = new LambdaQueryChainWrapper<TtBoxOrnaments>(boxOrnamentsMapper)
    //                     .eq(TtBoxOrnaments::getBoxId, box.getBoxId())
    //                     .list();
    //
    //             residue = 0;
    //             for (TtBoxOrnaments ornament : list) {
    //                 // HashMap<String, Integer> map = new HashMap<>();
    //                 // map.put(String.valueOf(ornament.getOrnamentsId()), ornament.getRealOdds());
    //                 // boxSpace = map;
    //                 boxSpace.put(String.valueOf(ornament.getOrnamentId()), ornament.getRealOdds());
    //                 // redisCache.setCacheMap(boxSpaceKey, map);
    //                 residue += ornament.getRealOdds();
    //             }
    //             // redisCache.setCacheObject(residueKey, residue);
    //         }
    //
    //     } else {
    //         log.warn("【抽奖】非法的用户类型");
    //         return null;
    //     }
    //
    //     // 2 计算随机数，开始抽奖（5次尝试）
    //     String targetOrnamentId = null;
    //     for (int c = 0; c < 5; c++) {
    //
    //         Random random = new Random();
    //         int r = 0;
    //         for (int i = 0; i < 5; i++) {
    //             r = random.nextInt(residue);
    //         }
    //
    //         int count = 0;  //宝箱的第几个饰品
    //         boolean flag = false;
    //         for (String ornamentId : boxSpace.keySet()) {
    //             if (flag) break;
    //             for (int i = 0; i < boxSpace.get(ornamentId); i++) {
    //                 if (count == r) {
    //                     targetOrnamentId = ornamentId;
    //                     flag = true;
    //                     break;
    //                 }
    //                 count++;
    //             }
    //         }
    //
    //         if (!StringUtils.isBlank(targetOrnamentId)){
    //             break;
    //         }
    //     }
    //
    //     // 多次尝试依然没抽到东西，按序给一个
    //     if (ObjectUtil.isEmpty(targetOrnamentId)) {
    //         for (String ornamentId : boxSpace.keySet()){
    //             if (boxSpace.get(ornamentId)>0){
    //                 targetOrnamentId = ornamentId;
    //             }
    //         }
    //     }
    //
    //     // 3 缓存减一
    //     if (user.getUserType().equals("01")) {
    //         String boxSpaceKey = basekey + ANCHOR_ODDS + ":ornaments:";
    //         String residueKey = basekey + ANCHOR_ODDS + ":residue:";
    //         boxSpace.put(targetOrnamentId, boxSpace.get(targetOrnamentId) - 1);
    //         redisCache.setCacheMap(boxSpaceKey, boxSpace);
    //         redisCache.setCacheObject(residueKey, residue - 1);
    //     } else if (user.getUserType().equals("02")) {
    //         String boxSpaceKey = basekey + REAL_OPPS + ":ornaments:";
    //         String residueKey = basekey + REAL_OPPS + ":residue:";
    //         boxSpace.put(targetOrnamentId, boxSpace.get(targetOrnamentId) - 1);
    //         redisCache.setCacheMap(boxSpaceKey, boxSpace);
    //         redisCache.setCacheObject(residueKey, residue - 1);
    //     }
    //
    //     // 4 抽奖成功，构建开箱记录数据
    //     TtOrnamentsA ornamentsData = bindBoxMapper.getOrnamentsData(box.getBoxId(), Integer.valueOf(targetOrnamentId), null);
    //     TtBoxRecords boxRecord = TtBoxRecords.builder()
    //             .userId(user.getUserId())
    //             .boxId(box.getBoxId())
    //             .boxName(box.getBoxName())
    //             .boxPrice(box.getPrice())
    //             .ornamentsId(Integer.valueOf(targetOrnamentId))
    //             .ornamenName(ornamentsData.getShortName())
    //             .imageUrl(ornamentsData.getImageUrl())
    //             .ornamentsPrice(ornamentsData.getUsePrice())
    //             .ornamentsLevelId(ornamentsData.getOrnamentsLevelId())
    //             .holderUserId(user.getUserId())
    //             .source("0")
    //             .status("0")
    //             .fightRoundNumber(round)
    //             // .fightId()   后续补充
    //             .isShow(0)
    //             .build();
    //
    //     return boxRecord;
    // }


    // 座位是否人满
    public Boolean seatsIsFull(List<FightSeat> seats) {
        boolean f = true;
        for (FightSeat seat : seats) {
            if (seat.getStatus().equals(0)) return !f;
        }
        return f;
    }

    // @Override
    // @Transactional
    // public R<Object> joinFight(Integer fightId, Integer joinSeatNum, TtUser ttUser, Integer rounds) {
    //
    //     // 查询回合信息
    //     TtFight ttFight = new LambdaQueryChainWrapper<>(fightMapper)
    //             .eq(TtFight::getId, fightId)
    //             .eq(TtFight::getStatus, "0")
    //             .one();
    //     if (ObjectUtils.isEmpty(ttFight)) return R.fail("加入对战失败，该房间已结束！");
    //
    //     if (ttUser.getAccountAmount().compareTo(ttFight.getBoxPriceTotal()) < 0)
    //         return R.fail("加入房间失败，您的账户余额不足！");
    //
    //     // 位置是否为空
    //     List<TtFightUser> fightUserListIsSeatNum = new LambdaQueryChainWrapper<>(fightUserMapper)
    //             .eq(TtFightUser::getFightId, fightId)
    //             .eq(TtFightUser::getJoinSeatNum, joinSeatNum)
    //             .eq(TtFightUser::getStatus, "0")
    //             .list();
    //     if (!ObjectUtils.isEmpty(fightUserListIsSeatNum)) return R.fail("当前位置已被锁定，请重新选择位置！！");
    //
    //     List<TtFightUser> fightUserListIsJoin = new LambdaQueryChainWrapper<>(fightUserMapper)
    //             .eq(TtFightUser::getFightId, fightId)
    //             .eq(TtFightUser::getUserId, ttUser.getUserId())
    //             .list();
    //     if (fightUserListIsJoin.size() == 1) return R.fail("您已成功加入该房间，请勿重复加入！");
    //     if (fightUserListIsJoin.size() > 1) return R.fail("数据异常！");
    //
    //     // 加入对战
    //     while (true) {
    //         Boolean lock = redisLock.tryLock(JOIN_FIGHT_LOCK + fightId, 5L, 10L, TimeUnit.SECONDS);
    //         if (!lock) {
    //             continue;
    //         }
    //         try {
    //             List<TtFightUser> fightUserList = new LambdaQueryChainWrapper<>(fightUserMapper)
    //                     .eq(TtFightUser::getFightId, fightId)
    //                     .eq(TtFightUser::getStatus, "0")
    //                     .list();
    //
    //             if (fightUserList.size() >= ttFight.getPlayerNum()) return R.fail("数据异常！");
    //
    //             // 创建并保存对战用户
    //             TtFightUser ttFightUser = TtFightUser.builder().build();
    //             ttFightUser.setFightId(fightId);
    //             ttFightUser.setUserId(ttUser.getUserId());
    //             ttFightUser.setJoinPrice(ttFight.getBoxPriceTotal());
    //             ttFightUser.setJoinSeatNum(joinSeatNum);
    //             ttFightUser.setCreateTime(new Date());
    //             int insertTtFightUser = fightUserMapper.insert(ttFightUser);
    //             log.info("玩家{}加入对战{}", ttUser.getUserId(), fightId);
    //             if (insertTtFightUser <= 0) return R.fail("数据异常！");
    //
    //             // ws广播参加对局玩家信息
    //             JoinFightUserDataVO joinFightUserData = apiFightMapper.selectJoinFightUserData(ttFightUser.getId());
    //             ResultData<JoinFightUserDataVO> resultData = new ResultData<>();
    //             resultData.setCode(3);
    //             resultData.setTypeName("success");
    //             resultData.setData(joinFightUserData);
    //             // 广播给所有人
    //             WebSocketUsers.sendMessageToUsersByText(JSON.toJSONString(resultData));
    //
    //             // 结算
    //             BigDecimal openBoxBeanTotal = ttFight.getBoxPriceTotal();
    //             ttUser.setAccountAmount(ttUser.getAccountAmount().subtract(openBoxBeanTotal));
    //             ttUser.setAccountCredits(ttUser.getAccountCredits().add(openBoxBeanTotal));
    //             userService.updateById(ttUser);
    //             // 加入mq（推广奖励）
    //             if (!ObjectUtil.isEmpty(ttUser.getParentId())) {
    //                 pWelfareMQData mqData = pWelfareMQData.builder()
    //                         .userId(ttUser.getUserId())
    //                         .account(openBoxBeanTotal)
    //                         .createTime(new Timestamp(System.currentTimeMillis()))
    //                         .build();
    //                 rabbitTemplate.convertAndSend(PROMOTION_WELFARE_EXCHANGE.getValue(), PROMOTION_WELFARE_KEY1.getValue(), mqData);
    //             }
    //             // 消费日志
    //             if (fightUserList.isEmpty()) {
    //                 userService.insertUserAmountRecords(ttUser.getUserId(), "1", "2", openBoxBeanTotal.negate(), ttUser.getAccountAmount());
    //                 userService.insertUserCreditsRecords(ttUser.getUserId(), "2", "2", openBoxBeanTotal, ttUser.getAccountCredits());
    //             } else {
    //                 userService.insertUserAmountRecords(ttUser.getUserId(), "1", "3", openBoxBeanTotal.negate(), ttUser.getAccountAmount());
    //                 userService.insertUserCreditsRecords(ttUser.getUserId(), "2", "3", openBoxBeanTotal, ttUser.getAccountCredits());
    //             }
    //
    //             // pool 玩家一加入就计算开箱结果
    //             CompletableFuture.runAsync(() -> openBox(ttFight, ttUser), customThreadPoolExecutor);
    //             // openBox(ttFight, ttUser);
    //
    //             // 如果人满，统一保存所有结果
    //             if (fightUserList.size() + 1 == ttFight.getPlayerNum()) {
    //                 CompletableFuture.runAsync(() -> sendResult(ttFight, rounds), customThreadPoolExecutor);
    //                 // sendResult(ttFight,rounds);
    //             }
    //
    //             return R.ok("加入对战成功！");
    //
    //         } catch (Exception e) {
    //             return R.fail("数据异常！");
    //         } finally {
    //             redisLock.unlock(JOIN_FIGHT_LOCK + fightId);
    //         }
    //
    //         // if (lock) {
    //         //     try {
    //         //         List<TtFightUser> fightUserList = new LambdaQueryChainWrapper<>(fightUserMapper).eq(TtFightUser::getFightId, fightId)
    //         //                 .eq(TtFightUser::getStatus, "0").list();
    //         //         if (fightUserList.size() < ttFight.getPlayerNum()) {
    //         //             TtFightUser ttFightUser = TtFightUser.builder().build();
    //         //             ttFightUser.setFightId(fightId);
    //         //             ttFightUser.setUserId(ttUser.getUserId());
    //         //             ttFightUser.setJoinPrice(ttFight.getBoxPriceTotal());
    //         //             ttFightUser.setJoinSeatNum(joinSeatNum);
    //         //             ttFightUser.setCreateTime(new Date());
    //         //             int insertTtFightUser = fightUserMapper.insert(ttFightUser);
    //         //             if (insertTtFightUser > 0) {
    //         //                 JoinFightUserDataVO joinFightUserData = apiFightMapper.selectJoinFightUserData(ttFightUser.getId());
    //         //                 ResultData<JoinFightUserDataVO> resultData = new ResultData<>();
    //         //                 resultData.setCode(3);
    //         //                 resultData.setTypeName("success");
    //         //                 resultData.setData(joinFightUserData);
    //         //                 WebSocketUsers.sendMessageToUsersByText(JSON.toJSONString(resultData));
    //         //                 BigDecimal openBoxBeanTotal = ttFight.getBoxPriceTotal();
    //         //                 ttUser.setAccountAmount(ttUser.getAccountAmount().subtract(openBoxBeanTotal));
    //         //                 ttUser.setAccountCredits(ttUser.getAccountCredits().add(openBoxBeanTotal));
    //         //                 userService.updateById(ttUser);
    //         //                 // 加入mq（推广奖励）
    //         //                 if (!ObjectUtil.isEmpty(ttUser.getParentId())){
    //         //                     pWelfareMQData mqData = pWelfareMQData.builder()
    //         //                             .userId(ttUser.getUserId())
    //         //                             .account(openBoxBeanTotal)
    //         //                             .createTime(new Timestamp(System.currentTimeMillis()))
    //         //                             .build();
    //         //                     rabbitTemplate.convertAndSend(PROMOTION_WELFARE_EXCHANGE.getValue(),PROMOTION_WELFARE_KEY1.getValue(),mqData);
    //         //                 }
    //         //                 if (fightUserList.isEmpty()) {
    //         //                     userService.insertUserAmountRecords(ttUser.getUserId(), "1", "2", openBoxBeanTotal.negate(), ttUser.getAccountAmount());
    //         //                     userService.insertUserCreditsRecords(ttUser.getUserId(), "2", "2", openBoxBeanTotal, ttUser.getAccountCredits());
    //         //                 } else {
    //         //                     userService.insertUserAmountRecords(ttUser.getUserId(), "1", "3", openBoxBeanTotal.negate(), ttUser.getAccountAmount());
    //         //                     userService.insertUserCreditsRecords(ttUser.getUserId(), "2", "3", openBoxBeanTotal, ttUser.getAccountCredits());
    //         //                 }
    //         //                 CompletableFuture.runAsync(() -> openBox(ttFight, ttUser), customThreadPoolExecutor);
    //         //                 if (fightUserList.size() + 1 == ttFight.getPlayerNum())
    //         //                     CompletableFuture.runAsync(() -> sendResult(ttFight,rounds), customThreadPoolExecutor);
    //         //                 return R.ok("加入对战成功！");
    //         //             }
    //         //             return R.fail("数据异常！");
    //         //         } else return R.fail("数据异常！");
    //         //     } catch (Exception e) {
    //         //         return R.fail("数据异常！");
    //         //     } finally {
    //         //         redisLock.unlock(RedisConstants.JOIN_FIGHT_LOCK + fightId);
    //         //     }
    //         // } else {
    //         //     try {
    //         //         TimeUnit.SECONDS.sleep(1L);
    //         //     } catch (Exception e) {
    //         //         return R.fail("数据异常！");
    //         //     }
    //         // }
    //     }
    //
    // }

    @Override
    public List<TtBoxVO> getFightBoxList(Integer boxTypeId) {

        List<TtBoxA> boxList = apiFightMapper.getFightBoxList(boxTypeId);

        // 补充物品信息
        // for (TtBoxA box : boxList){
        //     List<TtBoxOrnaments> list = new LambdaQueryChainWrapper<>(boxOrnamentsMapper)
        //             .eq(TtBoxOrnaments::getBoxId, box.getBoxId())
        //             .list();
        // }

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

        return apiBindBoxService.groupByBoxType(collect, 1);
    }

    @Override
    public List<ApiFightListDataVO> getFightList(String model, String status, Integer userId, Integer fightId) {
        return apiFightMapper.getFightList(model, status, userId, fightId);
    }

    @Override
    public FightResultDataVO getFightRecord(Integer fightId, Integer round, Integer rounds) {
        FightResultDataVO fightResultDataVO = new FightResultDataVO();
        fightResultDataVO.setFightId(fightId);

        TtFightResult ttFightResult = fightResultMapper.selectOne(new QueryWrapper<TtFightResult>()
                .eq("fight_id", fightId));
        if (ObjectUtil.isEmpty(ttFightResult)) return null;
        // 对局正式开始时间 - 客户端获取本回合结果时间
        long hhsj = new Date().getTime() - ttFightResult.getCreateTime().getTime();

        if (round != null) {
            fightResultDataVO.setRound(round);
        } else {
            // 当前回合数
            round = (int) hhsj / 6000 + 1;
            if (round > rounds) {
                // 时间已过，房间关闭
                return fightResultDataVO;
            } else {
                fightResultDataVO.setRound(round);
            }
        }
        List<PlayerGainsOrnamentsDataVO> playerGainsOrnamentsDataVOList = new ArrayList<>();

        // 计算当前回合数奖品
        List<TtFightUser> list = fightUserMapper
                .selectList(new QueryWrapper<TtFightUser>()
                        .select("fight_id", "user_id", "join_seat_num")
                        .eq("fight_id", fightId)
                        .groupBy("fight_id", "user_id", "join_seat_num"));

        for (int i = 0; i < list.size(); i++) {

            PlayerGainsOrnamentsDataVO playerGainsOrnamentsDataVO = new PlayerGainsOrnamentsDataVO();
            // 获取奖品信息
            List<TtBoxRecords> ttBoxRecords = ttBoxRecordsService.list(new QueryWrapper<TtBoxRecords>()
                    .eq("user_id", list.get(i).getUserId())
                    .eq("fight_id", list.get(i).getFightId()));
            TtOrnament ttOrnament = ttOrnamentsZBTService.getById(ttBoxRecords.get(round - 1).getOrnamentId());

            playerGainsOrnamentsDataVO.setUserId(ttBoxRecords.get(round - 1).getUserId());
            playerGainsOrnamentsDataVO.setJoinSeatNum(list.get(i).getJoinSeatNum());

            UserPackSackDataVO userPackSackDataVO = new UserPackSackDataVO();
            userPackSackDataVO.setImageUrl(ttOrnament.getImageUrl());
            userPackSackDataVO.setOrnamentsPrice(ttOrnament.getPrice());
            userPackSackDataVO.setItemName(ttOrnament.getName());
            userPackSackDataVO.setShortName(ttOrnament.getShortName());

            if (ttOrnament.getQuality() != null) ttOrnament.setQuality("1");

            TtOrnamentsLevel ttOrnamentsLevel = ttOrnamentsLevelService.getById(ttOrnament.getQuality());
            userPackSackDataVO.setLevelImg(ttOrnamentsLevel.getLevelImg());

            playerGainsOrnamentsDataVO.setOrnamentsData(userPackSackDataVO);

            playerGainsOrnamentsDataVOList.add(playerGainsOrnamentsDataVO);
        }
        fightResultDataVO.setPlayerGainsOrnamentsData(playerGainsOrnamentsDataVOList);

        return fightResultDataVO;
    }

    private void sendResult(TtFight ttFight, Integer rounds) {

        log.info("sendResult");
        log.info("对局信息 " + ttFight.getPlayerNum());

        int num = 25;
        while (num > 0) {
            try {
                List<TtBoxRecords> ttBoxRecordsList = new LambdaQueryChainWrapper<>(boxRecordsService.getBaseMapper())
                        .eq(TtBoxRecords::getStatus, "4")
                        .eq(TtBoxRecords::getSource, "1")
                        .eq(TtBoxRecords::getFightId, ttFight.getId())
                        .list();

                Map<String, FightBoxVO> boxDataJsonString = ttFight.getBoxDataMap();

                if (StringUtils.isEmpty(boxDataJsonString)) return;

                List<BoxDataBodyA> boxDataList = JSONObject.parseObject(JSON.toJSONString(boxDataJsonString), new TypeReference<List<BoxDataBodyA>>() {
                });

                int boxNumTotal = boxDataList.stream().mapToInt(BoxDataBodyA::getBoxNum).sum();

                // 等待开箱结果
                if (boxNumTotal * ttFight.getPlayerNum() > ttBoxRecordsList.size()) {
                    log.info("等待开箱结果 " + ttBoxRecordsList.size());
                    // TimeUnit.MILLISECONDS.sleep(1000);
                    TimeUnit.MILLISECONDS.sleep(400);
                    // TimeUnit.MILLISECONDS.sleep(100);
                    num--;
                    continue;
                }

                Map<Integer, List<TtBoxRecords>> boxRecordsGroupingByUserId = ttBoxRecordsList.stream().collect(Collectors.groupingBy(TtBoxRecords::getUserId));
                List<PlayerGainsOrnamentsDataVO> playerGainsOrnamentsDataList = new ArrayList<>();
                for (Integer userId : boxRecordsGroupingByUserId.keySet()) {
                    PlayerGainsOrnamentsDataVO playerGainsOrnamentsData = getPlayerGainsOrnamentsData(userId, ttFight.getId());
                    playerGainsOrnamentsDataList.add(playerGainsOrnamentsData);
                }
                FightResultDataVO fightResultData = new FightResultDataVO();
                fightResultData.setFightId(ttFight.getId());
                fightResultData.setPlayerGainsOrnamentsData(playerGainsOrnamentsDataList);
                List<PlayerGainsOrnamentsDataVO> winnerGainsOrnamentsData = allotResult(boxRecordsGroupingByUserId, ttFight);
                fightResultData.setWinnerGainsOrnamentsData(winnerGainsOrnamentsData);
                new LambdaUpdateChainWrapper<>(fightMapper).eq(TtFight::getId, ttFight.getId()).set(TtFight::getStatus, "1")
                        .set(TtFight::getUpdateTime, new Date()).update();

                // 关闭对战房间
                TtFightResult ttFightResult = fightResultMapper.selectOne(new QueryWrapper<TtFightResult>().eq("fight_id", ttFight.getId()));
                if (ttFightResult != null) {

                    // rabbitTemplate.convertAndSend(FightQueueConfig.FIGHT_EXCHANGE, FightQueueConfig.FIGHT_ROUTING_KEY, ttFight.getId(),
                    //         message -> {
                    //             // 注意这里时间可以使long，而且是设置header
                    //             message.getMessageProperties().setDelay((rounds * 6000));
                    //             return message;
                    //         }
                    // );

                    log.info(ttFight.getId() + "将在{}ms后执行", rounds * 6000);
//                rabbitProduc.sendDelayMessage(ttRoll.getId().intValue() + "", difference);

                }


                new LambdaUpdateChainWrapper<>(fightUserMapper).eq(TtFightUser::getFightId, ttFight.getId()).set(TtFightUser::getStatus, "2")
                        .set(TtFightUser::getUpdateTime, new Date()).update();
                ResultData<FightResultDataVO> resultData = new ResultData<>();
                resultData.setCode(1);
                resultData.setTypeName("success");
                resultData.setData(fightResultData);
                WebSocketUsers.sendMessageToUsersByText(JSON.toJSONString(resultData));
                TtFightResult fightResult = TtFightResult.builder().fightId(ttFight.getId()).createTime(new Date()).fightResult(JSON.toJSONString(fightResultData)).build();

                log.info("=====================保存对战房间" + ttFight.getId() + "结果===================");
                fightResultMapper.insert(fightResult);
                return;
            } catch (Exception e) {
                return;
            }
        }
    }

    private List<PlayerGainsOrnamentsDataVO> allotResult(Map<Integer, List<TtBoxRecords>> boxRecordsGroupingByUserId, TtFight ttFight) {
        Map<Integer, Map<String, String>> dataA = new HashMap<>();
        for (Map.Entry<Integer, List<TtBoxRecords>> entry : boxRecordsGroupingByUserId.entrySet()) {
            Integer userId = entry.getKey();
            List<TtBoxRecords> boxRecordsList = entry.getValue();
            TtFightUser ttFightUser = new LambdaQueryChainWrapper<>(fightUserMapper).eq(TtFightUser::getFightId, ttFight.getId())
                    .eq(TtFightUser::getUserId, userId).eq(TtFightUser::getStatus, "0").one();
            PlayerGainsOrnamentsDataVO playerGainsOrnamentsData = getPlayerGainsOrnamentsData(userId, ttFight.getId());
            Map<String, String> data = new HashMap<>();
            BigDecimal reduce = boxRecordsList.stream().map(TtBoxRecords::getOrnamentsPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            data.put("priceTotal", JSON.toJSONString(reduce));
            data.put("userId", JSON.toJSONString(userId));
            data.put("playerGainsOrnamentsData", JSON.toJSONString(playerGainsOrnamentsData));
            data.put("boxRecordsList", JSON.toJSONString(boxRecordsList));
            dataA.put(ttFightUser.getJoinSeatNum(), data);
        }
        Map<Integer, BigDecimal> balanceMap = dataA.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> JSONObject
                .parseObject(entry.getValue().get("priceTotal"), new TypeReference<BigDecimal>() {
                })));
        BigDecimal balanceA = balanceMap.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        List<Integer> maxJoinSeatNumList = balanceMap.entrySet().stream().filter(entry -> entry.getValue().equals(balanceA))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        BigDecimal balanceB = balanceMap.values().stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        List<Integer> minJoinSeatNumList = balanceMap.entrySet().stream().filter(entry -> entry.getValue().equals(balanceB))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        Map<Integer, Map<String, String>> collectA = dataA.entrySet().stream().filter(entry -> !maxJoinSeatNumList.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<Integer, Map<String, String>> collectB = dataA.entrySet().stream().filter(entry -> !minJoinSeatNumList.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if ("0".equals(ttFight.getModel())) {
            return singlePlayer(ttFight, dataA, maxJoinSeatNumList, collectA);
        } else if ("1".equals(ttFight.getModel())) {
            return singlePlayer(ttFight, dataA, minJoinSeatNumList, collectB);
        } else if ("2".equals(ttFight.getModel())) {
            if (ttFight.getPlayerNum() == 4 && dataA.size() == 4) {
                return null;
            }
        }
        return null;
    }

    private List<PlayerGainsOrnamentsDataVO> singlePlayer(TtFight ttFight, Map<Integer, Map<String, String>> dataTabulate,
                                                          List<Integer> joinSeatNumList, Map<Integer, Map<String, String>> collect) {
        if (ttFight.getPlayerNum() == 2 && dataTabulate.size() == 2) {
            if (joinSeatNumList.size() == 1) return onePersonWin(ttFight, dataTabulate, joinSeatNumList, collect);
            else if (joinSeatNumList.size() == 2) return dogFall(ttFight, dataTabulate, joinSeatNumList);
        } else if (ttFight.getPlayerNum() == 3 && dataTabulate.size() == 3) {
            if (joinSeatNumList.size() == 1) return onePersonWin(ttFight, dataTabulate, joinSeatNumList, collect);
            else if (joinSeatNumList.size() == 2) {
                List<PlayerGainsOrnamentsDataVO> playerGainsOrnamentsDataVOList = dogFall(ttFight, dataTabulate, joinSeatNumList);
                divideEquallyLosePrice(collect, playerGainsOrnamentsDataVOList);
                return playerGainsOrnamentsDataVOList;
            } else if (joinSeatNumList.size() == 3) return dogFall(ttFight, dataTabulate, joinSeatNumList);
        } else if (ttFight.getPlayerNum() == 4 && dataTabulate.size() == 4) {
            if (joinSeatNumList.size() == 1) return onePersonWin(ttFight, dataTabulate, joinSeatNumList, collect);
            else if (joinSeatNumList.size() == 2) {
                List<PlayerGainsOrnamentsDataVO> playerGainsOrnamentsDataVOList = dogFall(ttFight, dataTabulate, joinSeatNumList);
                List<BigDecimal> losePriceList = collect.values().stream()
                        .map(stringStringMap -> JSONObject.parseObject(stringStringMap.get("priceTotal"), new TypeReference<BigDecimal>() {
                        }))
                        .collect(Collectors.toList());
                if (losePriceList.get(0).compareTo(losePriceList.get(1)) == 0) {
                    List<PlayerGainsOrnamentsDataVO> playerGainsOrnamentsData = collect.values().stream()
                            .map(stringStringMap -> JSONObject.parseObject(stringStringMap.get("playerGainsOrnamentsData"),
                                    new TypeReference<PlayerGainsOrnamentsDataVO>() {
                                    }))
                            .collect(Collectors.toList());
                    for (int i = 0; i < playerGainsOrnamentsData.size(); i++) {
                        playerGainsOrnamentsDataVOList.get(i).getOrnamentsDataList().addAll(playerGainsOrnamentsData.get(i).getOrnamentsDataList());
                        new LambdaUpdateChainWrapper<>(boxRecordsService.getBaseMapper()).eq(TtBoxRecords::getFightId, ttFight.getId())
                                .eq(TtBoxRecords::getUserId, playerGainsOrnamentsData.get(i).getUserId()).set(TtBoxRecords::getStatus, "0")
                                .set(TtBoxRecords::getUpdateTime, new Date()).set(TtBoxRecords::getHolderUserId, playerGainsOrnamentsDataVOList.get(i)
                                        .getUserId()).update();
                    }
                } else {
                    divideEquallyLosePrice(collect, playerGainsOrnamentsDataVOList);
                }
                return playerGainsOrnamentsDataVOList;
            } else if (joinSeatNumList.size() == 3) {
                List<PlayerGainsOrnamentsDataVO> playerGainsOrnamentsDataVOList = dogFall(ttFight, dataTabulate, joinSeatNumList);
                divideEquallyLosePrice(collect, playerGainsOrnamentsDataVOList);
                return playerGainsOrnamentsDataVOList;
            } else if (joinSeatNumList.size() == 4) return dogFall(ttFight, dataTabulate, joinSeatNumList);
        }
        return null;
    }

    private void divideEquallyLosePrice(Map<Integer, Map<String, String>> collect,
                                        List<PlayerGainsOrnamentsDataVO> playerGainsOrnamentsDataVOList) {
        BigDecimal reduce = collect.values().stream().map(stringStringMap -> JSONObject.parseObject(stringStringMap
                .get("priceTotal"), new TypeReference<BigDecimal>() {
        })).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal divide = reduce.divide(new BigDecimal(playerGainsOrnamentsDataVOList.size()), 2, RoundingMode.HALF_UP);
        for (PlayerGainsOrnamentsDataVO playerGainsOrnamentsDataVO : playerGainsOrnamentsDataVOList) {
            new LambdaUpdateChainWrapper<>(userService.getBaseMapper()).eq(TtUser::getUserId, playerGainsOrnamentsDataVO.getUserId())
                    .setSql("`account_amount` = `account_amount` + " + divide).update();
            TtUser user = userService.getById(playerGainsOrnamentsDataVO.getUserId());
            userService.insertUserAmountRecords(playerGainsOrnamentsDataVO.getUserId(), TtAccountRecordType.INPUT, TtAccountRecordSource.GAME_TYPE_02, divide, user.getAccountAmount());
        }
        List<TtBoxRecords> boxRecordsList = new ArrayList<>();
        for (Map<String, String> value : collect.values()) {
            boxRecordsList.addAll(JSONObject.parseObject(value.get("boxRecordsList"), new TypeReference<List<TtBoxRecords>>() {
            }));
        }
        boxRecordsList = boxRecordsList.stream().peek(ttBoxRecords -> {
            ttBoxRecords.setStatus(RESOLVE.getCode());
            ttBoxRecords.setUpdateTime(new Date());
        }).collect(Collectors.toList());
        boxRecordsService.updateBatchById(boxRecordsList, 1);
    }

    private List<PlayerGainsOrnamentsDataVO> dogFall(TtFight ttFight,
                                                     Map<Integer, Map<String, String>> dataTabulate,
                                                     List<Integer> joinSeatNumList) {
        List<PlayerGainsOrnamentsDataVO> result = new ArrayList<>();
        for (Integer i : joinSeatNumList) {
            PlayerGainsOrnamentsDataVO playerGainsOrnamentsData = JSONObject.parseObject(
                    dataTabulate.get(i).get("playerGainsOrnamentsData"),
                    new TypeReference<PlayerGainsOrnamentsDataVO>() {
                    });
            result.add(playerGainsOrnamentsData);
            new LambdaUpdateChainWrapper<>(boxRecordsService.getBaseMapper()).eq(TtBoxRecords::getFightId, ttFight.getId())
                    .eq(TtBoxRecords::getUserId, playerGainsOrnamentsData.getUserId()).set(TtBoxRecords::getStatus, "0")
                    .set(TtBoxRecords::getUpdateTime, new Date()).set(TtBoxRecords::getHolderUserId, playerGainsOrnamentsData.getUserId()).update();
        }
        return result;
    }

    private List<PlayerGainsOrnamentsDataVO> onePersonWin(TtFight ttFight,
                                                          Map<Integer, Map<String, String>> dataTabulate,
                                                          List<Integer> joinSeatNumList,
                                                          Map<Integer, Map<String, String>> collect) {
        List<PlayerGainsOrnamentsDataVO> result = new ArrayList<>();
        List<UserPackSackDataVO> losePackSackDataList = new ArrayList<>();
        List<List<UserPackSackDataVO>> loseOrnamentsDataList = collect.values().stream().map(stringStringMap -> JSONObject.parseObject(
                        stringStringMap.get("playerGainsOrnamentsData"), new TypeReference<PlayerGainsOrnamentsDataVO>() {
                        }).getOrnamentsDataList())
                .collect(Collectors.toList());
        for (List<UserPackSackDataVO> loseOrnamentsData : loseOrnamentsDataList) {
            losePackSackDataList.addAll(loseOrnamentsData);
        }
        PlayerGainsOrnamentsDataVO playerGainsOrnamentsData = JSONObject.parseObject(dataTabulate.get(
                        joinSeatNumList.get(0)).get("playerGainsOrnamentsData"),
                new TypeReference<PlayerGainsOrnamentsDataVO>() {
                });
        playerGainsOrnamentsData.getOrnamentsDataList().addAll(losePackSackDataList);
        result.add(playerGainsOrnamentsData);
        for (Map.Entry<Integer, Map<String, String>> entry : dataTabulate.entrySet()) {
            Integer userId = JSONObject.parseObject(entry.getValue().get("userId"), new TypeReference<Integer>() {
            });
            new LambdaUpdateChainWrapper<>(boxRecordsService.getBaseMapper()).eq(TtBoxRecords::getFightId, ttFight.getId())
                    .eq(TtBoxRecords::getUserId, userId).set(TtBoxRecords::getStatus, "0").set(TtBoxRecords::getUpdateTime, new Date())
                    .set(TtBoxRecords::getHolderUserId, playerGainsOrnamentsData.getUserId()).update();
        }
        return result;
    }

    private PlayerGainsOrnamentsDataVO getPlayerGainsOrnamentsData(Integer userId, Integer fightId) {
        TtUser ttUser = new LambdaQueryChainWrapper<>(userService.getBaseMapper()).eq(TtUser::getUserId, userId).one();
        TtFightUser ttFightUser = new LambdaQueryChainWrapper<>(fightUserMapper).eq(TtFightUser::getFightId, fightId)
                .eq(TtFightUser::getUserId, userId).eq(TtFightUser::getStatus, "0").one();
        List<UserPackSackDataVO> ornamentsDataList = apiFightMapper.selectOrnamentsDataListByUserIdAndFightId(userId, fightId);
        PlayerGainsOrnamentsDataVO playerGainsOrnamentsData = new PlayerGainsOrnamentsDataVO();
        playerGainsOrnamentsData.setUserId(userId);
        playerGainsOrnamentsData.setUserName(ttUser.getUserName());
        playerGainsOrnamentsData.setNickName(ttUser.getNickName());
        playerGainsOrnamentsData.setAvatar(ttUser.getAvatar());
        playerGainsOrnamentsData.setOrnamentsDataList(ornamentsDataList);
        playerGainsOrnamentsData.setJoinSeatNum(ttFightUser.getJoinSeatNum());
        return playerGainsOrnamentsData;
    }

    private void openBox(TtFight ttFight, TtUser ttUser) {
        log.info("开箱");
        String boxDataJsonString = JSON.toJSONString(ttFight.getBoxData());
        List<BoxDataBodyA> boxDataList = JSONObject.parseObject(boxDataJsonString, new TypeReference<List<BoxDataBodyA>>() {
        });
        List<TtBoxRecords> boxRecordsList = new ArrayList<>();

        for (BoxDataBodyA boxData : boxDataList) {
            TtBox ttBox = new LambdaQueryChainWrapper<>(boxMapper)
                    .eq(TtBox::getBoxId, boxData.getBoxId())
                    .eq(TtBox::getIsFight, "0")
                    .eq(TtBox::getStatus, "0")
                    .one();
            if (StringUtils.isNull(ttBox)) throw new RuntimeException("数据异常！");

            // 计算开箱结果、保存开箱记录、返回开箱记录
            List<TtBoxRecords> ttBoxRecords = apiBindBoxService.addBoxRecord(ttUser, ttBox, boxData.getBoxNum());
            boxRecordsList.addAll(ttBoxRecords);

            // 统计箱子的历史开箱次数
            ttBox.setOpenNum(ttBox.getOpenNum() + boxData.getBoxNum());
            boxMapper.updateById(ttBox);
        }

        if (boxRecordsList.isEmpty()) throw new RuntimeException("数据异常！");

        boxRecordsList = boxRecordsList.stream().peek(ttBoxRecords -> {
            ttBoxRecords.setStatus(IN_PACKSACK_ON.getCode());
            ttBoxRecords.setSource(TtboxRecordSource.FIGHT.getCode());
            ttBoxRecords.setFightId(ttFight.getId());
            ttBoxRecords.setHolderUserId(ttUser.getUserId());
        }).collect(Collectors.toList());
        log.info("更新boxRecord状态为4");
        boxRecordsService.updateBatchById(boxRecordsList, 1);
    }
}
