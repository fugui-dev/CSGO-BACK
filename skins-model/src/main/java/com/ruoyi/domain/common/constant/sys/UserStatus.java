package com.ruoyi.domain.common.constant.sys;

public enum UserStatus {

    // 金币
    NORMAL(0);

    private final Integer code;

    UserStatus(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
