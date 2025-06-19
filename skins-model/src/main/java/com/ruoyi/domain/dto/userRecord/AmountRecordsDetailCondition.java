package com.ruoyi.domain.dto.userRecord;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class AmountRecordsDetailCondition {

    private Integer userId;

    private String nickName;

    private String phoneNumber;

    // 收入1 支出0
    private Integer type;

    private String source;

    // 1金币 2弹药
    @NotNull(message = "moneyType不能为空")
    private Integer moneyType;

    @Min(value = 1,message = "最小值1")
    private Integer page;

    @Min(value = 1,message = "最小值1")
    private Integer size;
    private Integer limit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
