package com.ruoyi.user.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.packSack.DecomposeLogCondition;
import com.ruoyi.domain.dto.packSack.DecomposeParam;
import com.ruoyi.domain.dto.packSack.DeliveryParam;
import com.ruoyi.domain.dto.packSack.PackSackCondition;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.vo.TtBoxRecordsDataVO;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.vo.UserPackSackDataVO;
import com.ruoyi.domain.vo.client.PackSackGlobalData;
import com.ruoyi.user.model.dto.SmeltRequest;
import com.ruoyi.user.model.vo.SmeltVO;

import java.util.List;

public interface ApiUserPackSackService {
    R delivery(DeliveryParam param, TtUser ttUser);

    int decompose(DecomposeParam param, TtUser ttUser);

    List<UserPackSackDataVO> getPackSack(Integer userId);

    List<TtBoxRecords> packSackHandle(List<Long> packSackIds, TtUser ttUser, Integer status);

    List<TtBoxRecordsDataVO>  decomposeLog(DecomposeLogCondition param);

    R clientPackSack(PackSackCondition condition);

    R<PackSackGlobalData> packSackGlobalData(Integer userId);

    /**
     * 熔炼背包饰品
     * @param smeltRequest
     * @return
     */
    R<SmeltVO> smelt(SmeltRequest smeltRequest);

}
