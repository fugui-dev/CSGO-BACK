package com.ruoyi.thirdparty.wechat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TtCoinFirstChargeBonusRecordParam {
    private Long id;
    private Long uid;
    private Integer status;
}
