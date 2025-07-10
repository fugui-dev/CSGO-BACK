package com.ruoyi.playingmethod.model.match;

import lombok.Data;

@Data
public class MatchUserExamineCmd {

    /**
     * 审核记录ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 审核状态(0-待审核,1-已通过,2-已拒绝)
     */
    private Integer status;

    /**
     * 审核意见
     */
    private String opinion;
}
