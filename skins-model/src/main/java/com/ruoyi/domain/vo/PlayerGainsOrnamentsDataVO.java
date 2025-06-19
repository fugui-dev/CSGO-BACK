package com.ruoyi.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class PlayerGainsOrnamentsDataVO {

    List<UserPackSackDataVO> ornamentsDataList;
    private UserPackSackDataVO ornamentsData;
    private Integer userId;
    private String userName;
    private String nickName;
    private String avatar;
    private Integer joinSeatNum;


}
