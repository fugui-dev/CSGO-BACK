package com.ruoyi.thirdparty.zbt.result.user;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BusinessData {

    private String appKey;
    private String appSecret;
    private String applyNote;
    private String area;
    private BigDecimal balance;
    private BigDecimal balanceLack;
    private String callbackUrl;
    private String email;
    private String grantIpList;
    private String rejectReason;
    private String remark;
    private String status;
    private Integer successRate24;
    private String telephone;
    private Long total24Access;
    private Long totalAccess;
    private String weixin;
}
