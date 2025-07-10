package com.ruoyi.playingmethod.model.match;

import lombok.Data;

import javax.websocket.Session;

@Data
public class MatchSessionData {
    private Session session;
    private Integer userId;
    private Integer matchId;
    private String groupName;
} 