package com.ruoyi.playingmethod.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.other.ApiLuckyUpgradeBody;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.UpgradeBodyA;
import com.ruoyi.domain.vo.ApiLuckyOrnamentsDataVO;

import java.util.List;

public interface ApiLuckyUpgradeService {

    List<ApiLuckyOrnamentsDataVO> getOrnamentsList(ApiLuckyUpgradeBody apiLuckyUpgradeBody);

    R upgrade(TtUser ttUser, UpgradeBodyA upgradeBodyA);

    R upgrade2(TtUser ttUser, UpgradeBodyA upgradeParam);

    // List<ApiLuckyUpgradeRecordDataVO> getUpgradeRecord(getUpgradeRecord param);
}
