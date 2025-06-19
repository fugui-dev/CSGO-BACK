package com.ruoyi.thirdparty.wechat.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.admin.mapper.TtOrderMapper;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.admin.service.TtRechargeProdService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.thirdparty.wechat.config.TianXinProperties;
import com.ruoyi.thirdparty.wechat.domain.PayOrderParam;
import com.ruoyi.thirdparty.wechat.entity.TianXinOrder;
import com.ruoyi.thirdparty.wechat.entity.TtCoinRechargeRecord;
import com.ruoyi.thirdparty.wechat.entity.TtCoinRecord;
import com.ruoyi.thirdparty.wechat.entity.TtUserLsjl;
import com.ruoyi.thirdparty.wechat.mapper.TianXinMapper;
import com.ruoyi.thirdparty.wechat.service.ITtCoinRechargeRecordService;
import com.ruoyi.thirdparty.wechat.service.ITtCoinRecordService;
import com.ruoyi.thirdparty.wechat.service.ITtUserLsjlService;
import com.ruoyi.thirdparty.wechat.service.TianXinService;
import com.ruoyi.thirdparty.wechat.utils.DemoUtils;
import com.ruoyi.thirdparty.wechat.utils.GenerateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties(value = TianXinProperties.class)
@Slf4j
public class TianXinServiceImpl implements TianXinService {

    private final TianXinProperties tianXinProperties;
    private final TtRechargeProdService coinItemService;
    private final TtUserMapper userMapper;
    private final TianXinMapper tianXinMapper;
    private final ITtCoinRechargeRecordService ttCoinRechargeRecordService;
    private final ITtCoinRecordService recordService;
    private final ITtUserLsjlService userLsjlService;
    private final TtOrderMapper orderMapper;

    public TianXinServiceImpl(TianXinProperties tianXinProperties,
                              TtRechargeProdService coinItemService,
                              TtUserMapper userMapper,
                              TianXinMapper tianXinMapper,
                              ITtCoinRechargeRecordService ttCoinRechargeRecordService,
                              ITtCoinRecordService recordService,
                              ITtUserLsjlService userLsjlService,
                              TtOrderMapper orderMapper) {
        this.tianXinProperties = tianXinProperties;
        this.coinItemService = coinItemService;
        this.userMapper = userMapper;
        this.tianXinMapper = tianXinMapper;
        this.ttCoinRechargeRecordService = ttCoinRechargeRecordService;
        this.recordService = recordService;
        this.userLsjlService = userLsjlService;
        this.orderMapper = orderMapper;
    }

