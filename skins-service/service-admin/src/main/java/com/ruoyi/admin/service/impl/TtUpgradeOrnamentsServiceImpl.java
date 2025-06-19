package com.ruoyi.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.other.TtUpgradeOrnaments;
import com.ruoyi.admin.mapper.TtUpgradeOrnamentsMapper;
import com.ruoyi.admin.service.TtUpgradeOrnamentsService;
import com.ruoyi.admin.util.RandomUtils;
import com.ruoyi.domain.other.TtUpgradeOrnamentsBody;
import com.ruoyi.domain.vo.TtUpgradeOrnamentsDataVO;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TtUpgradeOrnamentsServiceImpl extends ServiceImpl<TtUpgradeOrnamentsMapper, TtUpgradeOrnaments> implements TtUpgradeOrnamentsService {

    @Value("${mkcsgo.upgrade.defaultRequired}")
    private Integer defaultRequired;

    @Value("${mkcsgo.upgrade.anchorDefaultRequired}")
    private Integer anchorDefaultRequired;

    private final TtOrnamentMapper ornamentsMapper;

    public TtUpgradeOrnamentsServiceImpl(TtOrnamentMapper ornamentsMapper) {
        this.ornamentsMapper = ornamentsMapper;
    }

    @Override
    public List<TtUpgradeOrnamentsDataVO> queryList(TtUpgradeOrnamentsBody ttUpgradeOrnamentsBody) {
        return baseMapper.queryList(ttUpgradeOrnamentsBody);
    }

    @Override
    public String batchAdd(List<Long> ornamentsIds) {

        List<Long> idList = new ArrayList<>(ornamentsIds);

        // 默认区间
        int[] luckSection = {0, defaultRequired};
        int[] anchorLuckSection = {0, anchorDefaultRequired};
        String luckSectionStr = JSON.toJSONString(luckSection);
        String anchorLuckSectionStr = JSON.toJSONString(anchorLuckSection);

        // 饰品列表
        List<TtOrnament> list = new LambdaQueryChainWrapper<>(ornamentsMapper)
                .in(TtOrnament::getId, idList)
                .eq(TtOrnament::getIsPutaway, 0)
                .list();
        // List<TtOrnament> list = ornamentsMapper.selectBatchIds(tempList);
        // Map<Long, TtOrnament> ornamentsMap = list.stream().collect(Collectors.toMap(TtOrnament::getId, ttOrnaments -> ttOrnaments));

        for (TtOrnament ornament : list) {

            TtUpgradeOrnaments ttUpgradeOrnaments = TtUpgradeOrnaments.builder()
                    .ornamentsId(ornament.getId())
                    .ornamentPrice(ObjectUtil.isNotNull(ornament.getUsePrice()) ? ornament.getUsePrice() : ornament.getPrice())

                    .luckSection(luckSectionStr)
                    .anchorLuckSection(anchorLuckSectionStr)

                    .amountRequired(ornament.getUsePrice()
                            .multiply(new BigDecimal(luckSection[1]))
                            .multiply(new BigDecimal("0.01")))
                    .amountInvested(BigDecimal.ZERO)

                    .anchorAmountRequired(ornament.getUsePrice()
                            .multiply(new BigDecimal(anchorLuckSection[1]))
                            .multiply(new BigDecimal("0.01")))
                    .anchorAmountInvested(BigDecimal.ZERO)
                    .createTime(new Date())
                    .updateTime(new Date())
                    .status("0")
                    .build();

            // BigDecimal amountRequired = RandomUtils.getRandomPrice(ttUpgradeOrnaments.getLuckSection())
            //         .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            //         .multiply(ornamentsMap.get(OrnamentsId).getUsePrice())
            //         .setScale(2, RoundingMode.HALF_UP);
            //
            // ttUpgradeOrnaments.setAmountRequired(amountRequired);
            // ttUpgradeOrnaments.setAnchorLuckSection(luckSectionJSONStr);
            // BigDecimal anchorAmountRequired = RandomUtils.getRandomPrice(ttUpgradeOrnaments.getAnchorLuckSection())
            //         .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP).multiply(ornamentsMap.get(OrnamentsId).getUsePrice())
            //         .setScale(2, RoundingMode.HALF_UP);
            // ttUpgradeOrnaments.setAnchorAmountRequired(anchorAmountRequired);
            // ttUpgradeOrnaments.setCreateTime(DateUtils.getNowDate());
            this.save(ttUpgradeOrnaments);
        }
        return "";
    }

    @Override
    public String updateUpgradeOrnamentsById(TtUpgradeOrnamentsDataVO ttUpgradeOrnamentsDataVO) {
        TtUpgradeOrnaments ttUpgradeOrnaments = this.getById(ttUpgradeOrnamentsDataVO.getId());
        BeanUtils.copyBeanProp(ttUpgradeOrnaments, ttUpgradeOrnamentsDataVO);
        this.updateById(ttUpgradeOrnaments);
        return "";
    }

    @Override
    public Map<String, BigDecimal> getUpgradeProfitStatistics(Integer id) {
        return baseMapper.getUpgradeProfitStatistics(id);
    }
}
