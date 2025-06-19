package com.ruoyi.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApiLuckyOrnamentsDataVO {

    private Integer id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    private String itemName;

    private BigDecimal usePrice;

    private String imageUrl;

    private String shortName;

    private String ornamentName;

    private String typeName;

    private String rarityName;

    private String exteriorName;

    private String levelImg;
}
