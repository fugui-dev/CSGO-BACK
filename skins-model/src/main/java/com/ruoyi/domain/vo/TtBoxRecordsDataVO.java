package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TtBoxRecordsDataVO {

    private Long boxRecordId;
    private Integer userId;
    private Integer holderUserId;
    private Integer boxId;
    private String boxName;
    private BigDecimal boxPrice;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentsId;
    private String ornamentName;
    private String ornamentImgUrl;
    private String ornamentLevelImg;
    private BigDecimal ornamentsPrice;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
