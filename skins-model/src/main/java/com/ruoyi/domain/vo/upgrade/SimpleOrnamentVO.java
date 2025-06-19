package com.ruoyi.domain.vo.upgrade;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SimpleOrnamentVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;
    private String ornamentName;
    private Integer ornamentNumber;
    private String ornamentHashName;
    private BigDecimal ornamentPrice;
    private String ornamentImgUrl;
    private String ornamentLevel;
    private String ornamentLevelImg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String type;

    private String typeName;
}
