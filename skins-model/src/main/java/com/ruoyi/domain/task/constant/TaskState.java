package com.ruoyi.domain.task.constant;

public enum TaskState {

    UP(1),
    DOWN(2);

    private Integer code;

    TaskState(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
