package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ApiLuckyUpgradeRecordDataVO {

    private String nickName;

    private String avatar;

    private String probability;

    private BigDecimal amountConsumed;

    private String gainItemName;

    // 获得饰品价值
    private BigDecimal gainOrnamentsPrice;

    private String imageUrl;

    private Boolean isVictory;

    // private String result;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date openTime;
}
