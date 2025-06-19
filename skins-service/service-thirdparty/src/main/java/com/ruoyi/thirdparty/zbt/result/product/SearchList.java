package com.ruoyi.thirdparty.zbt.result.product;

import lombok.Data;

import java.util.List;

@Data
public class SearchList {
    private Integer appId;
    private String exterior;
    private String exteriorName;
    private String imageUrl;
    private String itemId;
    private String itemName;
    private String marketHashName;
    private List<PriceInfo> priceInfo;
    private String quality;
    private String qualityColor;
    private String qualityName;
    private String rarity;
    private String rarityColor;
    private String rarityName;
    private String shortName;
    private String type;
    private String typeName;
}
