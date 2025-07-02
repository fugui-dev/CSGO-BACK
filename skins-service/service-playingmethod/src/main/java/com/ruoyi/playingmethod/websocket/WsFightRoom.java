package com.ruoyi.playingmethod.websocket;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.admin.mapper.TtBoxOrnamentsMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.fight.FightSeat;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.other.FightBoutData;
import com.ruoyi.domain.vo.TtBoxOrnamentsDataVO;
import com.ruoyi.domain.vo.fight.FightBoxVO;
import com.ruoyi.domain.vo.fight.FightResultVO;
import com.ruoyi.playingmethod.service.ApiFightService;
import com.ruoyi.playingmethod.service.ApiFightUserService;
import com.ruoyi.playingmethod.websocket.constant.SMsgKey;
import com.ruoyi.playingmethod.websocket.util.WsResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对战房间
 */
@Slf4j
@Component
@ServerEndpoint("/ws/fight/room/{userId}/{fightId}")
public class WsFightRoom {

    static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    static volatile AtomicInteger viewersCount = new AtomicInteger(0);

    // 用来记录当前连接数的变量
    private static volatile int onlineCount = 0;
    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    public static ConcurrentHashMap<String, WsFightRoom> allRoomUserMap = new ConcurrentHashMap<>();
    // 与某个客户端的连接会话，需要通过它来与客户端进行数据收发
    private Session session;
    private static ApiFightService apiFightService;
    private static TtBoxRecordsService boxRecordsService;
    private static TtBoxOrnamentsMapper boxOrnamentsMapper;
    // private static TtBoxOrnamentsService ttBoxOrnamentsService;
    private static ApiFightUserService apiFightUserService;
    private static Integer fightRoundTime = null; // 战斗回合时间
    private Integer userId = null;
    private Integer fightId = null;
    private String key = "";
    private static RedisCache redisCache;

    // 心跳机制相关变量
    private ScheduledExecutorService heartBeatExecutor;
    private volatile long lastPongTime; // 添加volatile修饰符保证可见性
    private static final long HEART_BEAT_INTERVAL = 30 * 1000; // 心跳间隔30秒
    private static final long HEART_BEAT_TIMEOUT = 60 * 1000; // 超时时间60秒

    @Value("${mkcsgo.fight.roundTime}")
    public void ttFightService(Integer fightRoundTime) {
        WsFightRoom.fightRoundTime = fightRoundTime;
    }

    @Autowired
    public void ttFightService(ApiFightService apiFightService) {
        WsFightRoom.apiFightService = apiFightService;
    }

    @Autowired
    public void boxOrnamentsMapper(TtBoxOrnamentsMapper boxOrnamentsMapper) {
        WsFightRoom.boxOrnamentsMapper = boxOrnamentsMapper;
    }

    @Autowired
    public void boxRecordsService(TtBoxRecordsService boxRecordsService) {
        WsFightRoom.boxRecordsService = boxRecordsService;
    }

    public void apiFightUserService(ApiFightUserService apiFightUserService) {
        WsFightRoom.apiFightUserService = apiFightUserService;
    }

