package com.ruoyi.user.mapper;

import com.ruoyi.user.model.TtTaskCenterUser;
import com.ruoyi.user.model.dto.YesterdayExpenditureDTO;
import com.ruoyi.user.model.vo.ApiTaskCenterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ApiTaskCenterMapper {

    List<ApiTaskCenterVO> selectApiTaskCenterVOList(Long userId);

    List<YesterdayExpenditureDTO> getYesterdayExpenditure();

    String selectTaskTypeByTaskId(Integer taskId);

    BigDecimal selectCreditByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    TtTaskCenterUser selectTtTaskCenterUserByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    int insertYesterdayExpenditureBonusPoints(List<TtTaskCenterUser> ttTaskCenterUserList);

    int deleteYesterdayExpenditureBonusPoints();

    int markAsClaimedByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
}
