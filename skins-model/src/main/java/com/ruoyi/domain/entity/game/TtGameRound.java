package com.ruoyi.domain.entity.game;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 游戏回合表
 */
@Data
@TableName("tt_game_round")
public class TtGameRound {
    
    /**
     * 回合ID
     */
    @TableId
    private Long roundId;
    
    /**
     * 房间ID
     */
    private String roomId;
    
    /**
     * 回合序号
     */
    private Integer roundNumber;
    
    /**
     * 盲盒ID
     */
    private Long boxId;
    
    /**
     * 回合状态（preparing/opening/finished）
     */
    private String status;
    
    /**
     * 开始时间
     */
    private Date startTime;
    
    /**
     * 结束时间
     */
    private Date endTime;
} 