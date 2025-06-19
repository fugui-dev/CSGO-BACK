package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.dto.roll.GetRollPrizePool;
import com.ruoyi.domain.entity.roll.TtRollJackpotOrnaments;
import com.ruoyi.domain.entity.roll.TtRollJackpotOrnamentsBody;
import com.ruoyi.domain.vo.TtRollJackpotOrnamentsDataVO;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import com.ruoyi.domain.vo.roll.RollJackpotOrnamentsVO;
import com.ruoyi.domain.vo.roll.SimpleRollOrnamentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TtRollJackpotOrnamentsMapper extends BaseMapper<TtRollJackpotOrnaments> {
    List<TtRollJackpotOrnamentsDataVO> queryList(TtRollJackpotOrnamentsBody rollJackpotOrnamentsBody);

    List<SimpleRollOrnamentVO> rollShow(@Param("jackpotId") Integer jackpotId);

    List<RollJackpotOrnamentsVO> listByRollId(GetRollPrizePool param);

    Integer ornamentsNumberOfRoll(@Param("jackpotId") Integer jackpotId);

    List<TtBoxRecordsVO> byOrnamentIds(@Param("rollId") Integer rollId,
                                       @Param("noHOrnamentIds") List<Integer> noHOrnamentIds);

    Integer totalByCondition(GetRollPrizePool param);
}
