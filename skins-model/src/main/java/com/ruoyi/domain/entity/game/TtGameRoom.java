package com.ruoyi.domain.entity.game;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 游戏房间表
 */
@Data
@TableName("tt_game_room")
public class TtGameRoom {
    
    /**
     * 房间ID
     */
    @TableId
    private String roomId;
    
    /**
     * 房间状态（waiting/playing/ended）
     */
    private String status;
    
    /**
     * 游戏模式（normal/rich/poor）
     */
    private String gameMode;
    
    /**
     * 最大玩家数
     */
    private Integer maxPlayers;
    
    /**
     * 房间总价值
     */
    private BigDecimal totalValue;
    
    /**
     * 创建者ID
     */
    private String creatorId;
    
    /**
     * 创建者名称
     */
    private String creatorName;
    
    /**
     * 是否允许观战
     */
    private Boolean allowSpectators;
    
    /**
     * 总回合数
     */
    private Integer totalRounds;
    
    /**
     * 当前回合数
     */
    private Integer currentRound;
    
    /**
     * 胜利者ID列表（JSON数组）
     */
    private String winnerIds;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 开始时间
     */
    private Date startTime;
    
    /**
     * 结束时间
     */
    private Date endTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 房间配置（JSON）
     */
    private String roomConfig;
    
    /**
     * 游戏结果（JSON）
     */
    private String gameResult;
} 