package com.ruoyi.admin.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.admin.mapper.TtRollJackpotMapper;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.domain.dto.rollJackpotOrnament.RollJOEdit;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.roll.TtRollJackpot;
import com.ruoyi.domain.entity.roll.TtRollJackpotOrnaments;
import com.ruoyi.admin.mapper.TtRollJackpotOrnamentsMapper;
import com.ruoyi.admin.service.TtRollJackpotOrnamentsService;
import com.ruoyi.domain.entity.roll.TtRollJackpotOrnamentsBody;
import com.ruoyi.domain.vo.TtRollJackpotOrnamentsDataVO;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TtRollJackpotOrnamentsServiceImpl extends ServiceImpl<TtRollJackpotOrnamentsMapper, TtRollJackpotOrnaments> implements TtRollJackpotOrnamentsService {


    @Autowired
    private TtOrnamentMapper ttOrnamentMapper;

    @Autowired
    private TtRollJackpotMapper ttRollJackpotMapper;

    @Override
    public List<TtRollJackpotOrnamentsDataVO> queryList(TtRollJackpotOrnamentsBody rollJackpotOrnamentsBody) {
        return baseMapper.queryList(rollJackpotOrnamentsBody);
    }

    @Override
    public String insertRollJackpotOrnaments(TtRollJackpotOrnaments ttRollJackpotOrnaments) {
        TtOrnament ornament = ttOrnamentMapper.selectOrnamentById(ttRollJackpotOrnaments.getOrnamentsId());
        if (ornament == null){
            throw new ServiceException("未找到该饰品信息！");
        }
        ttRollJackpotOrnaments.setOrnamentName(ornament.getName());
        ttRollJackpotOrnaments.setImgUrl(ornament.getImageUrl());
        ttRollJackpotOrnaments.setCreateTime(DateUtils.getNowDate());
        this.save(ttRollJackpotOrnaments);
        return "";
    }

    @Override
    public String updateRollJackpotOrnamentsById(TtRollJackpotOrnaments rollJOEdit) {

        rollJOEdit.setUpdateTime(DateUtils.getNowDate());
        this.updateById(rollJOEdit);

        // 重新计算奖池总价
        List<TtRollJackpotOrnaments> list = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(TtRollJackpotOrnaments::getJackpotId, rollJOEdit.getJackpotId())
                .list();
        BigDecimal total = BigDecimal.ZERO;
        for (TtRollJackpotOrnaments item : list){
            total = total.add(item.getPrice().multiply(new BigDecimal(item.getOrnamentsNum())));
        }
        new LambdaUpdateChainWrapper<>(ttRollJackpotMapper)
                .eq(TtRollJackpot::getJackpotId,rollJOEdit.getJackpotId())
                .set(TtRollJackpot::getTotalPrice,total)
                .set(TtRollJackpot::getUpdateTime,new Date())
                .update();
        return "";
    }

    @Override
    public String batchAdd(Integer rollJackpotId, List<Long> OrnamentsIds) {

        List<Long> ornIds = new ArrayList<>(OrnamentsIds);

        List<TtOrnament> ornamentList = new LambdaQueryChainWrapper<>(ttOrnamentMapper).in(TtOrnament::getId, ornIds).list();

        for (TtOrnament ornament : ornamentList) {
            TtRollJackpotOrnaments ttRollJackpotOrnaments = TtRollJackpotOrnaments.builder().build();
            ttRollJackpotOrnaments.setJackpotId(rollJackpotId);
            ttRollJackpotOrnaments.setOrnamentsId(ornament.getId());
            ttRollJackpotOrnaments.setOrnamentsNum(1);
            ttRollJackpotOrnaments.setPrice(ObjectUtil.isNotEmpty(ornament.getUsePrice())?ornament.getUsePrice():ornament.getPrice());
            ttRollJackpotOrnaments.setImgUrl(ornament.getImageUrl());
            ttRollJackpotOrnaments.setOrnamentName(ornament.getName());
            ttRollJackpotOrnaments.setCreateTime(DateUtils.getNowDate());
            this.save(ttRollJackpotOrnaments);
        }
        return "";
    }
}
