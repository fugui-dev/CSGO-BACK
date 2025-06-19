package com.ruoyi.thirdparty.zbt.result.product;

import lombok.Data;

@Data
public class SellerUserInfoVO {

    private String avatar;
    private DeliveryStatsInfoDTO deliveryInfo;
    private String lastActive;
    private String nickname;
    private Integer platformId;
    private String thirdUserId;
    private String userId;
    private String verified;
}
