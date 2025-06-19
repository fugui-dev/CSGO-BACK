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
@TableName(value = "tt_upgrade_fail_ornaments")
public class  TtUpgradeFailOrnaments implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "升级失败奖励列表ID")
    @TableId
    private Integer id;

    @Excel(name = "升级饰品列表ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long upgradeId;

    @Excel(name = "失败奖励饰品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    @Excel(name = "失败奖励饰品名称")
    private String ornamentName;

    @Excel(name = "失败奖励饰品数量")
    private Integer ornamentNumber;

    @Excel(name = "饰品级别ID")
    private Integer ornamentLevelId;

    @Excel(name = "饰品价格")
    private BigDecimal ornamentPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
