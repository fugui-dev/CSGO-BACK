package com.ruoyi.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class AvailableMarketOrnamentVO {

    // 平台 "yy" "zbt"
    private Integer platform;

    // yy的
    private Integer templateId;
    // zbt的
    private String itemId;

    private Integer delivery;
    private String itemName;
    private String imageUrl;
    private BigDecimal cnyPrice;
    private BigDecimal price;
}
