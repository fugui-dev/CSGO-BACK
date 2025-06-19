package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TtUpgradeFailOrnamentsDataVO {

    private Integer id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long upgradeId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    private BigDecimal usePrice;

    private String itemName;

    private Integer ornamentNumber;

    private String imageUrl;

    private String type;

    private String typeName;

    private Integer ornamentLevelId;

    private String level;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
