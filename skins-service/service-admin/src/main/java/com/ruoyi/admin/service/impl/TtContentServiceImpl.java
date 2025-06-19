package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtContent;
import com.ruoyi.admin.mapper.TtContentMapper;
import com.ruoyi.admin.service.TtContentService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TtContentServiceImpl extends ServiceImpl<TtContentMapper, TtContent> implements TtContentService {

    @Override
    public List<TtContent> queryList(TtContent ttContent) {
        LambdaQueryWrapper<TtContent> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotNull(ttContent.getId())) wrapper.eq(TtContent::getId, ttContent.getId());
        if (StringUtils.isNotNull(ttContent.getTypeId())) wrapper.eq(TtContent::getTypeId, ttContent.getTypeId());
        if (StringUtils.isNotEmpty(ttContent.getTitle())) wrapper.like(TtContent::getTitle, ttContent.getTitle());
        if (StringUtils.isNotEmpty(ttContent.getContent())) wrapper.like(TtContent::getContent, ttContent.getContent());
        if (StringUtils.isNotEmpty(ttContent.getStatus())) wrapper.eq(TtContent::getStatus, ttContent.getStatus());
        wrapper.orderByDesc(TtContent::getCreateTime);
        return this.list(wrapper);
    }
}
