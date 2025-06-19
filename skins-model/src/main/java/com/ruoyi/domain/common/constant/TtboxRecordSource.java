package com.ruoyi.domain.common.constant;

// 账户流水来源类型
// 注意和TtboxRecordSource区分
public enum TtboxRecordSource {

    // 玩转盲盒
    BLIND_BOX(1,"玩转盲盒"),

    // 急速对战
    FIGHT(2,"急速对战"),

    // roll房
    ROLL(3,"roll房"),

    // 急速对战
    UPGRADE(4,"幸运升级"),
    // roll房
    SYS_GRANT(5,"系统发放"),

    MALL_EXCHANGE(6,"商城兑换"),

    REPLACEMENT(7,"汰换"),

    RESISTER_GIFT(8,"注册赠送福利"),


    REAL_NAME_GIFT(21,"新人实名福利")

    ;

    private Integer code;

    private String msg;

    TtboxRecordSource(Integer code, String msg){
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
