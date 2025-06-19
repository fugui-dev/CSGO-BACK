package com.ruoyi.thirdparty.zbt.result.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilters {

    private Integer appId;
    private String appName;
    private String icon;
    private String iconLarge;
    private List<FilterList> list;

}
