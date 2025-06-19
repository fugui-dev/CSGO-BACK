package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TtRollJackpotOrnamentsDataVO {

    private Integer id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentsId;

    private Integer ornamentsNum;

    private String itemName;

    private String shortName;

    private BigDecimal usePrice;

    private String imageUrl;

    private String ornamentLevelId;

    private String level;

    private String levelImg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
