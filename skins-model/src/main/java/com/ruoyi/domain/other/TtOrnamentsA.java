package com.ruoyi.domain.other;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TtOrnamentsA {

    // private Long id;
    // // 平台
    // private Integer partyType;
    // 在该平台的id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    private String name;
    private String shortName;
    private BigDecimal usePrice;
    private String imageUrl;
    private String exteriorName;

    private Integer ornamentsLevelId;

    private String levelImg;

    private Integer odds;

    private BigDecimal oddsResult;

}
