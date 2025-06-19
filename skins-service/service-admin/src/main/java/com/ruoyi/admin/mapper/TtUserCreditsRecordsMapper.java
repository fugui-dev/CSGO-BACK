package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.entity.recorde.TtUserCreditsRecords;
import com.ruoyi.domain.other.TtUserCreditsRecordsBody;
import com.ruoyi.domain.vo.TtUserCreditsRecordsRankVO;
import com.ruoyi.domain.vo.task.SimpleCreditsRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TtUserCreditsRecordsMapper extends BaseMapper<TtUserCreditsRecords> {
    List<TtUserCreditsRecords> queryList(TtUserCreditsRecordsBody ttUserCreditsRecordsBody);

    List<TtUserCreditsRecordsRankVO> rank(@Param("page") Integer page,
                                          @Param("size") Integer size,
                                          @Param("begin") String begin,
                                          @Param("end") String end);

    List<SimpleCreditsRecordVO> recordsByTimeAndHasBoss(@Param("beginT") String beginT,@Param("endT") String endT);
}
