package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RollListDataVO {

    /**
     * ROLL房ID
     */
    private Integer id;

    /**
     * ROLL房密码
     */
    private String rollPassword;

    /**
     * 是否有密码
     */
    private Boolean hasPW;

    /**
     * ROLL房类型
     */
    private String rollType;

    /**
     * ROLL房类型名称
     */
    private String rollTypeName;

    private String nickName;

    private String avatar;

    private String rollName;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Integer currentPeopleNum;

    private Integer peopleNum;

    /**
     * ROLL房状态
     */
    private String rollStatus;

    private String ornamentsList;

    /**
     * 奖池物品实例数量
     */
    private Integer ornamentsNum;

    private BigDecimal totalOrnamentsPrice;

    /**
     * 门槛
     */
    private BigDecimal minRecharge;
}
