package com.ruoyi.domain.common.constant.roll;

// roll房状态
public enum RollType {

    // 主播
    ANCHOR("1"),
    // 官方
    OFFICIAL("0");

    private final String code;

    RollType(String code){
        this.code = code;
    }

    public String getCode(){
        return this.code;
    }

}
