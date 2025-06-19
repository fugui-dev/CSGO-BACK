package com.ruoyi.domain.other;

import lombok.Data;

@Data
public class TtUserBody {

    private Integer userId;

    private String userName;

    private String nickName;

    private String userType;

    private String phoneNumber;

    private String status;

    private Long bdChannelId;

    //该参数仅作用前端传入是否结算主播佣金使用
    private Integer commissionRate;

    private Integer parentId;
}
