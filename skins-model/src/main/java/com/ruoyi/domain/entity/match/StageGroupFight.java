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
     * 分组ID
     */
    private Integer groupId;

    /**
     * 队伍ID
     */
    private Integer teamId;

    /**
     * 对手队伍ID
     */
    private Integer opponentTeamId;
}
