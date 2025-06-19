package com.ruoyi.domain.common.constant;

// 开箱记录状态/背包饰品状态
public enum TtboxRecordStatus {

    IN_PACKSACK_ON(0,"在背包显示"),

    // 用于对战模式，游戏完全结束才显示
    IN_PACKSACK_OFF(1,"在背包不显示"),
    APPLY_DELIVERY(2,"申请提货"),
    DELIVERY_YET(3,"已经提货"),
    RESOLVE(5,"注入分解"),
    ADMIN_DELETE(10,"管理员删除"),

    SMELT(8,"已投入汰换（熔炼）");

    private final Integer code;

    private final String msg;

    TtboxRecordStatus(Integer code, String msg){
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
