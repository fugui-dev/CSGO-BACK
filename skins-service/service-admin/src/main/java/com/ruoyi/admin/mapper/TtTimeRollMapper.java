package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.entity.roll.TtTimeRoll;
import com.ruoyi.domain.vo.TtRollPrizeDataVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TtTimeRollMapper extends BaseMapper<TtTimeRoll> {
    List<TtRollPrizeDataVO> getRollJackpotOrnamentsList(Integer id);
    List<TtRollPrizeDataVO> getSpecifiedTimeRollJackpotOrnamentsList(Integer id);
}
