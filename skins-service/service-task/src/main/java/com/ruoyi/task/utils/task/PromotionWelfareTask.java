package com.ruoyi.task.utils.task;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.task.DTO.pWelfareMQData;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

//

@AllArgsConstructor
public class PromotionWelfareTask implements Runnable {

    private TtUserService userService;

    private TtUser user;

    // 消费金额
    private BigDecimal price;

    @Override
    public void run() {
        // 统计推广福利
        pWelfareMQData msgDate = pWelfareMQData.builder()
                .userId(user.getUserId())
                .account(price)
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();

        TtUser user = userService.getById(msgDate.getUserId());
        TtUser parent = userService.getById(user.getParentId());
        if (ObjectUtil.isNotEmpty(parent)){
            BigDecimal credits = null;
            if ("01".equals(parent.getUserType())) {
                credits = msgDate.getAccount().multiply(new BigDecimal("0.045"));
            } else {
                credits = msgDate.getAccount().multiply(new BigDecimal("0.01"));
            }
            LambdaUpdateWrapper<TtUser> wrapper = new LambdaUpdateWrapper<>();
            wrapper
                    .eq(TtUser::getUserId,parent.getUserId())
                    .set(TtUser::getAccountCredits,parent.getAccountCredits().add(credits));
                    // .set(TtUser::getAccountAmount,parent.getAccountAmount().add(credits));
            userService.update(wrapper);
            // userService.insertUserCreditsRecords(parent.getUserId(),
            //         TtAccountRecordType.INPUT,
            //         TtAccountRecordSource.P_WELFARE,
            //         credits,
            //         parent.getAccountCredits().add(credits),
            //         user.getUserId(),
            //         user.getNickName(),
            //         msgDate.getAccount());
            userService.insertUserAmountRecords(parent.getUserId(),
                    TtAccountRecordType.INPUT,
                    TtAccountRecordSource.P_WELFARE,
                    credits,
                    parent.getAccountCredits().add(credits),
                    user.getUserId(),
                    user.getNickName(),
                    msgDate.getAccount());
        }
    }
}
