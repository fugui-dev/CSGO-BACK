package com.ruoyi.playingmethod.model.match;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StageCheerCmd {

    /**
     * 阶段ID
     */
    private Integer stageId;

    /**
     * 队伍ID
     */
    private Integer teamId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 助威金额
     */
    private BigDecimal amount;

}