    @Autowired
    public void redisCache(RedisCache redisCache) {
        WsFightRoom.redisCache = redisCache;
    }

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("userId") Integer userId,
                       @PathParam("fightId") Integer fightId) {
        try {
            addFightRoomUser(userId, fightId, session);

            R check = connectCheck(userId, fightId);
            if (!check.getCode().equals(200)) {
                session.getBasicRemote().sendText(check.getMsg());
                session.close();
                return;
            }

            log.debug("/ws/fight/room > > onOpen");
            log.info("用户{}进入房间{}，" + "在线人数{}", userId, fightId, WsFightRoom.onlineCount);
            sendMsgToPlayers("用户：" + userId + "进入房间，" + fightId + "在线人数" + WsFightRoom.onlineCount, null);

            // 初始化心跳机制
            // 使用命名线程池，便于问题排查
            heartBeatExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName("fight-heartbeat-" + userId + "-" + fightId);
                thread.setDaemon(true); // 设置为守护线程，避免影响JVM退出
                return thread;
            });
            lastPongTime = System.currentTimeMillis();

            // 定时发送ping和检查超时
            heartBeatExecutor.scheduleAtFixedRate(() -> {
                try {
                    if (session == null || !session.isOpen()) {
                        log.warn("会话已关闭，取消心跳任务");
                        heartBeatExecutor.shutdownNow();
                        return;
                    }
                    
                    long now = System.currentTimeMillis();
                    if (now - lastPongTime > HEART_BEAT_TIMEOUT) {
                        log.info("用户{} 对战{} 心跳超时，准备关闭连接", userId, fightId);
                        // 超时，关闭连接
                        try {
                            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Heartbeat timeout"));
                        } catch (IOException e) {
                            log.error("关闭超时连接失败", e);
                        }
                        heartBeatExecutor.shutdownNow();
                    } else {
                        // 发送ping
                        session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[0]));
                        log.debug("发送ping到用户{} 对战{}", userId, fightId);
                    }
                } catch (Exception e) {
                    log.error("用户{} 对战{} 心跳机制异常", userId, fightId, e);
                    // 发生异常时关闭线程池
                    if (heartBeatExecutor != null) {
                        heartBeatExecutor.shutdownNow();
                    }
                }
            }, 0, HEART_BEAT_INTERVAL, TimeUnit.MILLISECONDS);

            // 首次连接获取房间最新数据
            LambdaQueryWrapper<TtFight> fightQuery = new LambdaQueryWrapper<>();
            fightQuery
                    .eq(TtFight::getId, fightId);
            // .eq(TtFight::getStatus,0);
            TtFight fight = apiFightService.getOne(fightQuery);
            // 如果游戏结束，发送结果集数据
            if (fight.getStatus().equals(2) || fight.getStatus().equals(3)) {
                // 构建结果集
                // 根据宝箱id查询关联的所有饰品
                Map<String, FightBoxVO> boxData = fight.getBoxDataMap();
                ArrayList<FightBoxVO> fightBoxVOList = new ArrayList<>();
                boxData.keySet().forEach(boxId -> {
                    fightBoxVOList.add(boxData.get(boxId));
                });

                // 所有对战结果记录
                LambdaQueryWrapper<TtBoxRecords> boxRecordsQuery = new LambdaQueryWrapper<>();
                boxRecordsQuery
                        .eq(TtBoxRecords::getFightId, fightId);
                List<TtBoxRecords> allBoxRecords = boxRecordsService.list(boxRecordsQuery);

                FightResultVO resultVO = FightResultVO.builder()
                        .currentRound(-1L)
                        .fight(fight)
                        .winnerIds(fight.getWinnerList())
                        .fightResult(allBoxRecords)
                        .fightBoxVOList(fightBoxVOList)
                        .build();

                broadcastFight(fightId, WsResult.ok(SMsgKey.FIGHT_RESULT.name(), resultVO, "对局已结束,对局最新信息"));
                log.info("room onOpen 广播数据成功。");

            } else if (fight.getStatus().equals(1)) {

                // 构建结果集
                // 根据宝箱id查询关联的所有饰品
                Map<String, FightBoxVO> boxData = fight.getBoxDataMap();

                ArrayList<FightBoxVO> fightBoxVOList = new ArrayList<>();
                boxData.keySet().forEach(boxId -> {
                    List<TtBoxOrnamentsDataVO> boxOrnamentsVOS = boxOrnamentsMapper.selectTtBoxOrnamentsList(Integer.valueOf(boxId));
                    FightBoxVO fightBoxVO = JSONUtil.toBean(JSONUtil.toJsonStr(boxData.get(boxId)), FightBoxVO.class);
                    fightBoxVO.setOrnaments(boxOrnamentsVOS);
                    boxData.put(boxId,fightBoxVO);
                    fightBoxVOList.add(boxData.get(boxId));
                });

                // 所有对战结果记录
                LambdaQueryWrapper<TtBoxRecords> boxRecordsQuery = new LambdaQueryWrapper<>();
                boxRecordsQuery
                        .eq(TtBoxRecords::getFightId, fightId)
                        .eq(TtBoxRecords::getStatus, 0);
                // 首先从Redis中查询对战结果，没有则在从数据库中查询
                List<TtBoxRecords> allBoxRecords;
                String key = "fight_result:fight_" + fightId;
                allBoxRecords = redisCache.getCacheObject(key);

                if (Objects.isNull(allBoxRecords)) {
                    allBoxRecords = boxRecordsService.list(boxRecordsQuery);
                }

                // 时间差，计算当前进行到第几回合
                // LocalDateTime now = LocalDateTime.now();
                // Timestamp beginTime = fight.getBeginTime();
                // LocalDateTime beginTime1 = beginTime.toLocalDateTime();
                // Duration duration = Duration.between(beginTime1, now);
                // Long currentRound = duration.toMillis() / fightRoundTime; // 这个秒要和前端的【每回合时间】同步

                // 获取当前回合数
                Integer currentRound = 0;
                FightBoutData fightBoutData = redisCache.getCacheObject("fight_bout_data:fight_" + fightId);
                if (!Objects.isNull(fightBoutData)) {
                    currentRound = fightBoutData.getBoutNum();
                }

                FightResultVO resultVO = FightResultVO.builder()
                        .currentRound(currentRound.longValue())
                        .fight(fight)
                        .winnerIds(fight.getWinnerList())
                        .fightResult(allBoxRecords)
                        .fightBoxVOList(fightBoxVOList)
                        .build();

                // broadcastFight(fightId, WsResult.ok(SMsgKey.FIGHT_RESULT.name(), resultVO, "对局进行中,对局最新信息"));
                sendMsgToPlayers(WsResult.ok(SMsgKey.FIGHT_RESULT.name(), resultVO, "对局进行中,对局最新信息"), null);

                log.info("room onOpen 广播数据成功。");
            } else if (fight.getStatus().equals(0)) {
                // 没有结束，返回房间信息即可
                sendMsgToPlayers(WsResult.ok(SMsgKey.FIGHT_ROOM_INFO.name(), fight, "对局准备中，对战房间最新信息"), null);
                log.info("room onOpen 广播数据成功。");
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.warn("onopen warn");
        }
    }

    @OnClose
    public void onClose(Session session) {
        removeFightRoomUser();
        log.info("onClose");
        log.info("正常在线人数：" + WsFightRoom.onlineCount);
    }

    @OnError
    public void onError(Session session, Throwable exception) throws Exception {
        log.info("onError");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("onMessage");
    }

    /**
     * 连接检查
     */
    private R connectCheck(Integer userId, Integer fightId) {
        if (ObjectUtil.isNotEmpty(WsFightRoom.allRoomUserMap.get(userId + "_" + fightId))) {
            R.fail("用户" + userId + "已经连接对局" + fightId + "。请勿重复调用。");
        }
        TtFight fight = apiFightService.getById(fightId);
        if (ObjectUtil.isEmpty(fight)) {
            return R.fail("不存在的对局ID：" + fightId);
        }
        // if (fight.getStatus().equals(2) || fight.getStatus().equals(3)){
        //     return R.fail("对局"+fightId+"已结束。");
        // }
        return R.ok();
    }

    /**
     * 更新在线人数
     */
    private int addOnlineCount(int number) {
        synchronized (new Object()) {
            WsFightRoom.onlineCount = WsFightRoom.onlineCount + number;
            return WsFightRoom.onlineCount;
        }
    }

    /**
     * 单点推送
     */
    public void sendMsgToPlayers(Object message, List<String> keys) {
        String msg = JSON.toJSONString(message);

        try {
            if (ObjectUtil.isEmpty(keys) || keys.size() == 0) {
                // 推送给自己
                this.session.getBasicRemote().sendText(msg);
            } else {
                for (String key : keys) {
                    WsFightRoom ws = WsFightRoom.allRoomUserMap.get(key);
                    ws.session.getBasicRemote().sendText(msg);
                }
            }
        } catch (Exception e) {
            log.warn("推送消息异常。msg:" + msg);
        }
    }

    /**
     * 房间广播
     */
    public static void broadcastFight(Integer fightId, Object message) {
        String msg = JSON.toJSONString(message);

        Collection<WsFightRoom> wslist = WsFightRoom.allRoomUserMap.values();

        for (WsFightRoom ws : wslist) {
            try {
                if (!ws.fightId.equals(fightId)) continue;
                log.info(String.valueOf(ws.session.isOpen()));
                if (!ws.session.isOpen()) continue;
                RemoteEndpoint.Basic basicRemote = ws.session.getBasicRemote();
                basicRemote.sendText(msg);
            } catch (IOException e) {
                log.warn("ws推送广播给{}_{}消息异常。", ws.userId, ws.fightId);
            }
        }
    }

    /**
     * 全局广播
     */
    public static void broadcast(Object message) {

        ObjectMapper objectMapper = new ObjectMapper();
        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.warn("ws解析广播消息异常。");
        }

        Collection<WsFightRoom> wss = WsFightRoom.allRoomUserMap.values();
        for (WsFightRoom ws : wss) {
            try {
                ws.session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                log.warn("ws推送广播给{}_{}消息异常。", ws.userId, ws.fightId);
            }
        }
    }

    /**
     * 添加连接用户
     */
    private WsFightRoom addFightRoomUser(Integer userId, Integer fightId, Session session) {
        addOnlineCount(1);
        this.userId = userId;
        this.fightId = fightId;
        this.key = userId + "_" + fightId;
        this.session = session;
        WsFightRoom.allRoomUserMap.put(key, this);
        return this;
    }

    /**
     * 移除连接用户
     */
    private WsFightRoom removeFightRoomUser() {
        addOnlineCount(-1);
        WsFightRoom.allRoomUserMap.remove(userId + "_" + fightId);
        return this;
    }

    /**
     * 断开房间所有连接
     */
    public static Boolean batchClose(TtFight fight) {
        List<FightSeat> seats = fight.getSeatList();

        for (int i = 0; i < seats.size(); i++) {
            FightSeat seat = JSONUtil.toBean(JSONUtil.toJsonStr(seats.get(i)), FightSeat.class);

            Integer playerId = seat.getPlayerId();
            String key = ObjectUtil.isNotEmpty(playerId) ? String.valueOf(playerId) : "" + "_" + fight.getId();

            WsFightRoom ws = WsFightRoom.allRoomUserMap.get(key);
            if (ObjectUtil.isEmpty(ws)) continue;

            try {
                ws.session.close();
            } catch (IOException e) {
                log.warn("关闭连接异常");
            }
        }
        return true;
    }
}
