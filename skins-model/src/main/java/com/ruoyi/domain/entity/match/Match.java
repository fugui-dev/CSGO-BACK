package com.ruoyi.domain.entity.match;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "match")
public class Match  {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 比赛名称
     */
    private String name;
    /**
     * 比赛描述
     */
    private String description;

    /**
     * 日期 yyyy-MM-dd
     */
    private String dayTime;

    /**
     * 比赛开始时间
     */
    private LocalDateTime startTime;
    /**
     * 比赛结束时间
     */
    private LocalDateTime endTime;

    /**
     * 报名费
     */
    private BigDecimal amount;

    /**
     * 比赛状态(0-未开始,1-进行中,2-已结束)
     */
    private Integer status;
    /**
     * 最大队伍数量(默认16)
     */
    private Integer maxTeamNum;
    /**
     * 每队人数(默认10)
     */
    private Integer teamSize;
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


    private LocalDateTime createTime; // 创建时间


    private LocalDateTime updateTime; // 更新时间
}
