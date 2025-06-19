package com.ruoyi.domain.vo;

import lombok.Data;

@Data
public class JoinFightUserDataVO {

    private Integer fightId;
    private Integer userId;
    private Integer joinSeatNum;
    private String userName;
    private String nickName;
    private String avatar;

}
