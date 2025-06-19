package com.ruoyi.domain.vo.zbt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SellItemInfo {

    private Long itemId;
    private String itemName;
    private String marketHashName;


    private BigDecimal price;

    // 补贴价
    private BigDecimal subsidyPrice;

    // 上架价格
    private BigDecimal sellerPrice;

    // 人民币价格
    private BigDecimal cnyPrice;

    // // 发货类型
    // private BigDecimal delivery;
}
