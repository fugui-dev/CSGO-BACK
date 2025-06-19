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
@TableName(value = "tt_bonus")
public class TtBonus implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "福利名称")
    @ApiModelProperty(name = "福利名称")
    private String name;

    @Excel(name = "福利描述")
    @ApiModelProperty(name = "福利描述")
    private String description;

    @Excel(name = "封面图片")
    @ApiModelProperty(name = "封面图片")
    private String coverPicture;

    @Excel(name = "福利类型", readConverterExp = "2=充值红包,3=福利宝箱")
    @ApiModelProperty(name = "福利类型 2=充值红包,3=福利宝箱")
    private String type;

    @Excel(name = "条件类型 0日充值 1周充值 2月充值")
    @ApiModelProperty(name = "条件类型")
    private String conditionType;

    @Excel(name = "充值门槛")
    @ApiModelProperty(name = "充值门槛")
    private BigDecimal rechargeThreshold;

    @Excel(name = "奖励区间")
    @ApiModelProperty(name = "奖励区间")
    private String awardSection;

    @Excel(name = "启用状态", readConverterExp = "0=启用,1=禁用")
    @ApiModelProperty(name = "启用状态 0=启用,1=禁用")
    private String status;

    @Excel(name = "排序依据")
    @ApiModelProperty(name = "排序依据")
    private Integer sortBy;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField(exist = false)
    @ApiModelProperty("领取状态（0未领取，1已领取）")
    private Integer getStatus = 0;

}
