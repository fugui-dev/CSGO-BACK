package com.ruoyi.thirdparty.qspay.process;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.ruoyi.admin.mapper.TtOrderMapper;
import com.ruoyi.admin.mapper.TtRechargeProdMapper;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.domain.common.constant.PayOrderStatus;
import com.ruoyi.domain.common.constant.PayType;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.thirdparty.MaYi.service.MYService;
import com.ruoyi.thirdparty.common.service.RechargeSuccessfulNoticeService;
import com.ruoyi.thirdparty.qspay.client.QSPayClient;
import com.ruoyi.thirdparty.qspay.config.QSPayConfig;
import com.ruoyi.thirdparty.zhaocaipay.vo.UnifyPayPreOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


@Component
@Slf4j
public class QsPayProcess {

    @Autowired
    QSPayConfig qsPayConfig;

    @Autowired
    private TtRechargeProdMapper rechargeProdMapper;

    @Autowired
    private TtOrderMapper orderMapper;

    @Autowired
    private TtUserMapper userMapper;

    @Autowired
    private TtRechargeProdMapper rechargeListMapper;

    @Autowired
    private MYService myService;

    @Autowired
    private RechargeSuccessfulNoticeService rechargeSuccessfulNoticeService;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    /**
     * 预订单
     */
    public R preOrder(CreateOrderParam param, TtUser user, HttpServletRequest request){
        QSPayClient payClient = new QSPayClient(qsPayConfig);

        // 查询商品信息
        TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeProdMapper)
                .eq(TtRechargeProd::getId, param.getGoodsId())
                .eq(TtRechargeProd::getStatus, 0)
                .one();
        if (ObjectUtil.isEmpty(goods)) return R.fail("不存在的商品。");
        if (goods.getPrice().compareTo(param.getGoodsPrice()) != 0) return R.fail("商品价格不一致。");

        // 总价值
        BigDecimal totalAmount = param.getGoodsPrice().multiply(new BigDecimal(param.getGoodsNum()));

        //封装参数
        HashMap<String, String> map = new HashMap<>();
        String orderNo = UUID.randomUUID().toString();
        map.put("out_trade_no", orderNo); //系统订单号，非支付方订单号
        map.put("notify_url", qsPayConfig.getNotifyUrl());
        map.put("type", param.getPayType());
        map.put("name", goods.getName());
        map.put("money", totalAmount.toString());
        map.put("clientip", IpUtils.getIpAddr());
        map.put("sign_type", "MD5");

