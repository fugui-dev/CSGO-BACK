package com.ruoyi.promo.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 主播日流水
 */
@Data
public class AnchorDayTurnoverVO {

    private Long anchorId;

    private String anchorName;

    private BigDecimal turnover;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String beginTime;

    private String endTime;
}
