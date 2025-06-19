package com.ruoyi.domain.other;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TtBoxUser {

    private Long id;
    private Integer boxId;
    private Integer userId;
    private String userName;
    private String nickName;
    private String avatar;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentsId;
    private String itemName;
    private String shortName;
    private BigDecimal usePrice;
    private String imageUrl;
    private String exteriorName;
    private Integer ornamentsLevelId;
    private String levelImg;

    // 来源
    private String source;

}
