package com.ruoyi.playingmethod.websocket.entity;

import lombok.Data;

import javax.websocket.Session;

@Data
public class SessionData {
    private Integer userId = 0;
    private Session session;
}
