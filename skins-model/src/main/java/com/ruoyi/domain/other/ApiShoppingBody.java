package com.ruoyi.domain.other;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
public class ApiShoppingBody {

    @ApiModelProperty("商品名称")
    private String name;

    @ApiModelProperty("类型（1匕首 2手枪 3步枪 4微型冲锋枪 5重型武器 6手套 7印花 8其它）")
    private String type;

    @ApiModelProperty("品质（1普通 2StatTrak™ 3纪念品 4★ 5★ StatTrak™ 6闪耀 7金色 8全息 9闪亮）")
    private String quality;

    @ApiModelProperty("稀有度")
    private String rarity;

    @ApiModelProperty("外观（1崭新出厂 2略有磨损 3久经沙场 4残损不堪 5战痕累累 6无涂装）")
    private String exterior;

    @ApiModelProperty("最高价格")
    private BigDecimal maxPrice;

    @ApiModelProperty("最低价格")
    private BigDecimal minPrice = BigDecimal.ZERO;

    @ApiModelProperty("1价格升序 2价格降序 3更新时间升序 4更新时间降序")
    private Integer sortBy;

    @Min(value = 1, message = "每页长度最小为1")
    private Integer pageSize;

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

}
