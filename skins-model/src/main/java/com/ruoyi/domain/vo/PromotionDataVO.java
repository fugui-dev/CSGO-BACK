package com.ruoyi.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionDataVO {

    @ApiModelProperty("下级总充值")
    private BigDecimal rechargeTotal;

    @ApiModelProperty("总返佣金额")
    private BigDecimal rebateTotal;

    @ApiModelProperty("未结算金额")
    private BigDecimal unbalancedPrice;

    @ApiModelProperty("已结算金额")
    private BigDecimal balancedPrice;


    @ApiModelProperty("今日总充值")
    private BigDecimal todayRechargeTotal;

    @ApiModelProperty("今日总返佣")
    private BigDecimal todayRebateTotal;

    @ApiModelProperty("本周总充值")
    private BigDecimal thisWeekRechargeTotal;

    @ApiModelProperty("本周总返佣")
    private BigDecimal thisWeekRebateTotal;

    @ApiModelProperty("本月总充值")
    private BigDecimal thisMonthRechargeTotal;

    @ApiModelProperty("本周总返佣")
    private BigDecimal thisMonthRebateTotal;
}
