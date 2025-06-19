package com.ruoyi.thirdparty.note.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.thirdparty.note.config.YunXinProperties;
import com.ruoyi.thirdparty.note.domain.YunXinNoteResult;
import com.ruoyi.thirdparty.note.service.YunXinNoteService;
import com.ruoyi.thirdparty.note.util.CheckSumBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@EnableConfigurationProperties(value = YunXinProperties.class)
@Slf4j
public class YunXinNoteServiceImpl implements YunXinNoteService {

    private final YunXinProperties yunXinProperties;

    public YunXinNoteServiceImpl(YunXinProperties yunXinProperties) {
        this.yunXinProperties = yunXinProperties;
    }

    @Override
    public String sendNote(String mobile, String nonce) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(yunXinProperties.getServerUrl() + "/sendcode.action");
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        try {
            String checkSum = CheckSumBuilder.getCheckSum(yunXinProperties.getAppSecret(), nonce, curTime);
            httpPost.addHeader("AppKey", yunXinProperties.getAppKey());
            httpPost.addHeader("Nonce", nonce);
            httpPost.addHeader("CurTime", curTime);
            httpPost.addHeader("CheckSum", checkSum);
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("templateid", yunXinProperties.getTemplateId()));
            nvps.add(new BasicNameValuePair("mobile", mobile));
            nvps.add(new BasicNameValuePair("authCode", nonce));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
            HttpResponse response = httpClient.execute(httpPost);
            String responseEntity = EntityUtils.toString(response.getEntity());
            YunXinNoteResult result = JSONObject.parseObject(responseEntity, YunXinNoteResult.class);
            if (result.getCode() == 200) return result.getObj();
        } catch (IOException e) {
            return "";
        }
        return "";
    }
}
