package com.ruoyi.domain.other;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FightBoutData {

    @ApiModelProperty("对战ID")
    private Integer fightId;

    @ApiModelProperty("对战回合号")
    private Integer boutNum;

    @ApiModelProperty("过期时间")
    private Integer expirationTime;
}
