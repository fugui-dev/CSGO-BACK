package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.dto.promo.UserPlayInfoDTO;
import com.ruoyi.domain.dto.userRecord.AmountRecordsDetailCondition;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.vo.TeamDetailSimpleVO;
import com.ruoyi.domain.vo.TtUserAmountRecords.PersonBlendErcashVO;
import com.ruoyi.domain.vo.TtUserAmountRecords.TtUserBlendErcashVO;
import com.ruoyi.domain.vo.TtUserAmountRecords.UserAmountDetailVO;
import com.ruoyi.domain.vo.UserBERankVO;
import com.ruoyi.domain.vo.sys.SimpleTtUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface TtUserBlendErcashMapper extends BaseMapper<TtUserBlendErcash> {

    List<UserBERankVO> rank(@Param("sourceList") List<Integer> sourceList,
                            @Param("beginT") String beginT,
                            @Param("endT") String endT,
                            @Param("limit") Integer limit,
                            @Param("size") Integer size);

    List<SimpleTtUserVO> batchConsumeTotal(@Param("userIdList") List<Integer> userIdList,
                                           @Param("beginTime") String beginTime,
                                           @Param("endTime") String endTime,
                                           @Param("orderByFie") Integer orderByFie,
                                           @Param("orderByType") Integer orderByType,
                                           @Param("limit") Integer limit,
                                           @Param("size") Integer size);

    /**
     * 统计用户在一个时间段内的消费
     */
    PersonBlendErcashVO personTotalConsumeByTime(
            @Param("userId") Integer userId,
            @Param("beginT") String beginT,
            @Param("endT") String endT,
            @Param("type") Integer type
    );

    /**
     * 批量统计用户在一个时间段内的消费
     */
    List<PersonBlendErcashVO> personsTotalConsumeByTime(
            @Param("userIds") List<Integer> userIds,
            @Param("beginT") String beginT,
            @Param("endT") String endT,
            @Param("type") Integer type

    );

    List<UserAmountDetailVO> userAccountDetail(AmountRecordsDetailCondition param);

    List<TtUserBlendErcashVO> byCondition(
            @Param("userId") Integer userId,
            @Param("source") Integer source,
            @Param("type") Integer type,
            @Param("userName") String userName,
            @Param("moneyType") Integer moneyType,
            @Param("limit") Integer limit,
            @Param("size") Integer size);

    Integer count(
            @Param("userId") Integer userId,
            @Param("source") Integer source,
            @Param("type") Integer type,
            @Param("userName") String userName,
            @Param("moneyType") Integer moneyType,
            @Param("limit") Integer limit,
            @Param("size") Integer size
    );

    /**
     * 计算时间范围内所有用户下级的流水信息
     * 字符串格式 yyyy-MM-dd HH:mm:ss
     */
    List<UserPlayInfoDTO> calcPlayTotalByTimeScope(@Param("beginTime") String beginTime,@Param("endTime") String endTime);

    UserPlayInfoDTO calcPlayTotalByTimeScopeAndUser(@Param("beginTime") String beginTime,@Param("endTime") String endTime, @Param("userId") Integer userId);


    List<TeamDetailSimpleVO> teamDetailsList(@Param("parentId")Long parentId, @Param("beginTime")String beginTime, @Param("endTime")String endTime, @Param("pageSize")Integer pageSize, @Param("pageNum")Integer pageNum);

    /**
     * 获取名下主播数量
     */
    int getAnchorCount(Long userId);

    /**
     * 获取主播下级总充值
     * @param userId
     * @return
     */
    BigDecimal getTotalCharge(Integer userId);

    BigDecimal getTotalConsume(Integer userId);

    BigDecimal getTodayConsume(Integer userId);
}
