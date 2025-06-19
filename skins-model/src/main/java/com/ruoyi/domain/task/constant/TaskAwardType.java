package com.ruoyi.domain.task.constant;

// 任务奖励类型
public enum TaskAwardType {

    // 弹药
    CREDITS(1),

    // 其他
    OTHER(2);

    private Integer code;

    TaskAwardType(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
