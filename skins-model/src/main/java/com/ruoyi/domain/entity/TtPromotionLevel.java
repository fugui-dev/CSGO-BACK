package com.ruoyi.domain.entity;

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
@TableName(value = "tt_promotion_level")
public class TtPromotionLevel implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "推广等级名称")
    @ApiModelProperty("推广等级名称")
    private String name;

    @Excel(name = "推广等级图标")
    @ApiModelProperty("推广等级图标")
    private String icon;

    @Excel(name = "推广等级达标金额")
    @ApiModelProperty("推广等级达标金额")
    private BigDecimal rechargeThreshold;

    @Excel(name = "推广返佣比例")
    @ApiModelProperty("推广返佣比例")
    private BigDecimal commissions;

    @Excel(name = "等级达标奖励红包金额")
    @ApiModelProperty("等级达标奖励红包金额")
    private BigDecimal addedBonus;

    @Excel(name = "充值赠送比例")
    @ApiModelProperty("充值赠送比例")
    private BigDecimal rechargeGiftRatio;

    @Excel(name = "描述")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
