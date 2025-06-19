package com.ruoyi.thirdparty.yyyouping.utils.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class YYResult {

    private Integer code;
    private String msg;
    private Long timestamp;
    private Object data;

    public boolean isSuccess(){
        if (this.code.equals(0)){
            return true;
        }
        return false;
    }

}
