package com.ruoyi.domain.other;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 任务中心对象 tt_task_center
 *
 * @author ruoyi
 * @date 2024-05-25
 */
public class TtTaskCenter extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Integer taskId;

    /** 任务名称 */
    @Excel(name = "任务名称")
    private String taskName;

    /** 任务类型，参照字典管理 */
    @Excel(name = "任务类型，参照字典管理")
    private String type;

    /** 状态（0未启用 1启用） */
    @Excel(name = "状态", readConverterExp = "0=未启用,1=启用")
    private String status;

    public void setTaskId(Integer taskId)
    {
        this.taskId = taskId;
    }

    public Integer getTaskId()
    {
        return taskId;
    }
    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    public String getTaskName()
    {
        return taskName;
    }
    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("taskId", getTaskId())
                .append("taskName", getTaskName())
                .append("type", getType())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
