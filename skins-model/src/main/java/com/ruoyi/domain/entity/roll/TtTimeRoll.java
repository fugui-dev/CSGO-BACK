package com.ruoyi.domain.entity.roll;

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
@TableName(value = "tt_time_roll")
public class TtTimeRoll implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "奖池ID")
    private Integer jackpotId;

    @Excel(name = "房间名称")
    private String name;

    @Excel(name = "房间描述")
    private String description;

    private String rechargeCondition;

    @Excel(name = "充值门槛")
    private BigDecimal minRecharge;

    @Excel(name = "排序依据")
    private Integer sortBy;

    private String status;

    @Excel(name = "创建者")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField(select = false)
    private String delFlag;

    private Integer jobId;
}
