package com.ruoyi.promo.mapper;

import com.ruoyi.domain.entity.TtCommissionRecord;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.promo.domain.dto.TotalUserExpenditureDTO;
import com.ruoyi.promo.domain.vo.AnchorDayTurnoverVO;
import com.ruoyi.promo.domain.vo.DayInviteVO;
import com.ruoyi.promo.domain.vo.DayTurnoverVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推广流水Mapper
 */
@Mapper
public interface PromoTurnoverMapper {

    /**
     * 获取名下主播数量
     */
    int getAnchorCount(Long userId);

    /**
     * 获取名下主播总流水
     */
    BigDecimal getTotalTurnover(Long userId);

    /**
     * 获取名下主播上月流水
     */
    BigDecimal getLastMonthTurnover(Long userId);

    /**
     * 获取名下主播本月流水
     */
    BigDecimal getCurrentMonthTurnover(Long userId);

    /**
     * 获取名下主播上周流水
     */
    BigDecimal getLastWeekTurnover(Long userId);

    /**
     * 获取名下主播本周流水
     */
    BigDecimal getCurrentWeekTurnover(Long userId);

    /**
     * 获取名下主播昨日流水
     */
    BigDecimal getLastDayTurnover(Long userId);

    /**
     * 获取名下主播今日流水
     */
    BigDecimal getCurrentDayTurnover(Long userId);

    /**
     * 获取名下主播近10天日流水
     */
    List<DayTurnoverVO> getLast10DaysTurnover(Long userId);

    /**
     * 获取名下主播近10天日邀请
     */
    List<DayInviteVO> getLast10DaysInvite(Long userId);

    /**
     * 查询名下主播每日流水
     */
    List<AnchorDayTurnoverVO> getAnchorDayTurnover(AnchorDayTurnoverVO anchorDayTurnoverVO);

    @Select("SELECT * FROM tt_user WHERE parent_id = #{parentId}")
    List<TtUser> findByParentId(@Param("parentId") int parentId);

    @Select("SELECT * FROM tt_user WHERE user_id = #{userId}")
    TtUser findById(@Param("userId") int id);

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
    int updateCommissionRate(@Param("userId") Integer userId, @Param("commissionRate") BigDecimal commissionRate);

    /**
     * 获取佣金列表
     */
    List<TtCommissionRecord> getCommissionList(Integer userId);

    /**
     * 获取上个月用户消费额
     */
    List<TotalUserExpenditureDTO> getLastMonthTotalUserExpenditure();
}
