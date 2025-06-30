package com.ruoyi.playingmethod.newgame.websocket.message;

import lombok.Data;

@Data
public class WsMessage<T> {
    private String type;        // 消息类型
    private String roomId;      // 房间ID
    private Long timestamp;     // 时间戳
    private T data;            // 消息数据
    
    public static <T> WsMessage<T> create(String type, String roomId, T data) {
        WsMessage<T> message = new WsMessage<>();
        message.setType(type);
        message.setRoomId(roomId);
        message.setTimestamp(System.currentTimeMillis());
        message.setData(data);
        return message;
    }
} 