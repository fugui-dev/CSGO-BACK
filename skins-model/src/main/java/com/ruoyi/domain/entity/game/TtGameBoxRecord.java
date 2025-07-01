package com.ruoyi.domain.entity.game;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 开箱记录表
 */
@Data
@TableName("tt_game_box_record")
public class TtGameBoxRecord {
    
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
     * 回合ID
     */
    private Long roundId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 盲盒ID
     */
    private Long boxId;
    
    /**
     * 饰品ID
     */
    private String ornamentId;
    
    /**
     * 饰品名称
     */
    private String ornamentName;
    
    /**
     * 饰品价值
     */
    private BigDecimal ornamentValue;
    
    /**
     * 开箱时间
     */
    private Date openTime;
} 