package com.ruoyi.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TtBoxDataVO {

    private Integer boxId;
    private String boxName;
    private String boxTypeId;
    private String boxTypeName;
    private BigDecimal price;
    private String boxImg01;
    private String boxImg02;
    private Integer sort;
    private String isFight;
    private String status;
    private Long openNum;
    private String isHome;

    // 宝箱完全开启所需金额
    private BigDecimal amountConsumed;

    // 宝箱内饰品总价值
    private BigDecimal aggregateAmount;

    // 利润
    private BigDecimal profit;

    // 利润率
    private String profitMargin = "100%";

}
