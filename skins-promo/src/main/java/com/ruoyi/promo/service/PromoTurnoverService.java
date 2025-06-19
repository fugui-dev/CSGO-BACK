package com.ruoyi.promo.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.entity.TtCommissionRecord;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.promo.domain.vo.AnchorDayTurnoverVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PromoTurnoverService {

    /**
     * 获取实时数据
     */
    AjaxResult getRealTimeData(Long userId);

    /**
     * 获取近10天推广数据
     */
    AjaxResult getLast10DaysPromotionData(Long userId);

    /**
     * 获取名下主播每日流水
     */
    List<AnchorDayTurnoverVO> getAnchorDayTurnover(AnchorDayTurnoverVO anchorDayTurnoverVO);

    /**
     * 获取下级分支
     */
    List<TtUser> getSubBranches(Long userId);

    /**
     * 获取消费记录
     */
    List<TtUserBlendErcash> getPurchaseByUserId(Integer userId);

    /**
     * 获取佣金比例
     */
    BigDecimal getCommissionRateByUserId(Integer userId);

    /**
     * 修改佣金比例
     */
    int updateCommissionRate(Integer userId, BigDecimal commissionRate);

    /**
     * 获取佣金列表
     */
    List<TtCommissionRecord> getCommissionList(Integer userId);
}
