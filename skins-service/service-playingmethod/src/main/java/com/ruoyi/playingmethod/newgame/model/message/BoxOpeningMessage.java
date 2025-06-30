package com.ruoyi.playingmethod.newgame.model.message;

import com.ruoyi.domain.entity.ornament.TtOrnament;
import lombok.Data;

import java.util.List;

/**
 * 开箱动画消息
 */
@Data
public class BoxOpeningMessage {
    
    /**
     * 消息类型
     */
    private String type = "box_opening";
    
    /**
     * 房间ID
     */
    private String roomId;
    
    /**
     * 玩家ID
     */
    private String playerId;
    
    /**
     * 当前回合
     */
    private int round;
    
    /**
     * 箱子ID
     */
    private Long boxId;
    
    /**
     * 动画展示的饰品列表
     */
    private List<TtOrnament> candidates;
    
    /**
     * 最终结果饰品
     */
    private TtOrnament ornament;
    
    /**
     * 动画持续时间(毫秒)
     */
    private int duration = 3000;
    
    /**
     * 是否是最后一个玩家
     */
    private boolean lastPlayer;
    
    /**
     * 是否是最后一轮
     */
    private boolean lastRound;
} 