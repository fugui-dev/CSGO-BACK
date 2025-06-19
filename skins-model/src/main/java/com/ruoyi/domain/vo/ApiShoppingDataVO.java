package com.ruoyi.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiShoppingDataVO {

    private Integer id;
    private String itemName;
    private BigDecimal usePrice;
    private BigDecimal creditsPrice;
    private String imageUrl;
    private String itemId;
    private String shortName;

    private String ornamentName;
    private String type;
    private String typeName;
    private String quality;
    private String qualityName;
    private String rarity;
    private String rarityName;
    private String rarityColor;
    private String exterior;
    private String exteriorName;

}
