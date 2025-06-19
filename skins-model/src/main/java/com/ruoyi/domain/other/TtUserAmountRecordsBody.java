package com.ruoyi.domain.other;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class TtUserAmountRecordsBody {

    private Integer userId;

    private String nickName;

    private String phoneNumber;

    // 收入1 支出0
    private Integer type;

    private Integer source;

    // 1金币 2弹药
    private Integer moneyType;

//    @NotNull(message = "页码不能为空")
//    @Min(value = 1, message = "最小值1")
    private Integer page;

//    @NotNull(message = "分页长度不能为空")
//    @Min(value = 1, message = "最小值1")
    private Integer size;
    private Integer limit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;


    private Integer pageSize;

    private Integer pageNum;
}
