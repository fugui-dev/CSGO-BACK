package com.ruoyi.domain.other;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShoppingBody {

    private Integer id;
    private String itemName;
    private String type;
    private String exterior;
    private BigDecimal maxPrice;
    private BigDecimal minPrice = BigDecimal.ZERO;
    private Integer isPutaway;

}
