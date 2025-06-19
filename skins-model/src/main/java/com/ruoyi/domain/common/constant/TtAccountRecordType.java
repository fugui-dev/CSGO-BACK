package com.ruoyi.domain.common.constant;

// 流水类型
public enum TtAccountRecordType {

    // 收入
    INPUT(1),
    // 支出
    OUTPUT(0);

    private Integer code;

    TtAccountRecordType(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
