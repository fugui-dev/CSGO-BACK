package com.ruoyi.common.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class UserData {

    private Integer userId;
    private String userName;
    private String nickName;
    private String userType;
    private Integer vipLevel;
    private Integer promotionLevel;
    private String password;
    private String email;
    private String avatar;
    private BigDecimal accountAmount;
    private BigDecimal accountCredits;
    private String invitationCode;
    private String parentInvitationCode;
    private Long steamId;
    private String transactionLink;
    private String isRealCheck;
    private String realName;
}
