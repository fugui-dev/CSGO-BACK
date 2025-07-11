package com.ruoyi.playingmethod.service.match.message;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.framework.websocket.pojo.ResultData;
import com.ruoyi.framework.websocket.util.SemaphoreUtils;
import com.ruoyi.playingmethod.model.match.MatchSessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.Semaphore;

@Component
@ServerEndpoint("/api/websocket/match/{matchId}/{groupName}/{userId}")
public class MatchWebSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchWebSocketServer.class);
    
    public static int socketMaxOnlineCount = 500;
    private static Semaphore socketSemaphore = new Semaphore(socketMaxOnlineCount);

    @OnOpen
    public void onOpen(Session session, 
                      @PathParam("matchId") Integer matchId,
                      @PathParam("groupName") String groupName,
                      @PathParam("userId") Integer userId) throws Exception {
        boolean semaphoreFlag = SemaphoreUtils.tryAcquire(socketSemaphore);
        if (!semaphoreFlag) {
            sendErrorMessage(session, "当前在线人数超过限制数：" + socketMaxOnlineCount);
            session.close();
            return;
        }

        MatchSessionData sessionData = new MatchSessionData();
        sessionData.setSession(session);
        sessionData.setUserId(userId);
        sessionData.setMatchId(matchId);
        sessionData.setGroupName(groupName);
        
        MatchWebSocketUsers.addUser(sessionData);
        
        LOGGER.info("Match WebSocket连接建立 - matchId: {}, groupName: {}, userId: {}", matchId, groupName, userId);
        sendSuccessMessage(session, "连接成功");
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("Match WebSocket连接关闭 - {}", session.getId());
        MatchWebSocketUsers.removeBySessionId(session.getId());
        SemaphoreUtils.release(socketSemaphore);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOGGER.error("Match WebSocket发生错误 - {}", error.getMessage());
        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                LOGGER.error("关闭WebSocket session失败", e);
            }
        }
        MatchWebSocketUsers.removeBySessionId(session.getId());
        SemaphoreUtils.release(socketSemaphore);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if ("ping".equals(message)) {
            try {
                ResultData<String> resultData = new ResultData<>();
                resultData.setCode(200);
                resultData.setTypeName("心跳检测");
                resultData.setData("pong");
                session.getBasicRemote().sendText(JSON.toJSONString(resultData));
            } catch (IOException e) {
                LOGGER.error("发送心跳响应失败", e);
            }
        }
    }

    private void sendErrorMessage(Session session, String message) {
        try {
            ResultData<String> resultData = new ResultData<>();
            resultData.setCode(500);
            resultData.setTypeName("WebSocket错误");
            resultData.setData(message);
            session.getBasicRemote().sendText(JSON.toJSONString(resultData));
        } catch (IOException e) {
            LOGGER.error("发送错误消息失败", e);
        }
    }

    private void sendSuccessMessage(Session session, String message) {
        try {
            ResultData<String> resultData = new ResultData<>();
            resultData.setCode(200);
            resultData.setTypeName("WebSocket消息");
            resultData.setData(message);
            session.getBasicRemote().sendText(JSON.toJSONString(resultData));
        } catch (IOException e) {
            LOGGER.error("发送成功消息失败", e);
        }
    }
} 