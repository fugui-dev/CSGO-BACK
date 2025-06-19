package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.other.TtUpgradeOrnaments;
import com.ruoyi.domain.other.TtUpgradeOrnamentsBody;
import com.ruoyi.domain.vo.TtUpgradeOrnamentsDataVO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface TtUpgradeOrnamentsMapper extends BaseMapper<TtUpgradeOrnaments> {
    List<TtUpgradeOrnamentsDataVO> queryList(TtUpgradeOrnamentsBody ttUpgradeOrnamentsBody);

    Map<String, BigDecimal> getUpgradeProfitStatistics(Integer id);
}
