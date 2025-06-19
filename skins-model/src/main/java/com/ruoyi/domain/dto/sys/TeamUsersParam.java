package com.ruoyi.domain.dto.sys;

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
public class TeamUsersParam {

    private String userName;
    private List<Integer> bossIds;
    private List<Integer> employeeIds;

    // 排序字段 1、充值金额 2、综合消费 3、金币消费 4、弹药消费
    private Integer orderByFie;
    // 1升 2降
    private Integer orderType;

    // 时间区间 格式：yyyy-MM-dd HH:mm:ss
    private String beginTime;
    private String endTime;

    @Min(value = 1,message = "最小值1")
    private Integer page;
    @Min(value = 1,message = "最小值1")
//    @Max(value = 20,message = "最大值20")
    private Integer size;
}
