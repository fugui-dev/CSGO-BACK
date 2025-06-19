package com.ruoyi.domain.other;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TtBoxA {

    private Integer boxId;
    private Integer boxTypeId;
    private String boxName;
    private BigDecimal price;
    private String boxImg01;
    private String boxImg02;
    private List<TtOrnamentsA> boxOrnamentsList;
    private List<TtBoxLevelA> probabilityDistribution;

}
