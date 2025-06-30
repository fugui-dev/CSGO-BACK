package com.ruoyi.playingmethod.newgame.websocket;

import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.domain.entity.sys.TtUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class GameWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final TtUserService userService;

    public GameWebSocketHandshakeInterceptor(TtUserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            // 获取请求参数中的用户ID
            if (request instanceof ServletServerHttpRequest) {
                ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                String userId = servletRequest.getServletRequest().getParameter("userId");
                
                if (userId != null) {
                    // 验证用户
                    TtUser user = userService.getById(Long.parseLong(userId));
                    if (user != null ) {
                        // 将用户信息存储在WebSocket会话属性中
                        attributes.put("userId", userId);
                        attributes.put("currentUser", user);
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.error("WebSocket握手认证异常", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理
    }
} 