package com.ruoyi.domain.other;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateOrderParam {

    @NotNull(message = "商品id不能为空。")
    private Integer goodsId;

    @NotNull(message = "商品价格不能为空。")
    private BigDecimal goodsPrice;

    @NotNull(message = "商品数量不能为空。")
    @Min(value = 1, message = "最小值1")
    private Integer goodsNum;

    @ApiModelProperty("支付方式（alipay/wxpay）如果是yima聚合支付，20：微信扫码支付(用户主扫)\\n\" +\n" +
            "            \"21：微信APP支付\\n\" +\n" +
            "            \"22：微信JSAPI（适用于公众号或小程序）\\n\" +\n" +
            "            \"30：支付宝扫码支付(用户主扫)\\n\" +\n" +
            "            \"31：支付宝APP支付\\n\" +\n" +
            "            \"40：快捷支付(扫码)\\n\" +\n" +
            "            \"50：H5跳转微信小程序支付")
    private String payType;

    @ApiModelProperty("对应百度推广的线索url")
    private String logidUrl;

}
