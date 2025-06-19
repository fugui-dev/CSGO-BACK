package com.ruoyi.domain.vo;

//yy有品 批量查询在售商品详情vo

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class queryTemplateSaleByCategoryVO {
    private Integer templateId;
    private String templateHashName;
    private String templateName;
    private String iconUrl;
    private String exteriorName;
    private String rarityName;
    private Integer typeId;
    private String typeHashName;
    private Integer weaponId;
    private String weaponHashName;
    private BigDecimal minSellPrice;
    private BigDecimal referencePrice;
    private Integer sellNum;
}