    @Override
    public AjaxResult createOrder(PayOrderParam param, TtUser ttUser, String ip) {

        long itemId = param.getCoinItemId();
        Integer itemNum = param.getCoinItemNum();
        BigDecimal payAmount = param.getCoinItemAmount();
        TtRechargeProd item =  coinItemService.getById(itemId);

        BigDecimal realPayAmout = item.getPrice().multiply(new BigDecimal(itemNum));
        if (realPayAmout.compareTo(payAmount) != 0) {
            return AjaxResult.error("支付金额不正确！");
        }

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("version", tianXinProperties.getVersion());  // 版本号
        map.add("customerid", tianXinProperties.getCustomerid());    // 商户编号
        map.add("sdorderno", GenerateUtils.getGuid("TX"));    //商户请求的唯一标识，推荐为uuid，必填  IdUtil hutool工具
        map.add("total_fee", realPayAmout);  // 订单金额
        map.add("paytype", tianXinProperties.getPaytype());  // 支付编号
        map.add("bankcode", "");    // 银行编号
        map.add("notifyurl", tianXinProperties.getNotifyurl());  // 异步通知URL
        map.add("clientip", ip); // 用户ip
        map.add("returnurl", tianXinProperties.getReturnurl());  // 同步通知URL
        map.add("remark", "会员充值");    // 订单备注
        map.add("access_type", tianXinProperties.getAccess_type());  // 接入方式
        String signStr = DemoUtils.sortMapByValues(map.toSingleValueMap());
        String sign = DemoUtils.encryptToMD5(signStr + "&" + tianXinProperties.getUserkey());
        map.add("sign", sign);
        map.add("is_jump", false);

        Map<String, String> resMap = DemoUtils.postFormData(map, tianXinProperties.getApiurl() + "/apisubmit");

        if (tianXinProperties.getAccess_type().equals("API")) {
            if (resMap.get("status").equals("OK")) {
                // tianxin_order表添加数据
//                TianXinOrder tianXinOrder = TianXinOrder.builder().build();
//                tianXinOrder.setOrderId(resMap.get("sdorderno"));   // 系统订单号(唯一)
//                tianXinOrder.setPayType("5");   // 支付方式:5微信扫码
//                tianXinOrder.setGoodsId(String.valueOf(itemId));   // 商户商品ID
//                tianXinOrder.setGoodsPrice(payAmount);   // 商户商品价格
//                tianXinOrder.setGoodsNum(itemNum);   // 商户商品数量
//                tianXinOrder.setTotalAmount(realPayAmout);  // 付款总价
//                tianXinOrder.setUserIp(ip);    // 用户IP
//                tianXinOrder.setPayStatus(0);  // 支付状态: 0待支付，1已支付，2,取消支付
//                tianXinOrder.setCreateTime(DateUtils.getNowDate());    // 创建时间
//                tianXinOrder.setUserId(Long.valueOf(ttUser.getUserId()));    // 订单发起用户ID
//                tianXinOrder.setUserName(ttUser.getUserName());  // 订单发起用户名
//                tianXinOrder.setRemark("待付款！"); // 备注
//                tianXinOrder.setCallBackOrderId(resMap.get("orderid")); // 回传单号
//                tianXinMapper.insertTianXinOrder(tianXinOrder);

                TtOrder ttOrder = TtOrder.builder().build();
                ttOrder.setUserId(ttUser.getUserId());
                ttOrder.setThirdParty("0");
                ttOrder.setType("2");
                ttOrder.setGoodsId((int)itemId);
                ttOrder.setGoodsPrice(payAmount);
                ttOrder.setGoodsNum(itemNum);
                ttOrder.setTotalAmount(realPayAmout);
                ttOrder.setOrderId(resMap.get("sdorderno"));
                ttOrder.setSign(sign);
                ttOrder.setStatus("0");
                ttOrder.setOutTradeNo(resMap.get("orderid"));
                ttOrder.setPayUrl(resMap.get("url"));
                ttOrder.setCreateTime(DateUtils.getNowDate());
                int isSuccess = orderMapper.insert(ttOrder);

                //充值记录的新增
//                TtCoinRechargeRecord coinRechargeRecord = new TtCoinRechargeRecord();
//                coinRechargeRecord.setCoin(param.getCoinItemAmount());
//                coinRechargeRecord.setPayStatus("0"); // 订单状态：0未付款，1已付款，2已取消'
//                coinRechargeRecord.setUid(Long.valueOf(ttUser.getUserId()));
//                coinRechargeRecord.setUname(ttUser.getUserName());
//                coinRechargeRecord.setCreateTime(DateUtils.getNowDate());
//                coinRechargeRecord.setOrderNo(tianXinOrder.getOrderId());
//                ttCoinRechargeRecordService.insertTtCoinRechargeRecord(coinRechargeRecord);

                return AjaxResult.success("订单创建成功！", resMap);
            } else {
                return AjaxResult.error("订单创建失败！");
            }
        }
        return AjaxResult.error("接入方式选择错误，请联系管理员！");
    }


