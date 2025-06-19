package com.ruoyi.thirdparty.zbt.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultZbt<T> {
    private T data;
    private Integer errorCode;
    private Object errorData;
    private String errorMsg;
    private Boolean success;

    public static ResultZbt faild() {
        return new ResultZbt(null,500,null,"请求出错",false);
    }

}
