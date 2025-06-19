package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.domain.dto.queryCondition.OrnamentCondition;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.TtOrnamentsYY;
import com.ruoyi.domain.vo.WebsitePropertyDataVO;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TtOrnamentMapper extends BaseMapper<TtOrnament> {

    @Select("SELECT * FROM tt_ornament WHERE id = #{id}")
    TtOrnament selectOrnamentById(@Param("id") Long id);

    List<String> selectOrnamentsItemIdList();

    List<WebsitePropertyDataVO> list();

    List<SimpleOrnamentVO> simpleOrnamentInfo(@Param("idList") List<Long> idList);

    List<SimpleOrnamentVO> byCondition(OrnamentCondition condition);

    List<SimpleOrnamentVO> byCondition2(OrnamentCondition condition);

    Integer countByCondition(OrnamentCondition condition);

    List<Long> selectOrnamentsIdList();

    List<String> selectOrnamentsMarketHashNameList();

    int updateWebsiteProperty(WebsitePropertyDataVO websitePropertyDataVO);

    int deleteWebsitePropertyByIds(Integer[] ids);
}
