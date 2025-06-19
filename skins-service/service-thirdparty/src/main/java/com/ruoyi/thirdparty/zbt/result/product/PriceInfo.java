package com.ruoyi.thirdparty.zbt.result.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceInfo {

    private BigDecimal autoDeliverPrice;
    private Integer autoDeliverQuantity;
    private BigDecimal manualDeliverPrice;
    private Integer manualQuantity;
    private BigDecimal price;
    private Integer quantity;
    private Integer userId;

}
