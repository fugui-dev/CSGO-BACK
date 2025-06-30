package com.ruoyi.playingmethod.newgame.model.message;

import com.ruoyi.domain.entity.ornament.TtOrnament;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 开箱结果消息
 */
@Data
public class BoxResultMessage {
    
    /**
     * 消息类型
     */
    private String type = "box_result";
    
    /**
     * 房间ID
     */
    private String roomId;
    
    /**
     * 当前回合
     */
    private int round;
    
    /**
     * 每个玩家的开箱结果
     * Map<玩家ID, 饰品>
     */
    private Map<String, TtOrnament> playerResults;
    
    /**
     * 每个玩家的总价值
     * Map<玩家ID, 总价值>
     */
    private Map<String, BigDecimal> playerValues;
    
    /**
     * 是否是最后一轮
     */
    private boolean lastRound;
    
    /**
     * 游戏胜利者ID列表(仅最后一轮有值)
     */
    private List<String> gameWinners;
    
    /**
     * 游戏总价值(仅最后一轮有值)
     */
    private BigDecimal totalValue;
} 