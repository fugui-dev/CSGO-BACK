package com.ruoyi.domain.common.constant;

// 提货方式
public enum DeliveryPattern {

    // 手动
    MANUAL(1),
    // 自动
    AUTO(2),
    // 主播提货
    ANCHOR(3);

    private Integer code;

    DeliveryPattern(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
