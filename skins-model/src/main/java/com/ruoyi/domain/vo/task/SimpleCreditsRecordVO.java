package com.ruoyi.domain.vo.task;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class SimpleCreditsRecordVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "用户ID")
    private Integer userId;

    @Excel(name = "上级ID")
    private Integer parentId;

    private Integer type;

    private Integer source;

    @Excel(name = "变动弹药")
    private BigDecimal credits;

    @Excel(name = "最终弹药")
    private BigDecimal finalCredits;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String remark;

    private Integer pwChildId;

    private String pwChildName;

    private BigDecimal pwChildAccount;

    @TableField("task_id")
    private Integer taskId;
}
