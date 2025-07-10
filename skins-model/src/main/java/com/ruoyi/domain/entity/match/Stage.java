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
@TableName(value = "stage")
public class Stage {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 阶段类型(1-小组赛,2-淘汰赛(8强),3-淘汰赛(4强),4-决赛)
     */
    private Integer type;
    /**
     * 阶段名称
     */
    private String name;

    /**
     * 阶段描述
     */
    private String description;

    /**
     * 阶段开始时间
     */
    private LocalDateTime startTime;

    /**
     * 阶段结束时间
     */
    private LocalDateTime endTime;

    /**
     * 阶段状态(0-未开始,1-进行中,2-已结束)
     */
    private Integer status;

    /**
     * 比赛ID
     */
    private Integer matchId;

    /**
     * 每个阶段的最大队伍数量
     */
    private Integer teamNum;


    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间


}
