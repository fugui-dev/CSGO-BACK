package com.ruoyi.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SimpleUserVO {

    private Integer userId;
    private Integer rollUserId;
    private String nickName;
    private String userName;
    private Integer userType;
    private Integer phoneNumber;
    private Integer parentId;
    private String avatar;
}
