package com.ruoyi.thirdparty.baiduPromotion;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.admin.service.ITbPromotionChannelService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.entity.TbPromotionChannel;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.thirdparty.baiduPromotion.sdk.BaiduOcpcApiClient;
import com.ruoyi.thirdparty.baiduPromotion.util.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;


@Slf4j
@Component
public class BdPromotionProcess {

    @Autowired
    ITbPromotionChannelService promotionChannelService;

    @Autowired
    TtUserService userService;

    /**
     *
     * @param userId
     * @param logidUrl
     * @return 注册时上传线索信息
     */
    public void registerInfo(Integer userId, String logidUrl){

        //匹配域名，调用api
        TbPromotionChannel channel = baseProcess(logidUrl, LogidUrlEnum.REGISTER);
        if (channel == null){
            return;
        }

        //更新用户的渠道信息
        TtUser user = new TtUser();
        user.setUserId(userId);
        user.setBdChannelId(channel.getId());
        user.setBdChannelUrl(logidUrl);
        boolean update = userService.updateById(user);

        log.info("更新用户【{}】百度营销渠道为【{}】，操作成功？【{}】", userId, channel, update);

    }


    /**
     * 首充线索上传
     * @param orderNo
     * @return
     */
    public  void firstRecharge(TtOrder orderNo){
        //查询订单对应线索信息

        //匹配域名，调用api
        TbPromotionChannel channel = baseProcess(orderNo.getLogidUrl(), LogidUrlEnum.FIRST_RECHARGE);
        if (channel == null){
            log.info("首充订单【{}】未匹配到有效token渠道，停止处理...", orderNo.getOrderId());
            return;
        }

        log.info("百度营销用户首充流程【{}】处理完成==>", orderNo.getOrderId());

    }


    /**
     *
     * @param logidUrl
     * @return 注册时上传线索信息
     */
    public TbPromotionChannel baseProcess(String logidUrl, LogidUrlEnum logidUrlEnum){
        log.info("向百度营销推送线索数据流程开始==>logidUrl【{}】操作类型【{}】", logidUrl, logidUrlEnum.getCode());

        //1.通过logidUrl解析出域名
        String domain = UrlUtil.extractDomain(logidUrl);
        if (StringUtils.isBlank(domain)){
            log.info("提取url【{}】数据为空，停止任务...", logidUrl);
            return null;
        }

        //2.匹配对应域名查询出token
        TbPromotionChannel channel = new TbPromotionChannel();
        channel.setChannelDomain(domain);
        channel.setStatus(1);
        channel.setDelFlag(0);
        List<TbPromotionChannel> channelList = promotionChannelService.selectTbPromotionChannelList(channel);
        if (channelList.isEmpty()){
            log.info("未匹配到开启的推广渠道【{}】，停止任务...", domain);
            return null;
        }

        //有些渠道没有token，这种情况下不回传
        String token = channelList.get(0).getBdToken();
        if (StringUtils.isBlank(token)){
            return channelList.get(0);
        }

        //3.调用百度api上传
        JSONObject conversionItem = new JSONObject();
        conversionItem.put("logidUrl", logidUrl);
        conversionItem.put("newType", logidUrlEnum.getCode());
        ArrayList<JSONObject> conversionTypes = new ArrayList<>(1);
        conversionTypes.add(conversionItem);

        BaiduOcpcApiClient apiClient = new BaiduOcpcApiClient(token, conversionTypes);
        try {
            log.info("请求百度线索提供api，token==>【{}】，data==>【{}】", token,conversionTypes);
            JSONObject jsonObject = apiClient.uploadInfoApi();
            log.info("百度线索提供api响应==>【{}】", jsonObject);

            //FIXME 针对百度响应是否需要进一步处理？
            if ("success".equals(JSONObject.parseObject(jsonObject.get("header").toString()).get("desc"))){
                return channelList.get(0);
            }

        } catch (IOException e) {
            log.error("处理百度营销推广时出错==>", e);
        }

        return null;
    }

}
