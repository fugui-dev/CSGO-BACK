package com.ruoyi.domain.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 推广渠道通道对象 tb_promotion_channel
 * 
 * @author ruoyi
 * @date 2024-06-29
 */
public class TbPromotionChannel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 渠道id */
    private Long id;

    /** 渠道名称 */
    @Excel(name = "渠道名称")
    private String channelName;

    /** 渠道域名 */
    @Excel(name = "渠道域名")
    private String channelDomain;

    /** 百度token */
    @Excel(name = "百度token")
    private String bdToken;

    /** 登录密码 */
    @Excel(name = "登录密码")
    private String password;

    /** 启用状态 */
    @Excel(name = "启用状态")
    private Integer status;

    /** 删除标识 */
    private Integer delFlag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setChannelName(String channelName) 
    {
        this.channelName = channelName;
    }

    public String getChannelName() 
    {
        return channelName;
    }
    public void setChannelDomain(String channelDomain) 
    {
        this.channelDomain = channelDomain;
    }

    public String getChannelDomain() 
    {
        return channelDomain;
    }
    public void setBdToken(String bdToken) 
    {
        this.bdToken = bdToken;
    }

    public String getBdToken() 
    {
        return bdToken;
    }
    public void setPassword(String password) 
    {
        this.password = password;
    }

    public String getPassword() 
    {
        return password;
    }
    public void setStatus(Integer status) 
    {
        this.status = status;
    }

    public Integer getStatus() 
    {
        return status;
    }
    public void setDelFlag(Integer delFlag) 
    {
        this.delFlag = delFlag;
    }

    public Integer getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("channelName", getChannelName())
            .append("channelDomain", getChannelDomain())
            .append("bdToken", getBdToken())
            .append("password", getPassword())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("createBy", getCreateBy())
            .append("updateBy", getUpdateBy())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
