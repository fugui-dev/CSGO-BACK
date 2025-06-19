package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtContentType;
import com.ruoyi.admin.mapper.TtContentTypeMapper;
import com.ruoyi.admin.service.TtContentTypeService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TtContentTypeServiceImpl extends ServiceImpl<TtContentTypeMapper, TtContentType> implements TtContentTypeService {

    @Override
    public List<TtContentType> queryList(TtContentType ttContentType) {
        LambdaQueryWrapper<TtContentType> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotNull(ttContentType.getId())) wrapper.eq(TtContentType::getId, ttContentType.getId());
        if (StringUtils.isNotNull(ttContentType.getName())) wrapper.like(TtContentType::getName, ttContentType.getName());
        if (StringUtils.isNotEmpty(ttContentType.getAlias())) wrapper.like(TtContentType::getAlias, ttContentType.getAlias());
        if (StringUtils.isNotEmpty(ttContentType.getStatus())) wrapper.eq(TtContentType::getStatus, ttContentType.getStatus());
        wrapper.orderByDesc(TtContentType::getCreateTime);
        return this.list(wrapper);
    }
}
