package com.ruoyi.user.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SmeltRequest {

    @ApiModelProperty("要投入的背包内饰品ID")
    @NotEmpty(message = "投入饰品不能为空！")
    private List<Long> packageIds;

    @ApiModelProperty("熔炼目标饰品ID")
    @NotNull(message = "请选择熔炼目标！")
    private Long targetOrnament;


}
