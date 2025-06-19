package com.ruoyi.domain.other;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtUserPackSackBody {

    private Integer holderUserId;
    private String phoneNumber;
    private String status;
    private String source;
    private Integer fightId;
    private Integer rollId;
}
