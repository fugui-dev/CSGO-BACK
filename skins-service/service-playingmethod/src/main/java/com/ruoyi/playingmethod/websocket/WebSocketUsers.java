package com.ruoyi.playingmethod.websocket;

import com.ruoyi.playingmethod.websocket.entity.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketUsers {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketUsers.class);

    private static Map<String, SessionData> USERS = new ConcurrentHashMap<String, SessionData>();

    public static void put(String key, SessionData sessionData) {
        USERS.put(key, sessionData);
    }

    public static boolean remove(SessionData sessionData) {
        String key = null;
        boolean flag = USERS.containsValue(sessionData);
        if (flag) {
            Set<Map.Entry<String, SessionData>> entries = USERS.entrySet();
            for (Map.Entry<String, SessionData> entry : entries) {
                SessionData value = entry.getValue();
                if (value.getSession().equals(sessionData.getSession())) {
                    key = entry.getKey();
                    break;
                }
            }
        } else {
            return true;
        }
        return remove(key);
    }

    public static boolean remove(String key) {
        LOGGER.info("\n 正在移出用户 - {}", key);
        SessionData remove = USERS.remove(key);
        if (remove.getSession() != null) {
            boolean containsValue = USERS.containsValue(remove);
            LOGGER.info("\n 移出结果 - {}", containsValue ? "失败" : "成功");
            LOGGER.info("\n 当前人数 - {}", WebSocketUsers.getUsers().size());
            return containsValue;
        } else {
            return true;
        }
    }

    public static Map<String, SessionData> getUsers() {
        return USERS;
    }

    public static void sendMessageToUsersByText(String message) {
        Collection<SessionData> values = USERS.values();
        for (SessionData value : values) {
            try {
                value.getSession().getBasicRemote().sendText(message);
            } catch (IOException e) {
                //LOGGER.error("\n[发送消息异常]", e);
                LOGGER.error("\n[发送消息异常]");
            }
        }
    }

    public static void sendMessageToUserByText(Integer userId, String message) {
        Collection<SessionData> values = USERS.values();
        if (userId == 0) {
            for (SessionData value : values) {
                if (value.getSession() != null && Objects.equals(value.getUserId(), userId)) {
                    try {
                        value.getSession().getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        LOGGER.error("\n[发送消息异常]", e);
                    }
                }
            }
        } else {
            for (SessionData value : values) {
                if (value.getSession() != null && Objects.equals(value.getUserId(), userId)) {
                    try {
                        value.getSession().getBasicRemote().sendText(message);
                        return;
                    } catch (IOException e) {
                        LOGGER.error("\n[发送消息异常]", e);
                        return;
                    }
                }
            }
        }
    }
}
