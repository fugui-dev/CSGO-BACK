package com.ruoyi.promo.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 日流水
 */
@Data
public class DayTurnoverVO {
    private Date date;
    private BigDecimal amount;
}
