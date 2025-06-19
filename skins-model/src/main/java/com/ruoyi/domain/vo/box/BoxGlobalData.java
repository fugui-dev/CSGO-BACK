package com.ruoyi.domain.vo.box;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BoxGlobalData {

    private Integer boxId;
    private String boxName;

    // 单价
    private BigDecimal price;
    // private Integer openNumber;

    // 通用-------------------------------------------------
    // 宝箱完全开启所需金额
    private BigDecimal commonAmountConsumed;
    // 宝箱内饰品总价值
    private BigDecimal commonAggregateAmount;
    // 利润
    private BigDecimal commonProfit;
    // 利润率
    private BigDecimal commonProfitMargin;

    // 主播---------------------------------------------------
    // 宝箱完全开启所需金额
    private BigDecimal anchorAmountConsumed;
    // 宝箱内饰品总价值
    private BigDecimal anchorAggregateAmount;
    // 利润
    private BigDecimal anchorProfit;
    // 利润率
    private BigDecimal anchorProfitMargin;

}
