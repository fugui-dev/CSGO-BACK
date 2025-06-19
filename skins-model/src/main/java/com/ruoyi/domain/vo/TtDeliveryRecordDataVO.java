package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TtDeliveryRecordDataVO {

    private Integer id;
    private Integer userId;
    private String nickName;
    private String itemName;
    private String imageUrl;
    private String outTradeNo;
    private BigDecimal buyPrice;
    private BigDecimal ornamentsPrice;
    private String levelImg;
    private Integer thirdpartyDelivery;

    private Integer delivery;
    private String orderId;
    private String status;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String updateTime;
}
