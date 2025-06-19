package com.ruoyi.playingmethod.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.upgrade.UpgradeCondition;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.*;
import com.ruoyi.playingmethod.mapper.ApiUpgradeRecordMapper;

import com.ruoyi.playingmethod.service.ApiUpgradeRecordService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ApiUpgradeRecordServiceImpl extends ServiceImpl<ApiUpgradeRecordMapper, TtUpgradeRecord> implements ApiUpgradeRecordService {


    @Autowired
    TtUserService userService;

    @Override
    public R historyDetail(UpgradeCondition param) {

        Page<TtUpgradeRecord> pageInfo = new Page<>(param.getPage(), param.getSize());
        pageInfo.setOptimizeCountSql(false);

        LambdaQueryWrapper<TtUpgradeRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(ObjectUtil.isNotNull(param.getUpgradeRecordId()),TtUpgradeRecord::getId,param.getUpgradeRecordId());

        wrapper.eq(ObjectUtil.isNotNull(param.getOrnamentId()),TtUpgradeRecord::getTargetOrnamentId,param.getOrnamentId());

        wrapper.eq(ObjectUtil.isNotNull(param.getUserType()),TtUpgradeRecord::getUserType,param.getUserType());

        wrapper.eq(ObjectUtil.isNotNull(param.getUserId()),TtUpgradeRecord::getUserId,param.getUserId());
        wrapper.orderByDesc(TtUpgradeRecord::getOpenTime);

        pageInfo = this.page(pageInfo, wrapper);

        if (pageInfo.getRecords().size() == 0){
            return R.ok(pageInfo);
        }

        Set<Integer> userIds = pageInfo.getRecords().stream().map(TtUpgradeRecord::getUserId).collect(Collectors.toSet());

        //封装用户头像
        List<TtUser> list = userService.list(Wrappers.lambdaQuery(TtUser.class)
                .in(TtUser::getUserId, userIds));
        //头像map
        Map<Integer, String> avatarMap = list.stream().collect(Collectors.toMap(TtUser::getUserId, TtUser::getAvatar));
        pageInfo.getRecords().forEach(r->{
            if (r.getUserId() !=null){
                r.setUserAvatar(avatarMap.get(r.getUserId()));
            }
        });


        return R.ok(pageInfo);

    }
}
