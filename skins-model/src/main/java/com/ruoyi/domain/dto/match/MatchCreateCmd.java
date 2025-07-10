package com.ruoyi.domain.dto.match;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatchCreateCmd {

    /**
     * 比赛名称
     */
    private String name;
    /**
     * 比赛描述
     */
    private String description;
    /**
     * 比赛开始时间
     */
    private LocalDateTime startTime;
    /**
     * 报名开始时间
     */
    private LocalDateTime signUpStartTime;

    /**
     * 报名截止时间
     */
    private LocalDateTime signUpEndTime;

    /**
     * 开放时间 如果在开发之间之前 没有报满人员 则全服开放
     */
    private LocalDateTime openTime; // 开放时间

}
