package com.ruoyi.domain.dto;

import lombok.Data;

@Data
public class TeamDetailsListParam {

    private Integer parentId;

    private String beginTime;

    private String endTime;

    private Integer pageSize;

    private Integer pageNum;

}
