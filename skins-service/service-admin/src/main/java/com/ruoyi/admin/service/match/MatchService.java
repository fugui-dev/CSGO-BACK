package com.ruoyi.admin.service.match;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.match.MatchCreateCmd;


public interface MatchService {

    /**
     * 创建比赛
     * @param matchCreateCmd 比赛创建命令
     */
    R<Integer> createMatch(MatchCreateCmd matchCreateCmd);
}
