package com.ruoyi.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TtBoxOrnamentsDataVO {

    private Integer id;
    private Integer boxId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    private String imageUrl;
    private String itemName;
    private String name;
    private BigDecimal usePrice;
    private Integer ornamentsLevelId;

    // 等级图片 背景
    private String levelImg;

    private String level;
    private Integer odds;
    private String oddsPercentum;
    private Integer realOdds;
    private String realOddsPercentum;
    private Integer anchorOdds;
    private String anchorOddsPercentum;
}
