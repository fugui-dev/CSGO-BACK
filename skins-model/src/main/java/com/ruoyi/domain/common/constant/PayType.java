package com.ruoyi.domain.common.constant;

// 支付方式
public enum PayType {

    // 中云 支付宝
    ZY_ZFB("1"),

    // 卡密
    MK_CARD("2"),

    // 支付宝
    ZFB("3"),

    // 微信
    VX("4"),

    // 聚合支付(蚂蚁支付)
    JU_HE_ZHI_FU("5"),

    // 九嘉支付宝
    JIU_JIA_ZFB("6"),

    // 星火支付宝
    XIN_HUO_ZFB("7"),

    //QS聚合支付
    QS_PAY("8"),

    //招财支付
    ZC_PAY("9"),

    AB_PAY("10"),

    CS_PAY("11"),

    YS_PAY("12"),

    MS_PAY("13"),

    YM_PAY("14");

    private final String code;

    PayType(String code){
        this.code = code;
    }

    public String getCode(){
        return this.code;
    }

}
