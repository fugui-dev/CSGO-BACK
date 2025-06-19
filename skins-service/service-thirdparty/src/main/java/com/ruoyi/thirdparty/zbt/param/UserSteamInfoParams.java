package com.ruoyi.thirdparty.zbt.param;

import lombok.Data;

@Data
public class UserSteamInfoParams {

    private String appId;
    private String type;
    private String appKey;
    private String language;
    private String steamId;
    private String tradeUrl;
}
