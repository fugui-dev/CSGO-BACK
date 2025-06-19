package com.ruoyi.domain.other;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
@TableName(value = "tt_upgrade_ornaments")
public class TtUpgradeOrnaments implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "升级饰品ID")
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Excel(name = "饰品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentsId;

    @Excel(name = "饰品原价")
    @TableField("ornament_price")
    private BigDecimal ornamentPrice;

    @Excel(name = "饰品级别ID")
    private Integer ornamentsLevelId;

    @Excel(name = "通用幸运区间")
    private String luckSection;

    @Excel(name = "本轮出货所需金额")
    private BigDecimal amountRequired;

    @Excel(name = "本轮已投金额")
    private BigDecimal amountInvested;

    @Excel(name = "主播幸运区间")
    private String anchorLuckSection;

    @Excel(name = "本轮出货所需金额")
    private BigDecimal anchorAmountRequired;

    @Excel(name = "本轮已投金额")
    private BigDecimal anchorAmountInvested;

    // 总投入
    @TableField("total_input")
    private BigDecimal totalInput;

    // 0可用 1禁用
    @Excel(name = "状态")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
