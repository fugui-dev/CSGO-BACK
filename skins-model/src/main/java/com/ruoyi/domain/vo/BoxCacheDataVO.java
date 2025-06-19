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
public class BoxCacheDataVO {

    private String remainingNum;
    private Integer todayOpenNum;
    private BigDecimal todayArisePriceTotal;
    private BigDecimal todayProfit;

}
