package com.ruoyi.domain.common.constant;

// 发货订单状态（0发起提货 1待发货 3待收货 10订单完成 11订单取消）
public enum DeliveryOrderStatus {

    // 0发起提货(多余)
    //CREATE_ORDER(0,"0发起提货"),

    // 1待发货
    DELIVERY_BEFORE(1,"1待发货"),

    // 2已发货
    DELIVERY_AFTER(2,"2已发货"),

    // 3客户确认已收货
    OWN_BEFORE(3,"3客户确认已收货"),

    // // 4客户确认已收货
    // OWN_BEFORE(4,"3客户确认已收货"),

    // 10订单完成
    ORDER_COMPLETE(10,"10订单完成"),

    //11订单取消
    ORDER_CANCEL(11,"11订单取消"),

    // 12订单异常冻结
    ORDER_FREEZE(12,"12订单异常冻结");

    private final Integer code;
    private final String msg;

    DeliveryOrderStatus(Integer code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode(){
        return this.code;
    }
    public String getMsg(){
        return this.msg;
    }

}
