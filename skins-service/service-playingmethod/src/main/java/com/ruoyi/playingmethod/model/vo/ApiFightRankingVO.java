package com.ruoyi.playingmethod.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApiFightRankingVO {

    private Long userId;

    private String nickName;

    private String avatar;

    private BigDecimal totalBoxPrice;
}
