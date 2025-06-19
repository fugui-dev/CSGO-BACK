package com.ruoyi.framework.websocket;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.framework.websocket.pojo.ResultData;
import com.ruoyi.framework.websocket.pojo.SessionData;
import com.ruoyi.framework.websocket.util.SemaphoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

// @Component
// @ServerEndpoint("/api/websocket/skins")
public class WebSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);

    public static int socketMaxOnlineCount = 100;

    private static Semaphore socketSemaphore = new Semaphore(socketMaxOnlineCount);

    @OnOpen
    public void onOpen(Session session) throws Exception {
        SessionData sessionData = new SessionData();
        boolean semaphoreFlag = false;
        // 尝试获取信号量
        semaphoreFlag = SemaphoreUtils.tryAcquire(socketSemaphore);
        if (!semaphoreFlag) {
            // 未获取到信号量
            LOGGER.error("\n 当前在线人数超过限制数- {}", socketMaxOnlineCount);
            try {
                ResultData<String> resultData = new ResultData<>();
                resultData.setCode(500);
                resultData.setTypeName("WebSocket异常提示");
                resultData.setData("当前在线人数超过限制数：" + socketMaxOnlineCount);
                session.getBasicRemote().sendText(JSON.toJSONString(resultData));
            } catch (IOException e) {
                LOGGER.error("\n[发送消息异常]", e);
            }
            session.close();
        } else {
            // 添加用户
            sessionData.setSession(session);
            WebSocketUsers.put(session.getId(), sessionData);
            LOGGER.info("\n 建立连接 - {}", session);
            LOGGER.info("\n sessionId - {}", session.getId());
            LOGGER.info("\n 当前人数 - {}", WebSocketUsers.getUsers().size());
            try {
                ResultData<String> resultData = new ResultData<>();
                resultData.setCode(0);
                resultData.setTypeName("WebSocket连接成功");
                resultData.setData("连接成功");
                session.getBasicRemote().sendText(JSON.toJSONString(resultData));
            } catch (IOException e) {
                LOGGER.error("\n[发送消息异常]", e);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("\n 关闭连接 - {}", session);
        // 移除用户
        WebSocketUsers.remove(session.getId());
        // 获取到信号量则需释放
        SemaphoreUtils.release(socketSemaphore);
    }

    @OnError
    public void onError(Session session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            // 关闭连接
            session.close();
        }
        String sessionId = session.getId();
        LOGGER.info("\n 连接异常 - {}", sessionId);
        LOGGER.info("\n 异常信息 - {}", exception);
        // 移出用户
        WebSocketUsers.remove(sessionId);
        // 获取到信号量则需释放
        SemaphoreUtils.release(socketSemaphore);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (message.startsWith("login_userId_")) {
            Integer userId = Integer.valueOf(message.replace("login_userId_", ""));
            Map<String, SessionData> users = WebSocketUsers.getUsers().entrySet().stream()
                    .filter(entry -> Objects.equals(entry.getValue().getUserId(), userId))
                    .peek(entry -> {
                        entry.getValue().setUserId(0);
                        LOGGER.info("用户{}重新登录！", userId);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            SessionData sessionData = new SessionData();
            sessionData.setUserId(userId);
            sessionData.setSession(session);
            WebSocketUsers.put(session.getId(), sessionData);
            LOGGER.info("用户{}登录成功！", userId);
        }
        if (message.startsWith("logout_userId_")) {
            Integer userId = Integer.valueOf(message.replace("logout_userId_", ""));
            // 遍历所有用户集
            Map<String, SessionData> users = WebSocketUsers.getUsers().entrySet().stream()
                    .filter(entry -> Objects.equals(entry.getValue().getUserId(), userId))
                    .peek(entry -> {
                        entry.getValue().setUserId(0);
                        LOGGER.info("用户{}退出登录！", userId);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        if ("ping".equals(message)) {
            try {
                ResultData<String> resultData = new ResultData<>();
                resultData.setCode(200);
                resultData.setTypeName("心跳检测");
                resultData.setData("pong");
                session.getBasicRemote().sendText(JSON.toJSONString(resultData));
            } catch (IOException e) {
                LOGGER.error("\n[发送消息异常]", e);
            }
        }
    }
}
