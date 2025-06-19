package com.ruoyi.domain.task.constant.mq;

public enum MQMoudle {

    PROMOTION_WELFARE_QUEUE("pWelfareQueue"),
    PROMOTION_WELFARE_EXCHANGE("pWelfareExchange"),
    PROMOTION_WELFARE_KEY1("pwKey1");

    private String value;

    MQMoudle(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }

}
