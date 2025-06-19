package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.roll.TtRollUser;
import com.ruoyi.admin.mapper.TtRollUserMapper;
import com.ruoyi.admin.service.TtRollUserService;
import com.ruoyi.domain.vo.roll.RollUserPrizeVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TtRollUserServiceImpl extends ServiceImpl<TtRollUserMapper, TtRollUser> implements TtRollUserService {
    @Override
    public R rollWinners(Integer rollId) {

        List<RollUserPrizeVO> list = baseMapper.rollWinners(rollId);

        return R.ok(list);
    }
}
