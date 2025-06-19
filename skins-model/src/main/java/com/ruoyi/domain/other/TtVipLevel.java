package com.ruoyi.domain.other;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
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
@TableName(value = "tt_vip_level")
public class TtVipLevel implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "VIP等级名称")
    @ApiModelProperty(name = "VIP等级名称")
    private String name;

    @Excel(name = "VIP等级图标")
    @ApiModelProperty(name = "VIP等级图标")
    private String icon;

    @Excel(name = "VIP等级达标金额")
    @ApiModelProperty(name = "VIP等级达标金额")
    private BigDecimal rechargeThreshold;

    @Excel(name = "充值加送比例")
    @ApiModelProperty(name = "充值加送比例")
    private BigDecimal commissions;

    @Excel(name = "等级达标奖励红包金额")
    @ApiModelProperty(name = "等级达标奖励红包金额")
    private BigDecimal addedBonus;

    @Excel(name = "描述")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
