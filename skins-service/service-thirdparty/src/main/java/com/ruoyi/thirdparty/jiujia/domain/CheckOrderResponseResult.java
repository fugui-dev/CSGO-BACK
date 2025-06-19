package com.ruoyi.thirdparty.jiujia.domain;

import lombok.Data;

@Data
public class CheckOrderResponseResult<T> {

    private Integer code;
    private String msg;
    private T data;
}
