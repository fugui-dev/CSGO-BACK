package com.ruoyi.domain.dto.roll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetRollPrizePool {

    @NotNull(message = "不能为空")
    private Integer rollId;

    private Integer jackpotId;

    @Min(value = 1,message = "最小1")
    private Integer page;

    @Min(value = 1,message = "最小1")
    @Max(value = 20,message = "最大20")
    private Integer size;

    private Integer limit;
}
