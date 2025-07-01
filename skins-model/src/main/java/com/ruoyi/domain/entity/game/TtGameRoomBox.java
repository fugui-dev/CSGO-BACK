package com.ruoyi.domain.entity.game;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 房间盲盒配置表
 */
@Data
@TableName("tt_game_room_box")
public class TtGameRoomBox {
    
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
     * 盲盒ID
     */
    private Long boxId;
    
    /**
     * 盲盒名称
     */
    private String boxName;
    
    /**
     * 盲盒价格
     */
    private BigDecimal boxPrice;
    
    /**
     * 开启次数
     */
    private Integer boxCount;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
} 