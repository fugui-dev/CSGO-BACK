package com.ruoyi.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class FightBoxDataVO {

    private Integer boxId;
    private String boxImg01;
    private String boxImg02;
    private String boxName;
    private BigDecimal price;

}
