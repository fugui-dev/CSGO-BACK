package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtUpgradeOrnaments;
import com.ruoyi.domain.other.TtUpgradeOrnamentsBody;
import com.ruoyi.domain.vo.TtUpgradeOrnamentsDataVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TtUpgradeOrnamentsService extends IService<TtUpgradeOrnaments> {

    List<TtUpgradeOrnamentsDataVO> queryList(TtUpgradeOrnamentsBody ttUpgradeOrnamentsBody);

    String batchAdd(List<Long> ornamentsIds);

    String updateUpgradeOrnamentsById(TtUpgradeOrnamentsDataVO ttUpgradeOrnamentsDataVO);

    Map<String, BigDecimal> getUpgradeProfitStatistics(Integer id);
}
