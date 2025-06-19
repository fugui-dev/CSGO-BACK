package com.ruoyi.thirdparty.zbt.result.buy;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OpenBuyResultDTO {

    private BigDecimal buyPrice;
    private Integer delivery;
    private String offerId;
    private String orderId;
}
