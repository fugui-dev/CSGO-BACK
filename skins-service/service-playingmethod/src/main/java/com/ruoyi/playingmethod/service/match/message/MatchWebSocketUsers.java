package com.ruoyi.playingmethod.service.match.message;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.framework.websocket.pojo.ResultData;
import com.ruoyi.playingmethod.model.match.MatchSessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MatchWebSocketUsers {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchWebSocketUsers.class);
    
    // 使用三层Map结构管理会话: matchId -> groupName -> sessionId -> sessionData
    private static final Map<Integer, Map<String, Map<String, MatchSessionData>>> MATCH_SESSIONS = new ConcurrentHashMap<>();

    public static void addUser(MatchSessionData sessionData) {
        Integer matchId = sessionData.getMatchId();
        String groupName = sessionData.getGroupName();
        String sessionId = sessionData.getSession().getId();

        MATCH_SESSIONS.computeIfAbsent(matchId, k -> new ConcurrentHashMap<>())
                     .computeIfAbsent(groupName, k -> new ConcurrentHashMap<>())
                     .put(sessionId, sessionData);
    }

    public static void removeBySessionId(String sessionId) {
        MATCH_SESSIONS.forEach((matchId, groupMap) -> 
            groupMap.forEach((groupName, sessionMap) -> {
                if (sessionMap.containsKey(sessionId)) {
                    sessionMap.remove(sessionId);
                    if (sessionMap.isEmpty()) {
                        groupMap.remove(groupName);
                        if (groupMap.isEmpty()) {
                            MATCH_SESSIONS.remove(matchId);
                        }
                    }
                }
            })
        );
    }

    // 向特定比赛的所有用户广播消息
    public static void broadcastToMatch(Integer matchId, String typeName, Object data) {
        Map<String, Map<String, MatchSessionData>> groupMap = MATCH_SESSIONS.get(matchId);
        if (groupMap != null) {
            ResultData<Object> resultData = createResultData(typeName, data);
            String message = JSON.toJSONString(resultData);
            
            groupMap.values().forEach(sessionMap -> 
                sessionMap.values().forEach(sessionData -> 
                    sendMessage(sessionData.getSession(), message)
                )
            );
        }
    }

    // 向特定比赛的特定分组广播消息
    public static void broadcastToGroup(Integer matchId, String groupName, String typeName, Object data) {
        Map<String, Map<String, MatchSessionData>> groupMap = MATCH_SESSIONS.get(matchId);
        if (groupMap != null) {
            Map<String, MatchSessionData> sessionMap = groupMap.get(groupName);
            if (sessionMap != null) {
                ResultData<Object> resultData = createResultData(typeName, data);
                String message = JSON.toJSONString(resultData);
                
                sessionMap.values().forEach(sessionData -> 
                    sendMessage(sessionData.getSession(), message)
                );
            }
        }
    }

    // 向特定用户发送消息
    public static void sendToUser(Integer matchId, String groupName, Integer userId, String typeName, Object data) {
        Map<String, Map<String, MatchSessionData>> groupMap = MATCH_SESSIONS.get(matchId);
        if (groupMap != null) {
            Map<String, MatchSessionData> sessionMap = groupMap.get(groupName);
            if (sessionMap != null) {
                sessionMap.values().stream()
                    .filter(session -> session.getUserId().equals(userId))
                    .findFirst()
                    .ifPresent(sessionData -> {
                        ResultData<Object> resultData = createResultData(typeName, data);
                        String message = JSON.toJSONString(resultData);
                        sendMessage(sessionData.getSession(), message);
                    });
            }
        }
    }

    private static ResultData<Object> createResultData(String typeName, Object data) {
        ResultData<Object> resultData = new ResultData<>();
        resultData.setCode(200);
        resultData.setTypeName(typeName);
        resultData.setData(data);
        return resultData;
    }

    private static void sendMessage(javax.websocket.Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            LOGGER.error("发送WebSocket消息失败: {}", e.getMessage());
        }
    }
} 