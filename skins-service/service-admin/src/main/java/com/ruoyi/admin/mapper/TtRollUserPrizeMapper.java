package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.entity.roll.TtRollUser;
import com.ruoyi.domain.entity.roll.TtRollUserPrize;
import com.ruoyi.domain.vo.roll.RollUserPrizeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TtRollUserPrizeMapper extends BaseMapper<TtRollUserPrize> {
    List<RollUserPrizeVO> byRollUserIds(@Param("rollUserIds") List<Integer> rollUserIds);

    Integer ownOrnamentNumber(@Param("rollJackpotOrnamentId") Integer rollJackpotOrnamentId,@Param("rollId") Integer rollId);

    List<RollUserPrizeVO> byRollId(@Param("rollId") Integer rollId);
}