    @Override
    public String callBack(HttpServletRequest request) {
        String sign_str = "customerid=" + request.getParameter("customerid")
                + "&status=" + request.getParameter("status")
                + "&sdpayno=" + request.getParameter("sdpayno")
                + "&sdorderno=" + request.getParameter("sdorderno")
                + "&total_fee=" + request.getParameter("total_fee")
                + "&realmoney=" + request.getParameter("realmoney")
                + "&paytype=" + request.getParameter("paytype")
                + "&" + tianXinProperties.getUserkey();
        String sign = DemoUtils.encryptToMD5(sign_str);

        // 订单ID
        String sdorderno = request.getParameter("sdorderno");
//        String sdorderno = "20231111160000000002TX";

        // 获取tianxin订单数据
        TianXinOrder tianXinOrder = tianXinMapper.selectTianXinOrderByOrderId(sdorderno);
        // 获取充值记录
        TtCoinRechargeRecord ttCoinRechargeRecord = ttCoinRechargeRecordService.selectTtCoinRechargeRecordByOrder(sdorderno);

        if ("1".equals(request.getParameter("status")) && sign.equals(request.getParameter("sign"))) {
            Enumeration<String> parameterNames = request.getParameterNames();
            Map<String, Object> parameterMap = new HashMap<>();
            while (parameterNames.hasMoreElements()) {
                String paraName = parameterNames.nextElement();
                parameterMap.put(paraName, request.getParameter(paraName));
            }

            try {
                tianXinOrder.setPayStatus(1);
                tianXinOrder.setCallBackMsg("支付成功！");  // 回传信息
                tianXinOrder.setCallBackStatus(request.getParameter("status"));   // 回传状态 1:成功，其他失败
                tianXinOrder.setSign(request.getParameter("sign"));   // sign
                tianXinOrder.setSubject("FireSkins平台" + request.getParameter("realmoney") + "充值!");    // 商品名
                tianXinOrder.setUpdateTime(DateUtils.getNowDate());
                tianXinOrder.setRemark("付款成功！"); // 备注
                tianXinMapper.updateTianXinOrder(tianXinOrder);

                ttCoinRechargeRecord.setPayStatus("1");
                ttCoinRechargeRecord.setCallbackNo(request.getParameter("sdpayno"));
                ttCoinRechargeRecord.setUpdateTime(DateUtils.getNowDate());
                ttCoinRechargeRecord.setCallbackMsg(JSONObject.toJSONString(parameterMap));
                ttCoinRechargeRecordService.updateTtCoinRechargeRecord(ttCoinRechargeRecord);

                //记录金币数
                TtCoinRecord record = new TtCoinRecord();
                record.setMoney(tianXinOrder.getTotalAmount());
                record.setOperType("3");
                record.setType("0");
                record.setUid(tianXinOrder.getUserId());
                record.setUname(tianXinOrder.getUserName());
                record.setCreateTime(new Date());
                record.setStatus(1l);
                recordService.insertTtCoinRecord(record);

                //记录流水的变更
                TtUser ttUser = userMapper.selectTtUserById(tianXinOrder.getUserId());
                TtUserLsjl userLsjl = new TtUserLsjl();
//                userLsjl.setTtSsUserId(ttUser.getSuperiorId());
                userLsjl.setType("充值");
//                userLsjl.setvCoinBefore(ttUser.getBean());
                userLsjl.setvCoinChange(record.getMoney());
//                userLsjl.setvCoinAfter(ttUser.getBean().add(record.getMoney()));
                //设置变动弹药情况
//                userLsjl.setdCoinBefore(ttUser.getStoreBean());
//                userLsjl.setdCoinAfter(ttUser.getStoreBean());
                userLsjl.setdCoinChange(BigDecimal.valueOf(0));
                userLsjl.setCreateTime(DateUtils.getNowDate());
                userLsjlService.insertTtUserLsjl(userLsjl);

                userMapper.updateTtUserCoin(tianXinOrder.getUserId(), record.getMoney());

                // 删除10分钟之前的无用数据
                tianXinMapper.deleteDataTianXinOrder();
                tianXinMapper.deleteDataTtCoinRechargeRecord();
                return "success";
            } catch (Exception e) {
                return "error";
            }

        } else {
            try {
                tianXinOrder.setPayStatus(2);
                tianXinOrder.setUpdateTime(new Date());
                tianXinMapper.updateTianXinOrder(tianXinOrder);
                ttCoinRechargeRecord.setPayStatus("2");
                ttCoinRechargeRecord.setUpdateTime(DateUtils.getNowDate());
                ttCoinRechargeRecordService.updateTtCoinRechargeRecord(ttCoinRechargeRecord);
            } catch (Exception e) {
                return "error";
            }
            return "error";
        }
    }

    @Override
    public AjaxResult queryOrderStatus(String sdorderno) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        String reqtime = String.valueOf(System.currentTimeMillis() / 1000);
        String signStr = "customerid=" + tianXinProperties.getCustomerid() + "&sdorderno=" + sdorderno + "&reqtime=" + reqtime;
        String sign = DemoUtils.encryptToMD5(signStr + "&" + tianXinProperties.getUserkey());
        map.add("customerid", tianXinProperties.getCustomerid());
        map.add("sdorderno", sdorderno);
        map.add("reqtime", reqtime);
        map.add("sign", sign);
        Map<String, String> resMap = DemoUtils.postFormData(map, tianXinProperties.getApiurl() + "/apiorderquery");
        return AjaxResult.success(resMap);
    }
}
