package com.ruoyi.domain.common.constant;

// 第三方平台
public enum PartyType {

    // 发货平台-----------------
    // 扎比特
    ZBT(1),

    // yy有品
    YY_YOU_PING(2),

    // 支付平台------------------
    // 支付宝
    ZFB(3),

    // 微信
    VX(4),

    // 聚合支付
    JU_HE_ZHI_FU(5),

    // 九嘉支付宝
    JIU_JIA_ZFB(6);

    private final Integer code;

    PartyType(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
