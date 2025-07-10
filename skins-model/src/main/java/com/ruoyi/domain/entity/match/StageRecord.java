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
public class StageRecord {

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
     * 对手队伍ID
     */
    private Integer opponentTeamId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 对手用户ID
     */
    private Integer opponentUserId;

    /**
     * 阶段得分
     */
    private BigDecimal score;

    /**
     * 对手得分
     */
    private BigDecimal opponentScore;

    /**
     * 轮次
     */
    private Integer round;

    /**
     * 用户选择的概率
     */
    private Integer data;

    /**
     * 对手选择的概率
     */
    private Integer opponentData;

    /**
     * 开出来的概率
     */
    private Integer resultData;


    /**
     * 阶段队伍总分
     */
    private BigDecimal totalScore;

    /**
     * 阶段结果 (1 胜利, 0 平局, -1 失败)
     */
    private Integer result;


    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
