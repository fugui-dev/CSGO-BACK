package com.ruoyi.domain.dto.promo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserPlayInfoDTO {

    private Integer userId;

    private BigDecimal commissionRate;

    private BigDecimal totalAmount;


}
