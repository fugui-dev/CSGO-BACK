package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.TtUserBlendErcashMapper;
import com.ruoyi.admin.service.TtUserBlendErcashService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.dto.userRecord.BlendErcashCondition;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.vo.TtUserAmountRecords.TtUserBlendErcashVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TtUserBlendErcashServiceImpl extends ServiceImpl<TtUserBlendErcashMapper, TtUserBlendErcash> implements TtUserBlendErcashService {

    @Override
    public R byCondition(BlendErcashCondition condition) {

        condition.setLimit((condition.getPage() - 1) * condition.getSize());
        if (StringUtils.isBlank(condition.getUserName())) condition.setUserName(null);

        Page<TtUserBlendErcashVO> pageInfo = new Page<>(condition.getPage(), condition.getSize());
        List<TtUserBlendErcashVO> list = baseMapper.byCondition(
                condition.getUserId(),
                condition.getSource(),
                condition.getType(),
                condition.getUserName(),
                condition.getMoneyType(),
                condition.getLimit(),
                condition.getSize()
        );

        Integer total = baseMapper.count(
                condition.getUserId(),
                condition.getSource(),
                condition.getType(),
                condition.getUserName(),
                condition.getMoneyType(),
                condition.getLimit(),
                condition.getSize());
        pageInfo.setTotal(total);
        pageInfo.setRecords(list);

        return R.ok(pageInfo);
    }
}
