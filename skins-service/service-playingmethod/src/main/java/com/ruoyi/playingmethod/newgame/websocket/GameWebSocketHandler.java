package com.ruoyi.playingmethod.newgame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.playingmethod.newgame.constants.GameConstants;
import com.ruoyi.playingmethod.newgame.model.GamePlayer;
import com.ruoyi.playingmethod.newgame.model.GameRoom;
import com.ruoyi.playingmethod.newgame.websocket.message.WsMessage;
import com.ruoyi.playingmethod.newgame.service.GameService;
import com.ruoyi.admin.service.TtBoxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/game/{userId}/{roomId}")
public class GameWebSocketHandler {

    private static ObjectMapper objectMapper;
    private static RedisCache redisCache;
    private static GameService gameService;
    private static TtBoxService boxService;

    // 在线用户的WebSocket连接
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    // 房间观众的WebSocket连接
    private static final Map<String, Set<Session>> roomSpectators = new ConcurrentHashMap<>();
    
    // 当前会话
    private Session session;
    private String userId;
    private String roomId;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        GameWebSocketHandler.objectMapper = objectMapper;
    }

    @Autowired
    public void setRedisCache(RedisCache redisCache) {
        GameWebSocketHandler.redisCache = redisCache;
    }

    @Autowired
    public void setGameService(GameService gameService) {
        GameWebSocketHandler.gameService = gameService;
    }

    @Autowired
    public void setBoxService(TtBoxService boxService) {
        GameWebSocketHandler.boxService = boxService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("roomId") String roomId) {
        this.session = session;
        this.userId = userId;
        this.roomId = roomId;
        
        if (userId != null) {
            // 玩家连接
            userSessions.put(userId, session);
            log.info("玩家{}已连接", userId);
        } else if (roomId != null) {
            // 观众连接
            roomSpectators.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
            log.info("观众加入房间{}", roomId);
        }
    }

    @OnClose
    public void onClose() {
        if (userId != null) {
            userSessions.remove(userId);
            log.info("玩家{}已断开连接", userId);
        }
        
        if (roomId != null) {
            Set<Session> spectators = roomSpectators.get(roomId);
            if (spectators != null) {
                spectators.remove(session);
                if (spectators.isEmpty()) {
                    roomSpectators.remove(roomId);
                }
            }
            log.info("观众离开房间{}", roomId);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            // 解析消息
            WsMessage<?> wsMessage = objectMapper.readValue(message, WsMessage.class);
            handleGameMessage(wsMessage);
        } catch (IOException e) {
            log.error("处理WebSocket消息失败", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误", error);
    }

    private void handleGameMessage(WsMessage<?> message) {
        // 根据消息类型处理不同的游戏消息
        switch (message.getType()) {
            case GameConstants.WS_TYPE_BOX_OPENING:
                handleBoxOpening(message.getRoomId(), userId);
                break;
            // 添加其他消息类型的处理
        }
    }

    // 处理开箱动画
    private void handleBoxOpening(String roomId, String playerId) {
        // 从Redis获取房间信息
        GameRoom room = redisCache.getCacheObject(GameConstants.ROOM_CACHE_KEY + roomId);
        if (room == null || !GameConstants.ROOM_STATUS_PLAYING.equals(room.getStatus())) {
            return;
        }

        // 获取当前回合信息
        int currentRound = room.getCurrentRound();
        GameRoom.BoxConfig currentBox = getCurrentRoundBox(room, currentRound);
        
        // 为每个玩家发送开箱动画消息
        for (Map.Entry<String, GamePlayer> entry : room.getPlayers().entrySet()) {
            GamePlayer player = entry.getValue();
            String ornamentId = room.getPlayerResults().get(player.getUserId()).get(currentRound - 1);
            TtBox box = boxService.getById(currentBox.getBoxId());
            
            if (box == null) {
                continue;
            }

            // 创建开箱消息
            Map<String, Object> boxOpeningMessage = new HashMap<>();
            boxOpeningMessage.put("roomId", roomId);
            boxOpeningMessage.put("playerId", player.getUserId());
            boxOpeningMessage.put("round", currentRound);
            boxOpeningMessage.put("boxId", currentBox.getBoxId());
            boxOpeningMessage.put("ornamentId", ornamentId);
            boxOpeningMessage.put("lastPlayer", isLastPlayer(room, player));
            boxOpeningMessage.put("lastRound", isLastRound(room));
            
            // 发送消息
            broadcastToRoom(room, GameConstants.WS_TYPE_BOX_OPENING, boxOpeningMessage);
        }
    }

    // 获取当前回合的箱子配置
    private GameRoom.BoxConfig getCurrentRoundBox(GameRoom room, int round) {
        int currentCount = 0;
        for (GameRoom.BoxConfig config : room.getBoxConfigs()) {
            currentCount += config.getCount();
            if (round <= currentCount) {
                return config;
            }
        }
        return room.getBoxConfigs().get(room.getBoxConfigs().size() - 1);
    }

    // 检查是否是最后一个开箱的玩家
    private boolean isLastPlayer(GameRoom room, GamePlayer player) {
        int finishedCount = (int) room.getPlayerOpeningStatus().values().stream()
                .filter(Boolean.TRUE::equals)
                .count();
        return finishedCount == room.getPlayers().size() - 1 &&
               !Boolean.TRUE.equals(room.getPlayerOpeningStatus().get(player.getUserId()));
    }

    // 检查是否是最后一轮
    private boolean isLastRound(GameRoom room) {
        return room.getCurrentRound() >= room.getTotalRounds();
    }

    // 广播消息给房间所有成员(包括观众)
    public void broadcastToRoom(GameRoom room, String type, Object data) {
        try {
            WsMessage<?> message = WsMessage.create(type, room.getRoomId(), data);
            String messageJson = objectMapper.writeValueAsString(message);
            
            // 发送给玩家
            for (GamePlayer player : room.getPlayers().values()) {
                Session session = userSessions.get(player.getUserId());
                if (session != null && session.isOpen()) {
                    session.getBasicRemote().sendText(messageJson);
                }
            }
            
            // 发送给观众
            Set<Session> spectators = roomSpectators.getOrDefault(room.getRoomId(), Collections.emptySet());
            for (Session session : spectators) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(messageJson);
                }
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    /**
     * 广播游戏开始消息
     */
    public void broadcastGameStart(GameRoom room) {
        broadcastToRoom(room, GameConstants.WS_TYPE_GAME_START, room);
    }

    /**
     * 广播玩家加入消息
     */
    public void broadcastPlayerJoin(GameRoom room, GamePlayer player) {
        broadcastToRoom(room, GameConstants.WS_TYPE_PLAYER_JOIN, player);
    }

    /**
     * 广播玩家离开消息
     */
    public void broadcastPlayerLeave(GameRoom room, String userId) {
        broadcastToRoom(room, GameConstants.WS_TYPE_PLAYER_LEAVE, userId);
    }

    /**
     * 广播玩家准备消息
     */
    public void broadcastPlayerReady(GameRoom room, GamePlayer player) {
        broadcastToRoom(room, GameConstants.WS_TYPE_PLAYER_READY, player);
    }

    /**
     * 广播游戏结束消息
     */
    public void broadcastGameEnd(GameRoom room) {
        broadcastToRoom(room, GameConstants.WS_TYPE_GAME_END, room);
    }

    /**
     * 广播开箱动画消息
     */
    public void broadcastBoxOpening(GameRoom room, Map<String, Object> boxOpeningMessage) {
        broadcastToRoom(room, GameConstants.WS_TYPE_BOX_OPENING, boxOpeningMessage);
    }

    /**
     * 广播开箱结果消息
     */
    public void broadcastBoxResult(GameRoom room, Map<String, Object> boxResultMessage) {
        broadcastToRoom(room, GameConstants.WS_TYPE_BOX_RESULT, boxResultMessage);
    }
} 