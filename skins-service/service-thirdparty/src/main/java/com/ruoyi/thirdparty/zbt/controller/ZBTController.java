package com.ruoyi.thirdparty.zbt.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.mapper.TtUserBlendErcashMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtDeliveryRecordService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.delivery.TtDeliveryRecord;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.thirdparty.zbt.result.ResultZbt;
import com.ruoyi.thirdparty.zbt.result.user.BusinessData;
import com.ruoyi.thirdparty.zbt.result.user.UserAccountModel;
import com.ruoyi.thirdparty.zbt.service.ZBTService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;

import static com.ruoyi.domain.common.constant.DeliveryOrderStatus.DELIVERY_AFTER;
import static com.ruoyi.domain.common.constant.DeliveryOrderStatus.ORDER_COMPLETE;

@Api(tags = "扎比特")
@RestController
@RequestMapping
@Slf4j
public class ZBTController {

    private final ZBTService zbtService;

    @Autowired
    private TtDeliveryRecordService deliveryRecordService;

    @Autowired
    private TtBoxRecordsService boxRecordsService;

    @Autowired
    private TtUserBlendErcashMapper userBlendErcashMapper;

    @Autowired
    private TtUserService userService;

    public ZBTController(ZBTService zbtService) {
        this.zbtService = zbtService;
    }

    @ApiOperation("管理端查询用户余额")
    @GetMapping("/admin/zbt/balance")
    public R<UserAccountModel> balance() {
        ResultZbt<UserAccountModel> balance = zbtService.balance();
        if (balance.getSuccess()) return R.ok(balance.getData());
        return R.fail("用户余额查询失败");
    }

    @ApiOperation("管理端获取开发者信息")
    @GetMapping("/admin/zbt/developmentInfo")
    public R<BusinessData> developmentInfo() {
        ResultZbt<BusinessData> developmentInfo = zbtService.developmentInfo();
        if (developmentInfo.getSuccess()) return R.ok(developmentInfo.getData());
        return R.fail("获取用户开发者账号相关信息失败");
    }


