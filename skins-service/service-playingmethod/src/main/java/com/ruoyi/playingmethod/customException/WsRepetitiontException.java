package com.ruoyi.playingmethod.customException;

// websocket用户重复连接异常
public class WsRepetitiontException extends RuntimeException{
    public WsRepetitiontException(String msg){
        super(msg);
    }
}
