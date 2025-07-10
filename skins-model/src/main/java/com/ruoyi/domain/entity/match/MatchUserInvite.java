package com.ruoyi.domain.entity.match;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "match_user_invite")
public class MatchUserInvite {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 比赛ID
     */
    private Integer matchId;

    /**
     * 队伍ID
     */
    private Integer teamId;

    /**
     * 队长用户ID
     */
    private Integer captainUserId;

    /**
     * 被邀请用户ID
     */
    private Integer invitedUserId;

    /**
     * 邀请状态(0-未接受,1-已接受,2-已拒绝)
     */
    private Integer status;

    /**
     * 邀请时间
     */
    private String inviteTime;

    /**
     * 处理时间
     */
    private String dealWithTime;

    /**
     * 邀请消息
     */
    private String message;

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
