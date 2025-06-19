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
public class UpgradeResultDataVOA {

    private Long id;

    private BigDecimal ornamentsPrice;
    private String itemName;
    private String shortName;
    private String imageUrl;
    private String exteriorName;
    private String levelImg;
}
