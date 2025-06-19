package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

// 提货申请记录信息
@Data
public class DeliveryApplyVO {

    private Integer id;

    private Integer userId;

    private String outTradeNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    private String ornamentName;

    private Integer boxRecordsId;

    private String nickName;

    // // 平台和id
    // private Integer partyType;

    // 饰品hash
    private String hashName;

    private BigDecimal ornamentsPrice;
    private String name;
    private String imageUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
