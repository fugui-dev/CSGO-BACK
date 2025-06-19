package com.ruoyi.playingmethod.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Data
public class OpenBox2Request {

    @ApiModelProperty("要开启的箱子ID")
    @NotNull(message = "数量不能为空！")
    private Integer boxId;


    @ApiModelProperty("开启数量")
    @NotNull(message = "数量不能为空！")
    @Min(value = 1)
    @Max(value = 5)
    private Integer num;

    @ApiModelProperty("要注入的背包饰品ID")
    @NotEmpty(message = "注入的背包饰品不能为空！")
    private Set<Long> packageIds;

}
