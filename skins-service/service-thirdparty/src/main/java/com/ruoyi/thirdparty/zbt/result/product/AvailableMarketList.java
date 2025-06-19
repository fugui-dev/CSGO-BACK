package com.ruoyi.thirdparty.zbt.result.product;

import lombok.Data;

import java.util.List;

@Data
public class AvailableMarketList {

    private Integer limit;
    private List<AvailableMarket> list;
    private String offsetToken;
    private Integer page;
    private Integer pages;
    private String scrollId;
    private Long total;
}
