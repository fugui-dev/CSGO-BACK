package com.ruoyi.domain.other;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class TtRechargeRecordBody {

    private Integer userId;

    private Integer parentId;
    private String orderId;
    private String outTradeNo;
    private String channelType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    //是否主播虚拟充值
    private Integer anchorVirtual;
}
