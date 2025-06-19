package com.ruoyi.promo.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TotalUserExpenditureDTO {
    private Integer userId;
    private String userName;
    private String userType;
    private Integer parentId;
    private BigDecimal commissionRate;
    private BigDecimal amount;
}
