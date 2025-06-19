package com.ruoyi.domain.other;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class TtDeliveryRecordBody {

    private String userName;

    private String phoneNumber;

    private String outTradeNo;

    private String orderId;

    private Integer userId;

    private String status;

    private String itemName;

    private String levelImg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String endTime;
}
