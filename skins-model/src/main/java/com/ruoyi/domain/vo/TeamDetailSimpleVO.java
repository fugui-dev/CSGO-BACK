package com.ruoyi.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeamDetailSimpleVO {

    private Integer userId;
    private String nickName;
    private BigDecimal recharge;
    private BigDecimal beConsume;

}
