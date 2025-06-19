package com.ruoyi.user.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class YesterdayExpenditureDTO {
    private Long userId;
    private BigDecimal totalRecharge;
}
