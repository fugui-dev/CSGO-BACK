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

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("roomId") String roomId) {
        this.session = session;
        this.userId = userId;
        this.roomId = roomId;
        
        if (userId != null) {
            // 玩家连接
            userSessions.put(userId, session);
            log.info("玩家{}已连接", userId);
            

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

    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误", error);
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

}

