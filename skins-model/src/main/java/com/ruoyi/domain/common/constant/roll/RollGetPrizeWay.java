package com.ruoyi.domain.common.constant.roll;

// roll房玩家获奖方式
public enum RollGetPrizeWay {

    // 系统指定
    SYS(0),
    // 随机
    COMMON(1);

    private final Integer code;

    RollGetPrizeWay(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
