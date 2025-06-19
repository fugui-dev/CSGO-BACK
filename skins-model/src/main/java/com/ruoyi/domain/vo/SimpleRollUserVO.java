package com.ruoyi.domain.vo;

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
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class SimpleRollUserVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @Excel(name = "用户ID")
    private Integer userId;

    @Excel(name = "用户名")
    private String userName;

    @Excel(name = "昵称")
    private String nickName;

    @Excel(name = "头像")
    private String avatar;

    @Excel(name = "Roll房ID")
    private Integer rollId;

    @Excel(name = "宝箱记录ID")
    private Long boxRecordId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date joinTime;

}
