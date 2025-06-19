package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.roll.TtRollUser;

public interface TtRollUserService extends IService<TtRollUser> {
    R rollWinners(Integer rollId);
}
