package com.ruoyi.domain.common.constant;

//福利类型
public enum BonusConditionType {

    // 日收入福利
    DAY(0),
    // 周收入福利
    WEEK(1),

    MONTH(2), //月充值福利

    TOTAL(3), //总充值

    SingleRecharge(4); //单笔充值

    private Integer code;

    BonusConditionType(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
