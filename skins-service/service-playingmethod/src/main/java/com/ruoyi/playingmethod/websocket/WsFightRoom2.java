package com.ruoyi.playingmethod.websocket;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.admin.mapper.TtBoxOrnamentsMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.vo.TtBoxOrnamentsDataVO;
import com.ruoyi.domain.vo.fight.FightBoxVO;
import com.ruoyi.domain.vo.fight.FightResultVO;
import com.ruoyi.playingmethod.service.ApiFightService;
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
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 对战房间
 */
@Slf4j
@ServerEndpoint("/ws/fight/room2/{userId}/{fightId}")
@Component
public class WsFightRoom2 {

    static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    static Map<String, Session> viewersSessionMap = new ConcurrentHashMap<>();

    private String sessionKey;

    private static Integer fightRoundTime;

    private static ApiFightService apiFightService;

    private static TtBoxRecordsService ttBoxRecordsService;

    private static TtBoxOrnamentsMapper ttBoxOrnamentsMapper;

    @Value("${mkcsgo.fight.roundTime}")
    public void setFightRoundTime(Integer fightRoundTime) {
        WsFightRoom2.fightRoundTime = fightRoundTime;
    }

    @Autowired
    public void ttFightService(ApiFightService apiFightService) {
        WsFightRoom2.apiFightService = apiFightService;
    }

    @Autowired
    public void boxRecordsService(TtBoxRecordsService ttBoxRecordsService) {
        WsFightRoom2.ttBoxRecordsService = ttBoxRecordsService;
    }

    @Autowired
    public void boxOrnamentsMapper(TtBoxOrnamentsMapper ttBoxOrnamentsMapper) {
        WsFightRoom2.ttBoxOrnamentsMapper = ttBoxOrnamentsMapper;
    }

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("userId") Integer userId,
                       @PathParam("fightId") Integer fightId) {

        sessionKey = fightId.toString() + "#" + userId.toString();

        // 检查用户是否已经进入某个房间
        if (!Objects.isNull(sessionMap.get(sessionKey))) {
            try {
                session.getBasicRemote().sendText("用户" + userId + "已加入" + fightId +"房间，不得重复进入");
                session.close();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sessionMap.put(sessionKey, session);
        }

        // 首次连接获取房间最新数据
        LambdaQueryWrapper<TtFight> fightQuery = new LambdaQueryWrapper<>();
        fightQuery
                .eq(TtFight::getId, fightId);
        TtFight ttFight = apiFightService.getOne(fightQuery);

        // 检查房间是否存在
        if (Objects.isNull(ttFight)) {
            try {
                session.getBasicRemote().sendText("房间" + fightId + "不存在");
                session.close();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 判断对战状态
        if (ttFight.getStatus() == 2) {
            // 构建结果集
            // 根据宝箱ID查询关联的所有饰品
            Map<String, FightBoxVO> boxData = ttFight.getBoxDataMap();
            ArrayList<FightBoxVO> fightBoxVOList = new ArrayList<>();
            boxData.keySet().forEach(boxId -> {
                fightBoxVOList.add(boxData.get(boxId));
            });

            // 所有对战结果记录
            LambdaQueryWrapper<TtBoxRecords> boxRecordsQuery = new LambdaQueryWrapper<>();
            boxRecordsQuery
                    .eq(TtBoxRecords::getFightId, fightId)
                    .eq(TtBoxRecords::getStatus, 0);
            List<TtBoxRecords> allBoxRecords = ttBoxRecordsService.list(boxRecordsQuery);

            FightResultVO resultVO = FightResultVO.builder()
                    .currentRound(-1L)
                    .fight(ttFight)
                    .winnerIds(ttFight.getWinnerList())
                    .fightResult(allBoxRecords)
                    .fightBoxVOList(fightBoxVOList)
                    .build();

            WsResult<FightResultVO> result = WsResult.ok(SMsgKey.FIGHT_RESULT.name(), resultVO, "对局已结束，对局最新信息");
            String message = JSON.toJSONString(result);
            broadcastMessageToFight(fightId, message);
        } else if (ttFight.getStatus() == 1) {
            // 构建结果集
            // 根据宝箱ID查询关联的所有饰品
            Map<String, FightBoxVO> boxData = ttFight.getBoxDataMap();
            ArrayList<FightBoxVO> fightBoxVOList = new ArrayList<>();
            boxData.keySet().forEach(boxId -> {
                List<TtBoxOrnamentsDataVO> boxOrnamentsVOS = ttBoxOrnamentsMapper.selectTtBoxOrnamentsList(Integer.valueOf(boxId));
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
            List<TtBoxRecords> allBoxRecords = ttBoxRecordsService.list(boxRecordsQuery);

            // 时间差，计算当前进行到第几回合
            LocalDateTime now = LocalDateTime.now();
            Timestamp beginTime = ttFight.getBeginTime();
            LocalDateTime localDateTime = beginTime.toLocalDateTime();

            Duration duration = Duration.between(localDateTime, now);
            Long currentRound = duration.toMillis() / fightRoundTime; // 这个秒要和前端的【每回合时间】同步

            FightResultVO resultVO = FightResultVO.builder()
                    .currentRound(currentRound)
                    .fight(ttFight)
                    .winnerIds(ttFight.getWinnerList())
                    .fightResult(allBoxRecords)
                    .fightBoxVOList(fightBoxVOList)
                    .build();

            WsResult<FightResultVO> result = WsResult.ok(SMsgKey.FIGHT_RESULT.name(), resultVO, "对局进行中，对局最新信息");
            String message = JSON.toJSONString(result);
            broadcastMessageToFight(fightId, message);

            // 存储观战人，广播房间内观战人数
            viewersSessionMap.put(sessionKey, session);
            broadcastViewersCountToFight(fightId);
        } else if (ttFight.getStatus() == 0) {
            WsResult<TtFight> result = WsResult.ok(SMsgKey.FIGHT_ROOM_INFO.name(), ttFight, "对局准备中，对战房间最新信息");
            String message = JSON.toJSONString(result);
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
    }

    @OnClose
    public void onClose(Session session) {
        if (session.equals(sessionMap.get(sessionKey))) {
            sessionMap.remove(sessionKey);
        }
        if (session.equals(viewersSessionMap.get(sessionKey))) {
            viewersSessionMap.remove(sessionKey);
            Integer fightId = Integer.parseInt(sessionKey.substring(0, sessionKey.indexOf('#')));
            broadcastViewersCountToFight(fightId);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
    }

    // 向指定房间的用户广播消息
    private void broadcastMessageToFight(Integer fightId, String message) {
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            String sessionKey = entry.getKey();
            Session session = entry.getValue();
            if (sessionKey.substring(0, sessionKey.indexOf('#')).equals(fightId.toString())) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 向指定房间的用户广播观战人数
    private void broadcastViewersCountToFight(Integer fightId) {
        int viewersCount = 0;
        // 统计本房间的观战人数
        for (Map.Entry<String, Session> entry : viewersSessionMap.entrySet()) {
            String sessionKey = entry.getKey();
            if (sessionKey.substring(0, sessionKey.indexOf('#')).equals(fightId.toString())) {
                viewersCount++;
            }
        }
        // 向该房间所有用户广播
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            String sessionKey = entry.getKey();
            Session session = entry.getValue();
            if (sessionKey.substring(0, sessionKey.indexOf('#')).equals(fightId.toString())) {
                try {
                    Map<String, Object> map = new HashMap<>();
                    map.put("viewersCount", viewersCount);
                    map.put("message", "当前房间观战人数");
                    String message = JSON.toJSONString(map);
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
