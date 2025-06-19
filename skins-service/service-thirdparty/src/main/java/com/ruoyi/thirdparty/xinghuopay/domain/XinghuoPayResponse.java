package com.ruoyi.thirdparty.xinghuopay.domain;

import lombok.Data;

@Data
public class XinghuoPayResponse {

    private String code;

    private String msg;

    private String tradeNo;

    private String payurl;

    private String qrcode;

    private String urlscheme;
}
