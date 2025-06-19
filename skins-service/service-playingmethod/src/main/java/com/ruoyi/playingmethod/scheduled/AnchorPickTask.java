package com.ruoyi.playingmethod.scheduled;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtDeliveryRecordService;
import com.ruoyi.admin.service.TtUserBlendErcashService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.domain.common.constant.DeliveryPattern;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.delivery.TtDeliveryRecord;
import com.ruoyi.domain.entity.sys.TtUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling
public class AnchorPickTask {

    @Autowired
    TtUserService userService;

    @Autowired
    TtDeliveryRecordService deliveryRecordService;

    @Autowired
    TtUserBlendErcashService userBlendErcashService;

//    @Autowired
//    TtBoxRecordsService boxRecordsService;


    /**
     * 查询主播提货，超过4小时的发送弹药并且标记
     */
    @Scheduled(cron = "0 */1 * * * ?")
    private void task1() {

        // 获取4个小时前
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fourHourBefore = now.minusHours(4);

        List<TtDeliveryRecord> list = deliveryRecordService.list(Wrappers.lambdaQuery(TtDeliveryRecord.class)
                .eq(TtDeliveryRecord::getDelivery, DeliveryPattern.ANCHOR.getCode())
                .eq(TtDeliveryRecord::getPickCredits, 0)
                .lt(TtDeliveryRecord::getCreateTime, fourHourBefore));

        if (!list.isEmpty()){

            for (TtDeliveryRecord deliveryRecord : list) {

                //主播获得弹药
                TtUser user = userService.getById(deliveryRecord.getUserId());
                BigDecimal ornamentsPrice = deliveryRecord.getOrnamentsPrice();

                LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
                userUpdate
                        .eq(TtUser::getUserId, user.getUserId())
                        .setSql("account_credits = account_credits + " + ornamentsPrice);
                userService.update(userUpdate);

                // 综合消费日志
                TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                        .userId(user.getUserId())

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

                userBlendErcashService.save(blendErcash);

                deliveryRecord.setPickCredits(1);

            }

            //保存发货记录已发放弹药
            deliveryRecordService.saveOrUpdateBatch(list);

        }




    }

}
