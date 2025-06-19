package com.ruoyi.thirdparty.unifypaycallbackprocess.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopUpRatioVO {


    @ApiModelProperty("充值返现比例")
    private BigDecimal ratio;

    @ApiModelProperty("充值返现门槛")
    private BigDecimal threshold;

    public TopUpRatioVO() {
        this.ratio = BigDecimal.valueOf(0);
        this.threshold = BigDecimal.valueOf(0);
    }
}
