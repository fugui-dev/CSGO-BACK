package com.ruoyi.domain.dto.packSack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DecomposeLogCondition {

    @Min(value = 1,message = "最小值1")
    private Integer page;

    @Min(value = 1,message = "最小值1")
    @Max(value = 20,message = "最大值20")
    private Integer size;

    private Integer boxRecordStatus;
    private Integer limit;

    private Integer userId;

}
