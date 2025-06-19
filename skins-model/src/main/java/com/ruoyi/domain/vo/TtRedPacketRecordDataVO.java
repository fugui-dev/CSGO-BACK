package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class TtRedPacketRecordDataVO {

    private Integer id;

    private Integer redPacketId;

    private String title;

    private String receivePassword;

    private Integer userId;

    private String nickName;

    private BigDecimal receiveAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;
}
