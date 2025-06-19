package com.ruoyi.domain.other;

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
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_box")
public class TtBox implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "宝箱ID")
    @TableId
    private Integer boxId;

    @Excel(name = "宝箱名称")
    private String boxName;

    @Excel(name = "宝箱所属分类ID")
    private String boxTypeId;

    @Excel(name = "宝箱价格")
    private BigDecimal price;

    @Excel(name = "宝箱图片01")
    private String boxImg01;

    @Excel(name = "宝箱图片02")
    private String boxImg02;

    @Excel(name = "宝箱排序")
    private Integer sort;

    // 0：是对战宝箱
    @Excel(name = "是否对战宝箱")
    private String isFight;

    @Excel(name = "宝箱状态")
    private String status;

    @Excel(name = "宝箱开启次数")
    private Long openNum;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Excel(name = "添加价值高的饰品开箱次数")
    private Long highValueOpenNum;

    @Excel(name = "必中价值高的饰品开箱次数")
    private Long mustHighValueOpenNum;


    private String remark;

    @Excel(name = "是否首页推荐")
    private String isHome;

    @TableField(select = false)
    @Excel(name = "删除标志")
    private String delFlag;
}
