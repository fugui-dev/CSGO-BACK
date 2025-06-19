package com.ruoyi.domain.dto.userRecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DeliveryRecordsConfition {

    private List<Integer> statusList;

    private List<Integer> uIdList;

    @Min(value = 1,message = "最小1")
    private Integer page;

    @Min(value = 1,message = "最小1")
    @Max(value = 20,message = "最大20")
    private Integer size;

    private Integer limit;

}
