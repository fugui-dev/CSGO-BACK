package com.ruoyi.domain.other;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class TtRedPacketRecordBody {

    private Integer redPacketId;
    private Integer userId;
    private String receivePassword;
}
