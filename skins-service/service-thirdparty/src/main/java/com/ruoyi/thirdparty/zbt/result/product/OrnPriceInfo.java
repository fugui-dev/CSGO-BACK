package com.ruoyi.thirdparty.zbt.result.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrnPriceInfo {

    @JsonProperty("itemId")
    private Long id;
    private String marketHashName;
    private BigDecimal price; //如果是出售就是在售最低价，如果是求购就是求购最高价，加买家手续费后的价格
    private Integer quantity; //数量，如果是出售就是在售数量，如果是求购就是求购数量
    private BigDecimal autoDeliverPrice; //自动发货在售最低价
    private Integer autoDeliverQuantity; //自动发货在售数量

    private BigDecimal manualDeliverPrice; //人工发货在售最低价

    private Integer manualQuantity; //人工发货在售数量

    private BigDecimal avgPrice; //近10笔成交平均价格
    private BigDecimal medianPrice; //近10笔成交中位数价格


}
