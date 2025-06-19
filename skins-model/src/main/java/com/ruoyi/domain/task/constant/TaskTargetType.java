package com.ruoyi.domain.task.constant;

public enum TaskTargetType {

    CREDITS(1),
    DOWNLOAD(2);

    private Integer code;

    TaskTargetType(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

}
