package com.ruoyi.playingmethod.newgame.model.message;

import lombok.Data;

@Data
public class WsMessage<T> {
    private String type;
    private String roomId;
    private T data;
    
    public static <T> WsMessage<T> create(String type, String roomId, T data) {
        WsMessage<T> message = new WsMessage<>();
        message.setType(type);
        message.setRoomId(roomId);
        message.setData(data);
        return message;
    }
} 