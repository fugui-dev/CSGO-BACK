package com.ruoyi.domain.entity.match;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class MatchUserExamine {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 比赛ID
     */
    private Integer matchId;

    /**
     * 队伍ID
     */
    private Integer teamId;

    /**
     * 审核状态(0-待审核,1-已通过,2-已拒绝)
     */
    private Integer status;

    /**
     * 审核意见
     */
    private String opinion;
}
