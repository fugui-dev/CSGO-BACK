package com.ruoyi.domain.other;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class TtBoxRecordsBody {

    private Long id;
    private Integer userId;
    private Integer holderUserId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentsId;
    private String itemName;
    private String levelImg;
    private String status;
    private Integer boxId;
}
