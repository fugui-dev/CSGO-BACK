package com.ruoyi.user.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TtTaskCenterUser {
    private Long userId;
    private String type;
    private BigDecimal credit;
    private String claimed;
}
