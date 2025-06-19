package com.ruoyi.domain.common.constant;

//用户类型
public enum UserType {

    // 主播
    ANCHOR("01"),
    // 普通用户
    COMMON_USER("02");

    private String code;

    UserType(String code){
        this.code = code;
    }

    public String getCode(){
        return this.code;
    }

}
