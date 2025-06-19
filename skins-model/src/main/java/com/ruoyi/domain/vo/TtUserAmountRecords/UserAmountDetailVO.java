package com.ruoyi.domain.vo.TtUserAmountRecords;

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
public class UserAmountDetailVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "用户ID")
    private Integer userId;

    // 1、收入 2、支出
    private Integer type;

    // 1、金币 2弹药
    private Integer moneyType;

    // 来源
    private Integer source;

    @Excel(name = "变动金额")
    private BigDecimal amount;

    private BigDecimal credits;

    @Excel(name = "最终金额")
    private BigDecimal finalAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String remark;

    private Integer pwChildId;
    private String pwChildName;
    private BigDecimal pwChildAccount;

    @TableField("task_id")
    private Integer taskId;
}
