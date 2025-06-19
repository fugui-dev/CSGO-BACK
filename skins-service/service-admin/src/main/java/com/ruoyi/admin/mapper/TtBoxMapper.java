package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.other.TtBoxBody;
import com.ruoyi.domain.vo.TtBoxDataVO;
import com.ruoyi.domain.vo.box.BoxGlobalData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TtBoxMapper extends BaseMapper<TtBox> {
    List<TtBoxDataVO> selectTtBoxList(TtBoxBody ttBoxBody);

    BoxGlobalData globalData(@Param("boxId") Integer boxId);
}
