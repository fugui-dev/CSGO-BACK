package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.roll.GetRollPlayersParam;
import com.ruoyi.domain.entity.roll.TtRollUser;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import com.ruoyi.domain.vo.roll.RollUserPrizeVO;
import com.ruoyi.domain.vo.roll.RollUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TtRollUserMapper extends BaseMapper<TtRollUser> {
    R<List<TtBoxRecordsVO>> byRollId(Integer rollId);

    List<RollUserVO> pageByRollId(GetRollPlayersParam param);

    List<RollUserPrizeVO> rollWinners(@Param("rollId") Integer rollId);
}
