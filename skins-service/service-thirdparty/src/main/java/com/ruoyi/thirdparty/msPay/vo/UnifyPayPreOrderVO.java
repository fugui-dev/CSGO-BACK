package com.ruoyi.thirdparty.msPay.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnifyPayPreOrderVO {

    @ApiModelProperty("支付url")
    private String payUrl;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("支付方订单号")
    private String outTradeNo;


}
