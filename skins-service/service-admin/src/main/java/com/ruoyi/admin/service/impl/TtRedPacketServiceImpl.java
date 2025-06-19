package com.ruoyi.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.other.TtRedPacket;
import com.ruoyi.admin.mapper.TtRedPacketMapper;
import com.ruoyi.admin.service.TtRedPacketService;
import com.ruoyi.domain.other.TtRedPacketBody;
import com.ruoyi.domain.vo.TtRedPacketDataVO;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TtRedPacketServiceImpl extends ServiceImpl<TtRedPacketMapper, TtRedPacket> implements TtRedPacketService {

    @Override
    public List<TtRedPacketDataVO> queryList(TtRedPacketBody ttRedPacketBody) {

        List<TtRedPacketDataVO> list = baseMapper.queryList(ttRedPacketBody);

        List<TtRedPacket> redPacketList = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(TtRedPacket::getStatus, 0)
                .list();

        List<Integer> expiredRedPack = new ArrayList<>();
        for (TtRedPacket ttRedPacket : redPacketList) {

            Date validity = ttRedPacket.getValidity();
            if (ObjectUtil.isNull(validity) || DateUtil.compare(validity, DateUtils.getNowDate()) > 0) continue;

            expiredRedPack.add(ttRedPacket.getId());
            ttRedPacket.setStatus(1);
        }

        if (!expiredRedPack.isEmpty()){
            new LambdaUpdateChainWrapper<>(baseMapper)
                    .in(TtRedPacket::getId,expiredRedPack)
                    .set(TtRedPacket::getStatus,1)
                    .update();
        }

        return list;
    }

    @Override
    public List<String> insertRedPacket(TtRedPacketDataVO ttRedPacketDataVO) {

        List<String> pwList = new ArrayList<>();
        List<TtRedPacket> rpList = new ArrayList<>();

        for (int i = 0; i < ttRedPacketDataVO.getCreateNum(); i++) {

            TtRedPacket redPacket = new TtRedPacket();

            BeanUtil.copyProperties(ttRedPacketDataVO,redPacket);

            redPacket.setPassword(IdUtils.fastSimpleUUID().toUpperCase());

            pwList.add(redPacket.getPassword());
            redPacket.setCreateBy(SecurityUtils.getUsername());
            redPacket.setCreateTime(DateUtils.getNowDate());
            redPacket.setUseStatus(0);

            rpList.add(redPacket);

        }

        this.saveBatch(rpList,1);

        return pwList;
    }

    @Override
    public AjaxResult updateRedPacketById(TtRedPacket redPacket) {
        if (this.updateById(redPacket)){
            return AjaxResult.success();
        }
        return AjaxResult.error();
    }
}
