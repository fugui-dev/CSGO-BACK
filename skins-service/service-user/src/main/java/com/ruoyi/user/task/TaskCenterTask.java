package com.ruoyi.user.task;

import com.ruoyi.user.service.ApiTaskCenterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("TaskCenterTask")
public class TaskCenterTask {

    @Autowired
    private ApiTaskCenterService apiTaskCenterService;

    /**
     * 获取昨日充值量返弹药(已改金币)
     */
    public void getYesterdayExpenditureBonusPoints() {
        apiTaskCenterService.updateYesterdayBonusPoints();
    }


    /**
     * 自动计算充值量并发放返利金币
     */
    public void autoTopUpRebate() {
        apiTaskCenterService.autoTopUpRebate();
    }

    /**
     * 昨日总流水排名奖励自动发放
     */
    public void autoRankAward() {
        apiTaskCenterService.autoRankAward();
    }


}
