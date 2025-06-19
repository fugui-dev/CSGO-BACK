package com.ruoyi.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserPackSackDataVO {

    private Long id;


    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;
    private BigDecimal ornamentsPrice;
    private String itemName;
    private String shortName;

    private String ornamentName;
    private String imageUrl;
    private String exteriorName;

    private Integer ornamentsLevelId;
    private String levelImg;

}
