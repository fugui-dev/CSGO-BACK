package com.ruoyi.domain.dto.userRecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class BlendErcashCondition {

    private Integer userId;

    private Integer source;

    private Integer type;

    private String userName;

    private Integer moneyType;

    @NotNull(message = "页码不能为空")
    @Min(value = 1,message = "最小1")
    private Integer page;

    @NotNull(message = "分页长度不能为空")
    @Min(value = 1,message = "最小1")
    @Max(value = 20,message = "最大20")
    private Integer size;

    private Integer limit;

}
