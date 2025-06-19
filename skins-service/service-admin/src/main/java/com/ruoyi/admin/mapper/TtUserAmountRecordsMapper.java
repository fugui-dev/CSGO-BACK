package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.vo.TtUserAccountRecordsRankVO;
import com.ruoyi.domain.entity.recorde.TtUserAmountRecords;
import com.ruoyi.domain.other.TtUserAmountRecordsBody;
import com.ruoyi.domain.vo.UserBERankVO;
import com.ruoyi.domain.vo.task.SimpleAmountRecordVO;
import com.ruoyi.domain.vo.task.SimpleBlendErcashRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface TtUserAmountRecordsMapper extends BaseMapper<TtUserAmountRecords> {

    List<TtUserAmountRecords> queryList(TtUserAmountRecordsBody ttUserAmountRecordsBody);

    List<TtUserAccountRecordsRankVO> rank(@Param("beginT") String beginT,
                                          @Param("endT") String endT,
                                          @Param("page") Integer page,
                                          @Param("size") Integer size);

    BigDecimal pWHistoryTotal(Integer uid);

    List<UserBERankVO> blendErcashRank(@Param("sourceList") List<Integer> sourceList,
                                       @Param("beginT") String beginT,
                                       @Param("endT") String endT,
                                       @Param("limit") Integer limit,
                                       @Param("size") Integer size);

    List<SimpleAmountRecordVO> recordsByTimeAndHasBoss(@Param("beginT") String beginT,
                                                       @Param("endT") String endT);

    Integer totalSize(TtUserAmountRecordsBody param);

    List<SimpleBlendErcashRecordVO> BlendErcashByTimeAndHasBoss(@Param("beginT") String beginT,
                                                                @Param("endT") String endT);

    BigDecimal pWelfareByTime(@Param("bossId") Integer bossId,
                              @Param("beginT") String beginT,
                              @Param("endT") String endT);

    List<Integer> bossByHasConsumeEmployee(@Param("beginT") String beginT,@Param("endT") String endT);

    /**
     * 获取用户的总消费
     */
    BigDecimal getTotalConsumptionByUserId(Integer userId);
}
