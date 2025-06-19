package com.ruoyi.domain.dto.packSack;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PackSackCondition {

    private List<Integer> uidList;

    private List<Integer> statusList;

    private String name;

    private String beginTime;

    private String endTime;

    @ApiModelProperty("排序字段（1获得时间 2价格）")
    private Integer orderByFie;

    @ApiModelProperty("价格排序（1升序 2降序）")
    private Integer orderByType;

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "最小1")
    private Integer page;

    @NotNull(message = "分页长度不能为空")
    @Min(value = 1, message = "最小1")
    @Max(value = 50, message = "最大20")
    private Integer size;

    private Integer limit;
}
