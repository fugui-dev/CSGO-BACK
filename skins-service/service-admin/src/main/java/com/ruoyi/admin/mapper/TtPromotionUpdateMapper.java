package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.entity.sys.TtPromotionUpdate;
import com.ruoyi.domain.vo.TeamDetailVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Mapper
public interface TtPromotionUpdateMapper extends BaseMapper<TtPromotionUpdate> {

    // @MapKey("employee_id")
    List<TeamDetailVO> latelyUpdate(@Param("allEmployeesId") List<Integer> allEmployeesId);
}
