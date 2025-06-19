package com.ruoyi.thirdparty.MaYi.utils;

// zy返回值

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ZYHTTPRes {

    public enum resCode{

        success("0000"),
        DOING("0001");

        private String code;

        resCode(String code){
            this.code = code;
        }
        public String getCode(){
            return this.code;
        }
    }

    private String respCode;
    //返回码说明
    private String respDesc;
    //返回码
    private String payCode;
    private String payDesc;
    private String amt;
    private String tranTime;
    private String tranType;
    //扩展信息
    private String msgExt;
    private String payUrl;
    // 交易流水号
    private String merReqNo;
    //服务端响应流水号
    private String serverRspNo;
    private String sign;

}
