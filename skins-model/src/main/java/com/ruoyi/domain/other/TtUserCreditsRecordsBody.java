package com.ruoyi.domain.other;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TtUserCreditsRecordsBody {

    private Integer userId;

    private String nickName;

    private String phoneNumber;

    private Integer type;

    private String source;

    private Integer page;

    private Integer size;

    private Integer limit;
}
