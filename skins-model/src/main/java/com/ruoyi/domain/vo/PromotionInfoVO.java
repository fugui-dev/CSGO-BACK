package com.ruoyi.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionInfoVO {

    @ApiModelProperty("我的等级")
    private Integer level;

    @ApiModelProperty("返佣比例")
    private BigDecimal commissions;

    @ApiModelProperty("团队人数")
    private Integer teamSize;

    @ApiModelProperty("明日预计收入")
    private BigDecimal afterDayPre;

}
