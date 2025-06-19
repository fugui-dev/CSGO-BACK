package com.ruoyi.domain.dto.sys;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderQueryCondition {

    private List<Integer> uidList;

    private List<Integer> statusList;

    // 时间区间
    private String beginTime;

    private String endTime;

    // 排序字段： time、totalAmount
    private String orderByFie;

    // 升1 降2
    private Integer orderByType;

    @Min(value = 1,message = "最小1")
    private Integer page;

    @Min(value = 1,message = "最小1")
    @Max(value = 20,message = "最大20")
    private Integer size;

    private Integer limit;

}
