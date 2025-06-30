package com.ruoyi.playingmethod.newgame.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class GameHistory {
    
    /**
     * 房间ID
     */
    private String roomId;
    
    /**
     * 游戏模式
     */
    private String gameMode;
    
    /**
     * 房间创建者ID
     */
    private String creatorId;
    
    /**
     * 房间创建时间
     */
    private Date createTime;
    
    /**
     * 游戏开始时间
     */
    private Date startTime;
    
    /**
     * 游戏结束时间
     */
    private Date endTime;
    
    /**
     * 参与玩家列表
     */
    private List<GamePlayer> players;
    
    /**
     * 箱子配置
     */
    private List<GameRoom.BoxConfig> boxConfigs;
    
    /**
     * 每个玩家的开箱结果
     * Map<玩家ID, List<饰品ID>>
     */
    private Map<String, List<String>> playerResults;
    
    /**
     * 胜利者ID列表
     */
    private List<String> winners;
    
    /**
     * 总价值
     */
    private BigDecimal totalValue;
    
    /**
     * 每个玩家的总价值
     * Map<玩家ID, 总价值>
     */
    private Map<String, BigDecimal> playerValues;
} 