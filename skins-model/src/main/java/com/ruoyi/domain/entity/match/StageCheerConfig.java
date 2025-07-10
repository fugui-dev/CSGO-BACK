package com.ruoyi.domain.entity.match;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "stage_cheer_config")
public class StageCheerConfig {


    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 阶段类型(1-小组赛,2-淘汰赛(8强),3-淘汰赛(4强),4-决赛)
     */
    private Integer type;


    /**
     * 助威奖励倍数
     */
    private BigDecimal bonusTimes;


    /**
     * 助威积分比例
     */
    private BigDecimal scoreProportion;


    private LocalDateTime createTime; // 创建时间


    private LocalDateTime updateTime; // 更新时间


}
