package com.ruoyi.domain.dto.queryCondition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RollPrizesCondition {

    @NotNull(message = "rollId不能为空。")
    private Integer rollId;

    @Min(value = 1,message = "最小1")
    @NotNull(message = "page不能为空")
    private Integer page;

    @Max(value = 20,message = "最大20")
    @NotNull(message = "page不能为空")
    private Integer size;

    private Integer limit;

}
