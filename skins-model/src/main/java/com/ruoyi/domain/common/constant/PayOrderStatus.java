package com.ruoyi.domain.common.constant;

// 支付订单状态
public enum PayOrderStatus {

    /** 订单状态：-1超时未付款 0未付款，1已付款，2已取消 */

    // 0未付款
    NO_PAY("0"),

    // 用户支付成功
    PAY_YET("1"),

    // 用户支付失败
    PAY_FAIL("-1"),

    // 用户取消（超时未付款）
    CANCEL("2"),

    // 用户已支付，但回调处理异常
    CALL_BACK_ERRO("3"),

    // 支付完成
    PAY_COMPLE("4");

    private final String code;

    PayOrderStatus(String code){
        this.code = code;
    }

    public String getCode(){
        return this.code;
    }

}
