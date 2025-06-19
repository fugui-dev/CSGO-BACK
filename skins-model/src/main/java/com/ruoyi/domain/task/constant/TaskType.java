package com.ruoyi.domain.task.constant;

public enum TaskType {

    COMMON(1),
    DAY(2),
    WEEK(3),
    MONTH(4);

    private Integer code;

    TaskType(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
