package com.ruoyi.domain.common.constant.roll;

// roll房状态
public enum RollStatus {

    // 未开奖
    ING("0"),
    // 已开奖
    END("1");

    private final String code;

    RollStatus(String code){
        this.code = code;
    }

    public String getCode(){
        return this.code;
    }

}
