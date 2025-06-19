package com.ruoyi.domain.other;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class CreateFightBody {

    @ApiModelProperty("模式（0欧皇 1非酋）")
    @NotBlank(message = "模式不能为空")
    private String model;

    @ApiModelProperty("玩家人数")
    @Max(value = 12, message = "最大值12")
    private Integer playerNumber;

    // 所有宝箱
    @NotNull(message = "宝箱数据不能为空")
    private Map<Integer, Integer> boxIdAndNumber;

    // private Integer rounds;
    // 回合数
    // private Integer createNum = 1;

}
