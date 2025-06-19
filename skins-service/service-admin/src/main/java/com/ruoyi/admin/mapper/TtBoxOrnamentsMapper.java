package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.other.TtBoxOrnaments;
import com.ruoyi.domain.vo.TtBoxOrnamentsDataVO;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TtBoxOrnamentsMapper extends BaseMapper<TtBoxOrnaments> {

    List<Integer> selectBoxIdList();

    List<TtBoxOrnamentsDataVO> selectTtBoxOrnamentsList(Integer boxId);

    List<TtBoxOrnamentsDataVO> ornametBelongbox(Integer integer);

    List<SimpleOrnamentVO> simpleBoxDetail(Integer boxId);

    List<Long> allOrnId(@Param("boxId") Integer boxId);
}
