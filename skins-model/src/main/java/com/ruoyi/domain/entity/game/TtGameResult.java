package com.ruoyi.domain.entity.game;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 游戏结果表
 */
@Data
@TableName("tt_game_result")
public class TtGameResult {
    
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
     * 总价值
     */
    private BigDecimal totalValue;
    
    /**
     * 是否胜利
     */
    private Boolean isWinner;
    
    /**
     * 团队编号（2V2模式使用）
     */
    private Integer teamNumber;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 