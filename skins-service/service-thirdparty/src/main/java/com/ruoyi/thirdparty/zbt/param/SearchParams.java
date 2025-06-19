package com.ruoyi.thirdparty.zbt.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchParams {
    private String appId;
    private String appKey;
    private String category;
    private String exterior;
    private String hero;
    private List<String> itemIds;
    private String itemSet;
    private String keyword;
    private String language;
    private String limit;
    private String maxPrice;
    private String minPrice;
    private String orderBy;
    private String page;
    private String quality;
    private String rarity;
    private String slot;
    private String type;
    private String weapon;
}
