package com.ruoyi.thirdparty.alipay.service;

public interface RealNameAuthenticationService {

    String authInitialize(String realName, String idNum);

    String startCertify(String certifyId);

    String queryCertifyResult(String certifyId);
}
