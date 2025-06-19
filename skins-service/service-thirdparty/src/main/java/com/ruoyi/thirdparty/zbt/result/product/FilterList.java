package com.ruoyi.thirdparty.zbt.result.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterList {

    private String alias;
    private String key;
    private List<FilterKeyList> list;
    private String name;
    private String searchKey;

}
