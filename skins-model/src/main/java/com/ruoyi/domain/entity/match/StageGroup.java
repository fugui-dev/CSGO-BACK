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
@TableName(value = "stage_group")
public class StageGroup {

    @TableId(type = IdType.AUTO)
    private Integer id;

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

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间

}
