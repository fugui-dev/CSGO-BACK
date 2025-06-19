package com.ruoyi.framework.websocket.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultData<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;

    private String typeName;

    private T data;
}
