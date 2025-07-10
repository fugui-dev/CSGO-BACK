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
@TableName(value = "stage_team")
public class StageTeam {

    @TableId(type = IdType.AUTO)
    private Integer id;


    /**
     * 阶段ID
     */
    private Integer stageId;

    /**
     * 队伍ID
     */
    private Integer teamId;

    /**
     * 阶段队伍总分
     */
    private BigDecimal totalScore;

    /**
     * 阶段队伍总胜场数
     */
    private Integer winCount;

    /**
     * 阶段队伍初始分数
     */
    private BigDecimal initialScore;

    /**
     * 阶段队伍总助威金额
     */
    private BigDecimal cheerAmount;

    /**
     * 阶段排名
     */
    private Integer rank;

    /**
     * 阶段结果 (1 胜利, 0 平局, -1 失败)
     */
    private Integer result;


    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
