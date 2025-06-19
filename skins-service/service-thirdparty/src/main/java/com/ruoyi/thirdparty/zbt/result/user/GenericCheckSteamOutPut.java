package com.ruoyi.thirdparty.zbt.result.user;

import lombok.Data;

import java.util.List;

@Data
public class GenericCheckSteamOutPut {

    private Integer appId;
    private Integer checkStatus;
    private List<CheckStatusInfo> statusList;
    private UserSteamInfo steamInfo;
}
