package com.ruoyi.domain.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@TableName("tt_task")
public class TtTask {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    // 描述
    @TableField("task_describe")
    private String taskDescribe;

    @TableField("name")
    private String name;

    // 任务状态
    @TableField("state")
    private Integer state;

    // 任务类型
    @TableField("type")
    private Integer type;

    // 目标值类型
    @TableField("target_type")
    private Integer targetType;

    // 目标值
    @TableField("target_value")
    private Integer targetValue;

    // 奖励类型
    @TableField("award_type")
    private Integer awardType;

    // 奖励数量
    @TableField("award_value")
    private Integer awardValue;

    // 触发条件
    @TableField("task_condition")
    private String taskCondition;

    // 海报地址
    @TableField("placard_url")
    private String placardUrl;

}