    /**
     * zbt购买回调
     *
     * @param requestBody
     * @return
     */
    @Anonymous
    @PostMapping("/notify/zbt")
    @ApiOperation("ZBT购买回调")
    public String handleZbtCallback(@RequestBody Map<String, String> requestBody) {
        log.info("ZBT回调信息主体{}",requestBody.toString());
        // 从请求体中获取 ZBT 提供的签名
        String notifySign = requestBody.get("sign");
        log.info("zbt返回的签名{}",notifySign);
        if (StringUtils.isEmpty(notifySign)) {
            return "fail";
        }
        //去除sign
        requestBody.remove("sign");
        // 在这里进行签名验证，与请求中的签名进行比较
        // 构建 ASCII 字符串， 调用方法对 Map 进行 ASCII 码排序
        String sortedString = ascii(requestBody);
        ResultZbt<BusinessData> developmentInfoResultZbt = zbtService.developmentInfo();
        sortedString = sortedString + "&sign=" + developmentInfoResultZbt.getData().getAppSecret();
        String sign = generateMD5(sortedString);
        log.info("sign签名{}",sign);
        // 将计算得到的签名与接收到的签名进行比较,验证ZBT签名防止伪造通知
        if (requestBody.get("type").equals("0") && sign.equals(notifySign)) {
            //获取申请记录详情，获取申请详情
            String outTradeNo = String.valueOf(requestBody.get("outTradeNo"));
            TtDeliveryRecord ttDeliveryRecord = deliveryRecordService.getOne(Wrappers.lambdaQuery(TtDeliveryRecord.class)
                    .eq(TtDeliveryRecord::getOutTradeNo , outTradeNo), false);
            log.info("提货记录==={}",ttDeliveryRecord);

            TtBoxRecords ttBoxRecord = boxRecordsService.getById(ttDeliveryRecord.getBoxRecordsId());
            log.info("ttBoxRecord==={}",ttBoxRecord);

            // 根据回调状态进行操作
            int status = Integer.parseInt(requestBody.get("status"));
            log.info("回调状态==={}",status);
            try {
                switch (status) {
                    case 1:
                        //1 等待发货, 现在状态1也会推送，意味着这笔订单购买成功了
                        ttDeliveryRecord.setStatus(DELIVERY_AFTER.getCode());
                        ttDeliveryRecord.setMessage(DELIVERY_AFTER.getMsg());

                        break;
                    case 3:
                        //3 等待收货，意味着可以通知你们平台的用户去接受报价了
//                        ttDeliveryRecord.setStatus(1);
                        ttDeliveryRecord.setStatus(DELIVERY_AFTER.getCode());
                        ttDeliveryRecord.setMessage(DELIVERY_AFTER.getMsg());


                        if (ttBoxRecord != null){
                            ttBoxRecord.setStatus(2); //已提取
                            boxRecordsService.updateById(ttBoxRecord);
                        }
                        break;
                    case 10:
                        //10 成功
                        ttDeliveryRecord.setStatus(ORDER_COMPLETE.getCode());
                        ttDeliveryRecord.setMessage(ORDER_COMPLETE.getMsg());

                        if (ttBoxRecord != null){
                            ttBoxRecord.setStatus(2); //已提取
                            boxRecordsService.updateById(ttBoxRecord);


                            BigDecimal ornamentsPrice = ttBoxRecord.getOrnamentsPrice();
                            //用户获得弹药 FIXME 这里直接更新用户有可能会造成金额不一致问题，改成sql累加最好
                            TtUser user = userService.getById(ttBoxRecord.getHolderUserId());
                            user.setAccountCredits(user.getAccountCredits().add(ornamentsPrice));
                            userService.updateById(user);

                            // 综合消费日志
                            TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                                    .userId(ttBoxRecord.getHolderUserId())

                                    // .amount(ObjectUtil.isNotEmpty(total) ? total : null)
                                    // .finalAmount(ObjectUtil.isNotEmpty(Amount) ? userById.getAccountAmount().subtract(Amount) : null)

                                    .credits(ObjectUtil.isNotEmpty(ornamentsPrice) ? ornamentsPrice : null)
                                    .finalCredits(ObjectUtil.isNotEmpty(ornamentsPrice) ? user.getAccountCredits().add(ornamentsPrice) : null)

                                    .total(ornamentsPrice)  // 收支合计

                                    .type(TtAccountRecordType.INPUT.getCode())
                                    .source(TtAccountRecordSource.DECOMPOSE_ORNAMENT.getCode())
                                    .remark(TtAccountRecordSource.DECOMPOSE_ORNAMENT.getMsg())

                                    .createTime(new Timestamp(System.currentTimeMillis()))
                                    .updateTime(new Timestamp(System.currentTimeMillis()))
                                    .build();

                            userBlendErcashMapper.insert(blendErcash);

                        }
                        break;
                    case 11:
                        //11 订单取消或订单失败
                        ttDeliveryRecord.setStatus(11);

                        if (ttBoxRecord != null){
                            ttBoxRecord.setStatus(0); //背包
                            boxRecordsService.updateById(ttBoxRecord);
                        }
                        break;
                    default:
                        // 处理其他状态的逻辑
                        break;
                }
                //更新提取记录状态
                deliveryRecordService.updateById(ttDeliveryRecord);

                log.error("回调成功{}",requestBody.toString());
                return "success";
            } catch (Exception e) {
                // 处理异常情况
                log.error("回调失败");
                e.printStackTrace();
                return "fail";
            }
        }
        log.info("ZBT回调签名错误");
        log.error("本站签名{}",sign);
        log.error("回调签名{}",notifySign);
        return "fail";
    }

    public  String ascii(Map<String, String> params) {
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        StringBuilder str = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String val = entry.getValue() == null ? "null" : entry.getValue();
            str.append(entry.getKey()).append('=').append(val).append('&');
        }

        return str.toString().replaceAll("&$", ""); // 移除末尾的'&'
    }

    private static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();

            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString().toUpperCase(); // 将结果转为大写
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


}
