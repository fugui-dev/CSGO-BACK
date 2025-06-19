package com.ruoyi.domain.other;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApiLuckyUpgradeBody {

    private Integer id;

    @ApiModelProperty("分类：1匕首 2手枪 3步枪 4微型冲锋枪 5机枪 6手套 7印花 8武器箱")
    private String type;

    private String itemName;

    private BigDecimal priceMin = BigDecimal.ZERO;

    private BigDecimal priceMax;

    @ApiModelProperty("分页大小")
    private Integer size;

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("是否品质降序 0升序 1降序")
    private Integer isLevelDesc;

    @ApiModelProperty("是否价格降序 0升序 1降序")
    private Integer isPriceDesc;

}
