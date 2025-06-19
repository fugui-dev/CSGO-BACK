package com.ruoyi.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.domain.dto.upgrade.UpgradeCondition;
import com.ruoyi.domain.other.TtUpgradeRecord;
import com.ruoyi.admin.mapper.TtUpgradeRecordMapper;
import com.ruoyi.admin.service.TtUpgradeRecordService;
import com.ruoyi.domain.other.TtUpgradeRecordBody;
import com.ruoyi.domain.vo.upgrade.UpgradeRecordVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TtUpgradeRecordServiceImpl extends ServiceImpl<TtUpgradeRecordMapper, TtUpgradeRecord> implements TtUpgradeRecordService {

    @Override
    public List<UpgradeRecordVO> getUpgradeRecord(TtUpgradeRecordBody ttUpgradeRecordBody) {
        return baseMapper.getUpgradeRecord(ttUpgradeRecordBody);
    }

    @Override
    public R<Page<TtUpgradeRecord>> historyDetail(UpgradeCondition param) {

        Page<TtUpgradeRecord> pageInfo = new Page<>(param.getPage(), param.getSize());
        pageInfo.setOptimizeCountSql(false);

        LambdaQueryWrapper<TtUpgradeRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(ObjectUtil.isNotNull(param.getUpgradeRecordId()),TtUpgradeRecord::getId,param.getUpgradeRecordId());

        wrapper.eq(ObjectUtil.isNotNull(param.getOrnamentId()),TtUpgradeRecord::getTargetOrnamentId,param.getOrnamentId());

        wrapper.eq(ObjectUtil.isNotNull(param.getUserType()),TtUpgradeRecord::getUserType,param.getUserType());

        wrapper.eq(ObjectUtil.isNotNull(param.getUserId()),TtUpgradeRecord::getUserId,param.getUserId());
        wrapper.orderByDesc(TtUpgradeRecord::getOpenTime);

        pageInfo = this.page(pageInfo, wrapper);

        return R.ok(pageInfo);

    }

    @Override
    public R<Page<UpgradeRecordVO>> adminGetLog(UpgradeCondition param) {

        param.setLimit((param.getPage()-1) * param.getSize());

        List<UpgradeRecordVO> list = baseMapper.adminGetLog(param);

        Page<UpgradeRecordVO> pageInfo = new Page<>(param.getPage(), param.getSize());

        if (list.isEmpty()){
            pageInfo.setTotal(0);
            pageInfo.setRecords(list);
            return R.ok(pageInfo);
        }


        pageInfo.setTotal(list.get(0).getTotal());
        pageInfo.setRecords(list);

        return R.ok(pageInfo);

    }
}
