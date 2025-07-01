package com.ruoyi.domain.entity.game;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 游戏玩家表
 */
@Data
@TableName("tt_game_player")
public class TtGamePlayer {
    
    /**
     * 记录ID
     */
    @TableId
    private Long id;
    
    /**
     * 房间ID
     */
    private String roomId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名称
     */
    private String username;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 座位号
     */
    private Integer seatNumber;
    
    /**
     * 是否是房主
     */
    private Boolean isOwner;
    
    /**
     * 是否准备
     */
    private Boolean isReady;
    
    /**
     * 是否是机器人
     */
    private Boolean isRobot;
    
    /**
     * 团队编号（2V2模式使用）
     */
    private Integer teamNumber;
    
    /**
     * 获得的饰品ID列表（JSON数组）
     */
    private String ornamentIds;
    
    /**
     * 总价值
     */
    private BigDecimal totalValue;
    
    /**
     * 是否胜利
     */
    private Boolean isWinner;
    
    /**
     * 加入时间
     */
    private Date joinTime;
    
    /**
     * 离开时间
     */
    private Date leaveTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 