package com.ruoyi.common.constant.ThirdParty;

public enum Platform {

    ZBT(1),
    YY(2);

    private Integer code;

    Platform(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }
}
