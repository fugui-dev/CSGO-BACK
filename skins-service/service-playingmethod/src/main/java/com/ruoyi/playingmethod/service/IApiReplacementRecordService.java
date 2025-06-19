package com.ruoyi.playingmethod.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.user.model.dto.SmeltRequest;
import com.ruoyi.user.model.vo.SmeltVO;

import java.util.List;

public interface IApiReplacementRecordService {

    AjaxResult synthesizeItems(LoginUser user, List<Long> itemIds);

    R<SmeltVO> smelt(SmeltRequest smeltRequest);

}
