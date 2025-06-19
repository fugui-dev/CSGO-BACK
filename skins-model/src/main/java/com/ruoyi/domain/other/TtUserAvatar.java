package com.ruoyi.domain.other;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_user_avatar")
public class TtUserAvatar implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "用户头像")
    private String avatar;

    @Excel(name = "是否默认", readConverterExp = "0=否,1=是")
    private String isDefault;

    @Excel(name = "头像排序")
    private Integer sort;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}
