package com.ruoyi.playingmethod.model.match;

import lombok.Data;

@Data
public class StageGroupCmd {


    /**
     * 阶段ID
     */
    private Integer stageId;

    /**
     * 分组（A,B,C,D）
     */
    private String groupName;

    /**
     * 分组内队伍数量
     */
    private Integer teamCount; // 分组内队伍数量

    /**
     * 队伍ID
     */
    private Integer teamId;

}
