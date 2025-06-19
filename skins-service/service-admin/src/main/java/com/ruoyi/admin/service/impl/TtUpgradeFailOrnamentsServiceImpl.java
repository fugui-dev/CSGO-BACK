package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.controller.TtUpgradeFailOrnamentsController;
import com.ruoyi.admin.controller.TtUpgradeFailOrnamentsController.BatchAddParam;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.admin.service.TtUpgradeOrnamentsService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.other.TtUpgradeFailOrnaments;
import com.ruoyi.admin.mapper.TtUpgradeFailOrnamentsMapper;
import com.ruoyi.admin.service.TtUpgradeFailOrnamentsService;
import com.ruoyi.domain.other.TtUpgradeOrnaments;
import com.ruoyi.domain.vo.TtUpgradeFailOrnamentsDataVO;
import com.ruoyi.common.utils.bean.BeanUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TtUpgradeFailOrnamentsServiceImpl extends ServiceImpl<TtUpgradeFailOrnamentsMapper, TtUpgradeFailOrnaments> implements TtUpgradeFailOrnamentsService {

    @Autowired
    private TtOrnamentMapper ttOrnamentMapper;

    @Autowired
    private TtUpgradeOrnamentsService upgradeOrnamentsService;


    @Override
    public List<TtUpgradeFailOrnamentsDataVO> queryList(TtUpgradeFailOrnamentsController.listParam param) {
        return baseMapper.queryList(param);
    }

    @Override
    public AjaxResult batchAdd(BatchAddParam param) {
        Map<Long,Long> tempList = param.getOrnamentsIds().stream().collect(Collectors.toMap(Function.identity(), Function.identity()));

        List<TtOrnament> list = new LambdaQueryChainWrapper<>(ttOrnamentMapper)
                .in(TtOrnament::getId, tempList.keySet().toArray())
                .list();


        //应用到所有
        if (param.getUseAllFlag()){
            //截断表
            baseMapper.truncateTable();

            //查询所有幸运饰品
            List<TtUpgradeOrnaments> upgradeOrnamentList = upgradeOrnamentsService.list(Wrappers.lambdaQuery(TtUpgradeOrnaments.class)
                    .eq(TtUpgradeOrnaments::getStatus, 0));
            //每一个都要去添加
            for (TtUpgradeOrnaments upgradeOrnament : upgradeOrnamentList) {
                upgradeOneBatch(upgradeOrnament.getId(), list);
            }
            return AjaxResult.success();

        }

        //单个应用
        return upgradeOneBatch(param.getUpgradeId(), list);
    }

    private AjaxResult upgradeOneBatch(Long upgradeId, List<TtOrnament> list) {
        List<TtUpgradeFailOrnaments> collect = list.stream().map(ornament -> {

            TtUpgradeFailOrnaments build = TtUpgradeFailOrnaments.builder()
                    .upgradeId(upgradeId)
                    .ornamentId(ornament.getId())
                    .ornamentName(ornament.getName())
                    .ornamentPrice(ornament.getUsePrice())
                    .createTime(new Date())
                    .updateTime(new Date())
                    .ornamentNumber(1)
                    .build();
            return build;
        }).collect(Collectors.toList());

        this.saveBatch(collect);

        return AjaxResult.success(collect);
    }

    @Override
    public String updateUpgradeFailOrnamentsById(TtUpgradeFailOrnamentsDataVO ttUpgradeFailOrnamentsDataVO) {
        TtUpgradeFailOrnaments ttUpgradeFailOrnaments = TtUpgradeFailOrnaments.builder().build();
        BeanUtils.copyProperties(ttUpgradeFailOrnamentsDataVO, ttUpgradeFailOrnaments);
        this.updateById(ttUpgradeFailOrnaments);
        return "";
    }
}
