package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.entity.roll.TtRoll;
import com.ruoyi.domain.vo.TtRollPrizeDataVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TtRollMapper extends BaseMapper<TtRoll> {

    List<TtRollPrizeDataVO> getRollJackpotOrnamentsList(Integer rollId);

    List<TtRollPrizeDataVO> getSpecifiedRollJackpotOrnamentsList(Integer rollId);
}