        //调用api请求方法
        try {
            //JSON后的响应体
            // {"code":1,"trade_no":"2024061016442736463","payurl":"https://abc.tianlicloud.com/pay/submit/2024061016442736463/"}
            String result = payClient.sendPaymentRequest(map);
            log.info("预下单请求响应==>【{}】", result);

            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject.get("code").equals(1)){

                //下单成功，插入订单
                // 创建订单
                TtOrder order = new TtOrder();
                order.setOrderId(orderNo); //系统订单号
                order.setOutTradeNo(jsonObject.get("trade_no").toString()); //三方订单号

                order.setUserId(user.getUserId());
                order.setType(PayType.QS_PAY.getCode());

                order.setGoodsId(param.getGoodsId());
                order.setGoodsPrice(param.getGoodsPrice());
                order.setGoodsNum(param.getGoodsNum());
                order.setTotalAmount(totalAmount);

                order.setSign("sign");
                order.setStatus(PayOrderStatus.NO_PAY.getCode());

                order.setPayUrl(jsonObject.get("qrcode").toString());

                order.setCreateTime(new Date());
                order.setUpdateTime(new Date());

                orderMapper.insert(order);

                return R.ok(new UnifyPayPreOrderVO(order.getPayUrl(), orderNo, order.getOutTradeNo()));
            }

        } catch (Exception e) {
            log.error("拉起支付失败==>", e);
        }

        return R.fail("拉起支付失败！");
    }


    /**
     * 回调处理
     * @return
     */
    public String notifyProcess(Map map){
        log.info("收到QSpay支付回调==>【{}】", map);

        //验签
        String sign = map.get("sign").toString();
        if (StringUtils.isBlank(sign)){
            log.error("无签名参数，放弃处理...");
            return null;
        }

        QSPayClient payClient = new QSPayClient(qsPayConfig);
        String checkSign = null;
        try {
            checkSign = payClient.generateSign(map);
        } catch (Exception e) {
            log.error("生成解析签名失败...");
            return null;
        }
        if (!sign.equals(checkSign)){
            log.error("签名【{}】, 验签【{}】", sign, checkSign);
            log.error("验签失败，放弃处理...");
            return null;
        }

        //处理订单
        log.error("验签通过，开始处理回调【{}】", map);
        TtOrder order = null;
        // TODO: 2024/4/12 最好再查询一下第三方平台的订单信息
        try {
            // 查询订单信息
            order = new LambdaQueryChainWrapper<>(orderMapper)
                    .eq(TtOrder::getOrderId, map.get("out_trade_no"))
                    .eq(TtOrder::getStatus, PayOrderStatus.NO_PAY.getCode())
                    .one();

            if (ObjectUtil.isEmpty(order)) {
                log.warn("支付回调异常，不存在的有效订单。");
                return "fail";
            }

            // 查询用户信息
            TtUser user = new LambdaQueryChainWrapper<>(userMapper)
                    .eq(TtUser::getUserId, order.getUserId())
                    .eq(TtUser::getDelFlag, 0)
                    .one();
            if (ObjectUtil.isEmpty(user)) {
                log.warn("支付回调异常，不存在的有效用户。");
                return "fail";
            }

            if (order.getStatus().equals(PayOrderStatus.PAY_COMPLE.getCode()) || order.getStatus().equals(PayOrderStatus.PAY_YET.getCode())) {
                log.warn("重复的回调！该订单已完成。");
                return "success";
            }

            // 查询商品信息
            TtRechargeProd goods = new LambdaQueryChainWrapper<>(rechargeListMapper)
                    .eq(TtRechargeProd::getId, order.getGoodsId())
                    .eq(TtRechargeProd::getStatus, 0)
                    .one();
            if (ObjectUtil.isEmpty(goods)) {
                log.warn("支付回调异常，不存在的商品。");
                return "fail";
            }

            // 账户结算
            if (ObjectUtil.isNull(goods.getProductA())) goods.setProductA(BigDecimal.ZERO);
            if (ObjectUtil.isNull(goods.getProductC())) goods.setProductC(BigDecimal.ZERO);
            myService.payNotifyAccounting(order, user, goods, order.getGoodsNum());

            // 首充赠送
            myService.firstChargeGiftAmount(user, goods, order.getGoodsNum());

            // 推广等级充值赠送
            myService.promotionLevelChargeGiftAmount(user, goods, order.getGoodsNum());

            // 更新订单
            new LambdaUpdateChainWrapper<>(orderMapper)
                    .eq(TtOrder::getId, order.getId())
                    .set(TtOrder::getStatus, PayOrderStatus.PAY_COMPLE.getCode())
                    .set(TtOrder::getUpdateTime, new Date())
                    .update();

            // 发送充值成功通知
            rechargeSuccessfulNoticeService.sendRechargeSuccessNotice(user.getUserId().toString(), order.getGoodsPrice());

            return "success";
        } catch (Exception e) {
            e.printStackTrace();

            log.warn("聚合支付，回调异常");

            if (ObjectUtil.isNotNull(order)) {
                new LambdaUpdateChainWrapper<>(orderMapper)
                        .eq(TtOrder::getId, order.getId())
                        .set(TtOrder::getStatus, PayOrderStatus.CALL_BACK_ERRO.getCode())
                        .set(TtOrder::getUpdateTime, new Date())
                        .update();
            }
            return "fail";
        }

    }


}
