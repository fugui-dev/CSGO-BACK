package com.ruoyi.thirdparty.zbt.param;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NormalBuyParamV2DTO {

    private BigDecimal buyPrice;
    private BigDecimal maxPrice;
    private String outTradeNo;

    // 平台的物品在售记录id
    private String productId;
    private String tradeUrl;
}
