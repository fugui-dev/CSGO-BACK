package com.ruoyi.domain.common.constant.sys;

//用户类型
public enum MoneyType {

    // 金币
    GOLD(1),
    // 弹药
    CREDITS(2);

    private final Integer code;

    MoneyType(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
