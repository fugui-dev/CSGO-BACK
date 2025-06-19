package com.ruoyi.playingmethod.service;

import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.vo.UserPackSackDataVO;

import java.util.List;

public interface ApiCompoundService {

    R<UserPackSackDataVO> compound(List<Long> packSackIds, TtUser ttUser);

    List<UserPackSackDataVO> getUserCompoundRecord(Integer userId);
}
