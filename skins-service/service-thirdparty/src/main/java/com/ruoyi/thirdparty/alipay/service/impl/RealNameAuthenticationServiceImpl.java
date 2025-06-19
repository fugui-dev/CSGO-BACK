package com.ruoyi.thirdparty.alipay.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayUserCertifyOpenCertifyRequest;
import com.alipay.api.request.AlipayUserCertifyOpenInitializeRequest;
import com.alipay.api.request.AlipayUserCertifyOpenQueryRequest;
import com.alipay.api.response.AlipayUserCertifyOpenCertifyResponse;
import com.alipay.api.response.AlipayUserCertifyOpenInitializeResponse;
import com.alipay.api.response.AlipayUserCertifyOpenQueryResponse;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.thirdparty.alipay.config.AliProperties;
import com.ruoyi.thirdparty.alipay.service.RealNameAuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
@Slf4j
public class RealNameAuthenticationServiceImpl implements RealNameAuthenticationService {

    @Qualifier("RealNameAuthentication")
    private final AlipayClient RealNameAuthentication;
    private final AliProperties aliProperties;

    public RealNameAuthenticationServiceImpl(AlipayClient realNameAuthentication,
                                             AliProperties aliProperties) {
        RealNameAuthentication = realNameAuthentication;
        this.aliProperties = aliProperties;
    }

    @Override
    public String authInitialize(String realName, String idNum) {

        AlipayUserCertifyOpenInitializeRequest request = new AlipayUserCertifyOpenInitializeRequest();
        JSONObject identityObj = new JSONObject();
        identityObj.put("identity_type", "CERT_INFO");
        identityObj.put("cert_type", "IDENTITY_CARD");
        identityObj.put("cert_name", realName);
        identityObj.put("cert_no", idNum);
        JSONObject merchantConfigObj = new JSONObject();
        try {
            merchantConfigObj.put("return_url", aliProperties.getRealNameAuthenticationReturnUrl());
            JSONObject bizContentObj = new JSONObject();
            bizContentObj.put("outer_order_no", IdUtil.simpleUUID());
            bizContentObj.put("biz_code", "FACE");
            bizContentObj.put("identity_param", identityObj);
            bizContentObj.put("merchant_config", merchantConfigObj);
            request.setBizContent(bizContentObj.toString());
            AlipayUserCertifyOpenInitializeResponse response = RealNameAuthentication.execute(request);
            if (response.isSuccess()) {
                return response.getCertifyId();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String startCertify(String certifyId) {
        AlipayUserCertifyOpenCertifyRequest request = new AlipayUserCertifyOpenCertifyRequest();
        JSONObject bizContentObj = new JSONObject();
        bizContentObj.put("certify_id", certifyId);
        request.setBizContent(bizContentObj.toString());
        try {
            AlipayUserCertifyOpenCertifyResponse response = RealNameAuthentication.pageExecute(request, "GET");
            if (response.isSuccess()) {
                return "alipays://platformapi/startapp?appId=20000067&url=" + URLEncoder.encode(response.getBody(), "UTF-8");
            } else {
                return null;
            }
        } catch (AlipayApiException | UnsupportedEncodingException e) {
            return null;
        }
    }

    @Override
    public String queryCertifyResult(String certifyId) {
        AlipayUserCertifyOpenQueryRequest request = new AlipayUserCertifyOpenQueryRequest();
        JSONObject bizContentObj = new JSONObject();
        bizContentObj.put("certify_id", certifyId);
        request.setBizContent(bizContentObj.toString());
        try {
            AlipayUserCertifyOpenQueryResponse response = RealNameAuthentication.execute(request);
            if (StrUtil.equals(response.getCode(), "40004")) {
                return response.getSubMsg();
            }
            if (response.isSuccess()) {
                String body = response.getBody();
                log.info("返回的结构体：{}", body);
                if (StringUtils.isNotBlank(body)) {
                    JSONObject jsonObject = JSONObject.parseObject(body);
                    JSONObject queryResponse = jsonObject.getJSONObject("alipay_user_certify_open_query_response");
                    if (StrUtil.equals(queryResponse.getString("code"), "10000") && StrUtil.equals(queryResponse.getString("passed"), "T")) {
                        return "";
                    } else return "认证失败,请重新认证！";
                }
            } else return "认证查询服务调用失败！";
        } catch (AlipayApiException e) {
            return "开始认证服务调用失败！";
        }
        return "认证失败,请重新认证！";
    }
}
