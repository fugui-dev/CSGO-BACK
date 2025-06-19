package com.ruoyi.domain.task;

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
@TableName("tt_task_doing")
public class TtTaskDoing {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    // 任务id
    @TableField("task_id")
    private Integer taskId;

    // 玩家id
    @TableField("user_id")
    private Integer userId;

    // 任务完成状态
    @TableField("completion_state")
    private Integer completionState;

    // 任务进度值
    @TableField("progress")
    private Integer progress;

    // 开始时间
    @TableField("begin_time")
    private Timestamp beginTime;

    // 完成时间
    @TableField("compete_time")
    private Timestamp competeTime;

}
