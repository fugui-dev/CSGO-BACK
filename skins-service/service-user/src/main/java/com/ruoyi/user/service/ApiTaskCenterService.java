package com.ruoyi.user.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.user.model.TtTaskCenterUser;
import com.ruoyi.user.model.vo.ApiTaskCenterVO;

import java.util.List;

public interface ApiTaskCenterService {

    List<ApiTaskCenterVO> selectApiTaskCenterVOList(Long userId);

    String selectTaskTypeByTaskId(Integer taskId);

    TtTaskCenterUser selectTtTaskCenterUserByUserIdAndType(Long userId, String type);

    List<TtTaskCenterUser> updateYesterdayBonusPoints();

    //充值自动返利，如果使用改方法。则updateYesterdayBonusPoints()与getReward()方法均无需再手动调用，自动发放到用户账户中
    void autoTopUpRebate();

    AjaxResult getReward(Long userId, String type);

    void autoRankAward();

}
