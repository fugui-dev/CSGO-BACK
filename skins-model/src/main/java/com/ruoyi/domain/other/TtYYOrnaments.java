package com.ruoyi.domain.other;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_yy_ornaments")
public class TtYYOrnaments {

    @TableId(value = "id",type = IdType.AUTO)
    private String id;

    @TableField("name")
    private String name;

    @TableField("hash_name")
    private String hashName;

    @TableField("type_id")
    private Integer typeId;

    @TableField("type_name")
    private String typeName;

    @TableField("type_hash_name")
    private String typeHashName;

    @TableField("weapon_id")
    private Integer weaponId;

    @TableField("weapon_name")
    private String weaponName;

    @TableField("weapon_hash_name")
    private String weaponHashName;

    @TableField("update_time")
    private Timestamp updateTime;

}
