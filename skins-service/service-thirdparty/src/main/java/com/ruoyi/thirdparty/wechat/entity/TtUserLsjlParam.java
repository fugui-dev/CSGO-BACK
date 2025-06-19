package com.ruoyi.thirdparty.wechat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.Data;

import java.util.Date;

@Data
public class TtUserLsjlParam {

    /** 流水id */
    private Long id;

    /** 用户id */
    @Excel(name = "用户id")
    private Long ttUserId;

    /** 上上级用户id **/
    private Long ttSsUserId;

    /** 类型 **/
    private String type;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "TtUserLsjlParam{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
