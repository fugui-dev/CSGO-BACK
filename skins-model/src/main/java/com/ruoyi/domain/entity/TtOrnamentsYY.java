package com.ruoyi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_ornament_yy")
public class TtOrnamentsYY implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    @Excel(name = "哈希名称")
    @TableField("hash_name")
    private String hashName;

    @Excel(name = "名称")
    @TableField("name")
    private String name;

    @Excel(name = "类型id")
    @TableField("type_id")
    private Integer typeId;

    @Excel(name = "type_name")
    @TableField("type_name")
    private String typeName;

    @Excel(name = "type_hash_name")
    @TableField("type_hash_name")
    private String typeHashName;

    @Excel(name = "weapon_id")
    @TableField("weapon_id")
    private Integer weaponId;

    @Excel(name = "weapon_name")
    @TableField("weapon_name")
    private String weaponName;

    @Excel(name = "weapon_hash_name")
    @TableField("weapon_hash_name")
    private String weaponHashName;

    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private Timestamp updateTime;
}
