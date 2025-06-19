package com.ruoyi.framework.websocket.pojo;

import lombok.Data;

import javax.websocket.Session;

@Data
public class SessionData {

    private Integer userId = 0;
    private Session session;

}
