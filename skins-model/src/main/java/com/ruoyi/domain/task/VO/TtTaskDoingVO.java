package com.ruoyi.domain.task.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TtTaskDoingVO {

    // 任务id
    // @TableField("task_id")
    private Integer taskId;

    // 任务实例id
    // @TableField("task_id")
    private Integer taskDoingId;

    // 玩家id
    // @TableField("user_id")
    private Integer userId;

    // 描述
    // @TableField("describe")
    private String taskDescribe;

    // @TableField("name")
    private String name;

    // 任务类型
    // @TableField("type")
    private Integer type;

    // 目标值类型
    // @TableField("target_type")
    private Integer targetType;

    // 目标值
    // @TableField("target_value")
    private Integer targetValue;

    // 奖励类型
    // @TableField("award_type")
    private Integer awardType;

    // 奖励数量
    // @TableField("award_value")
    private Integer awardValue;

    // 任务完成状态
    // @TableField("completion_state")
    private Integer completionState;

    // 开始时间
    // @TableField("begin_time")
    private Timestamp beginTime;

    // 完成时间
    // @TableField("compete_time")
    private Timestamp competeTime;

    // 任务进度值
    // @TableField("progress")
    private Integer progress;

    // 海报地址
    // @TableField("placard_url")
    private String placardUrl;

}
