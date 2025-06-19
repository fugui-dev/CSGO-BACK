package com.ruoyi.domain.dto.userRecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderCondition {

    private List<Integer> userIdList;

    @Min(value = 1,message = "最小1")
    private Integer page;

    @Min(value = 1,message = "最小1")
    private Integer size;

    private Integer limit;

    // 1时间升序 2 时间降序
    private Integer orderBy;

    @NotNull(message = "订单状态不能为空")
    private Integer status;

}
