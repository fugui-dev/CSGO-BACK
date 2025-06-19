package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.dto.boxRecords.queryCondition;
import com.ruoyi.domain.dto.packSack.DecomposeLogCondition;
import com.ruoyi.domain.dto.roll.GetRollOpenPrizeParam;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.other.TtBoxRecordsBody;
import com.ruoyi.domain.vo.TtBoxRecordsDataVO;
import com.ruoyi.domain.vo.TtUserPackSackDataVO;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface TtBoxRecordsMapper extends BaseMapper<TtBoxRecords> {

    List<TtBoxRecordsDataVO> selectBoxRecordsList(TtBoxRecordsBody ttBoxRecordsBody);

    List<TtBoxRecords> selectBoxRecordsByDate(@Param("boxId") Integer boxId, @Param("formattedDate") String formattedDate);

    List<TtBoxRecordsDataVO> decomposeLog(DecomposeLogCondition param);

    List<Integer> myOwnFights(@Param("playerId") Integer playerId);

    List<TtBoxRecordsVO> byCondition(@Param("limit") Integer limit,
                                     @Param("size") Integer size,
                                     @Param("orderByFie") Integer orderByFie,
                                     @Param("boxRecordId") Long boxRecordId,
                                     @Param("boxId") Integer boxId,
                                     @Param("userId") Integer userId,
                                     @Param("userType") String userType,
                                     @Param("source") List<Integer> source,
                                     @Param("status") List<Integer> status,
                                     @Param("ornamentPriceMin") BigDecimal ornamentPriceMin,
                                     @Param("ornamentPriceMax") BigDecimal ornamentPriceMax,
                                     @Param("ornamentLevelIds") List<Integer> ornamentLevelIds
                                     );

    List<TtUserPackSackDataVO> propRankOfDay(@Param("beginT") String beginT,
                                             @Param("endT") String endT,
                                             @Param("sources") Integer[] sources,
                                             @Param("number") Integer number);

    List<TtBoxRecordsVO> rollOpenPrize(GetRollOpenPrizeParam param);

    Integer checkDeliveryAble(@Param("packSackIds") List<Long> packSackIds);

    Integer checkAllDeliveryAble(@Param("userId") Integer userId);

    /**
     * ̭�����״̬����
     */
    int updateStatusByIds(@Param("list") List<Long> list, @Param("status") String status);
}
