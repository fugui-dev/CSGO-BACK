package com.ruoyi.domain.other;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JackpotData {

    private String itemName;

    private BigDecimal usePrice;

    private String imageUrl;

    private String shortName;

    private String typeName;

    private String exteriorName;

    private String level;

    private String levelImg;

    private Integer id;

    private Integer allocatedNum;

    private Integer unAllocatedNum;

    private Integer ornamentsNum;

}
