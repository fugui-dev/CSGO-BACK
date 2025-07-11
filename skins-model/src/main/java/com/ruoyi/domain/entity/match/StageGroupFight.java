package com.ruoyi.domain.entity.match;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "stage_group_fight")
public class StageGroupFight {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 阶段ID
     */
    private Integer stageId;

    /**
     * 分组
     */
    private String groupName;

    /**
     * 队伍ID
     */
    private Integer teamId;

    /**
     * 对手队伍ID
     */
    private Integer opponentTeamId;

    /**
     * 回合数
     */
    private Integer round;

    /**
     * 状态 (0-未开始, 1-进行中, 2-已结束)
     */
    private Integer status;
}
