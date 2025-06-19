package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.domain.dto.userRecord.BlendErcashCondition;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.other.TtBoxBody;
import com.ruoyi.domain.vo.BoxCacheDataVO;
import com.ruoyi.domain.vo.TtBoxDataVO;

import java.util.Date;
import java.util.List;

public interface TtUserBlendErcashService extends IService<TtUserBlendErcash> {

    R byCondition(BlendErcashCondition condition);
}
