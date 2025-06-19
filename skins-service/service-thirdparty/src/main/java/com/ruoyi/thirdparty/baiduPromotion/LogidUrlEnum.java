package com.ruoyi.thirdparty.baiduPromotion;

public enum LogidUrlEnum {

    //注册49
    REGISTER(49),

    //充值10
    FIRST_RECHARGE(10);

    private Integer code;

    LogidUrlEnum(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
