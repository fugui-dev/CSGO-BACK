package com.ruoyi.domain.other;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OperationalStatistics {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private BigDecimal rechargePriceTotal;
    private BigDecimal deliveryPriceTotal;
    private BigDecimal profit;
}
