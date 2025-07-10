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
@TableName(value = "stage_cheer")
public class StageCheer {


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
     * 用户ID
     */
    private Integer userId;

    /**
     * 助威金额
     */
    private BigDecimal amount;


    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间


}
