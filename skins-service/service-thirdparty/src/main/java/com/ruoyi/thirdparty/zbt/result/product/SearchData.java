package com.ruoyi.thirdparty.zbt.result.product;

import com.ruoyi.domain.dto.zbt.OrnamentZBT;
import lombok.Data;

import java.util.Set;

@Data
public class SearchData {
    private Integer limit;
    private Set<OrnamentZBT> list;
    private String offsetToken;
    private Integer page;
    private Integer pages;
    private String scrollId;
    private Integer total;
}

