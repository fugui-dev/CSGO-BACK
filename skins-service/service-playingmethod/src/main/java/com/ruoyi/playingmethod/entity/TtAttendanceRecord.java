package com.ruoyi.playingmethod.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class TtAttendanceRecord {

    private static final long serialVersionUID = 1L;

    /**  */
    private Integer id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Integer userId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 获取金币数 */
    @Excel(name = "获取金币数")
    private Integer coin;

    @Excel(name = "是否删除")
    private String delFlag;


    private String isStatus = "0";

}
