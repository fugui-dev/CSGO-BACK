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
public class GetRollPlayersParam {

    @NotNull(message = "不能为空")
    private Integer rollId;

    @Min(value = 1, message = "页码最小为1")
    private Integer page;

    @Min(value = 1, message = "长度最小为1")
    @Max(value = 20, message = "长度最大为20")
    private Integer size;

    private Integer limit;
}
