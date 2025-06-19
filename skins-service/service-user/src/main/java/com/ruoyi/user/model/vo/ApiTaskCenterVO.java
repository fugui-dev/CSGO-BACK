package com.ruoyi.user.model.vo;

import lombok.Data;

@Data
public class ApiTaskCenterVO {

    private Integer taskId;

    private String taskName;

    private String description;

    /**
     * 领取条件（0不满足 1满足）
     */
    private String status;

    /**
     * 是否已领取（1已领取）
     */
    private String claimed;
}
