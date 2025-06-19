package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TtUpgradeOrnamentsDataVO {

    private Integer id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentsId;

    private BigDecimal usePrice;

    private String itemName;

    private String imageUrl;

    private String type;

    private String typeName;

    private Integer ornamentsLevelId;

    private String level;

    private String luckSection;

    private BigDecimal amountRequired;

    private BigDecimal amountInvested;

    private String anchorLuckSection;

    private BigDecimal anchorAmountRequired;

    private BigDecimal anchorAmountInvested;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
