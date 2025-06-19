package com.ruoyi.domain.task.constant;

public enum TaskCompletionState {

    COMPLETION(1),
    DOING(2),
    TIME_OUT(3),
    COMPLETION_PRIZE(4);

    private Integer code;

    TaskCompletionState(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }
}
