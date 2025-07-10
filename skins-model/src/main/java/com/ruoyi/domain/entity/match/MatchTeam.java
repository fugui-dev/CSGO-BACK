package com.ruoyi.domain.entity.match;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "match_team")
public class MatchTeam {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 比赛id
     */
    private Integer matchId;

    /**
     * 总排名
     */
    private Integer rank; // 排名

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 队伍状态状态(0-招募中,1-已组建,2-已解散)
     */
    private Integer status;

    /**
     * 当前成员数量
     */
    private Integer memberCount;

    /**
     * 最大成员数量
     */
    private Integer maxMemberCount;

    /**
     * 队长用户ID
     */
    private Integer captainUserId;

    /**
     * 队伍代码，用于唯一标识队伍，用于邀请、加入等操作
     */
    private String teamCode;

    /**
     * 队伍总分
     */
    private BigDecimal totalScore;

    /**
     * 队伍总胜场数
     */
    private Integer winCount;

    /**
     * 团队总助威金额
     */
    private BigDecimal totalCheerAmount;


    private LocalDateTime createTime; // 创建时间


    private LocalDateTime updateTime; // 更新时间
}
