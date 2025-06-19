package com.ruoyi.domain.other;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 汰换记录对象 tt_replacement_record
 * 
 * @author junhai
 * @date 2023-09-10
 */
public class TtReplacementRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 用户id */
    @Excel(name = "用户id")
    private Long uid;

    /** 用户名称 */
    @Excel(name = "用户名称")
    private String uname;

    /** 饰品ids */
    @Excel(name = "饰品ids")
    private String oids;

    /** 合成饰品的id */
    @Excel(name = "合成饰品的id")
    private Long awardOid;

    /** 合成饰品的名称 */
    @Excel(name = "合成饰品的名称")
    private String awardOname;

    /** 合成饰品的价格 */
    @Excel(name = "合成饰品的价格")
    private BigDecimal awardOprice;

    /**
     * 饰品的图片
     */
    @Excel(name = "饰品的图片")
    private String awardOimg;

    /** 合成日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "合成日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date time;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setUid(Long uid) 
    {
        this.uid = uid;
    }

    public Long getUid() 
    {
        return uid;
    }
    public void setUname(String uname) 
    {
        this.uname = uname;
    }

    public String getUname() 
    {
        return uname;
    }
    public void setOids(String oids) 
    {
        this.oids = oids;
    }

    public String getOids() 
    {
        return oids;
    }
    public void setAwardOid(Long awardOid) 
    {
        this.awardOid = awardOid;
    }

    public Long getAwardOid() 
    {
        return awardOid;
    }
    public void setAwardOname(String awardOname) 
    {
        this.awardOname = awardOname;
    }

    public String getAwardOname() 
    {
        return awardOname;
    }
    public void setAwardOprice(BigDecimal awardOprice) 
    {
        this.awardOprice = awardOprice;
    }

    public BigDecimal getAwardOprice() 
    {
        return awardOprice;
    }
    public void setTime(Date time) 
    {
        this.time = time;
    }

    public Date getTime() 
    {
        return time;
    }

    public String getAwardOimg() {
        return awardOimg;
    }

    public void setAwardOimg(String awardOimg) {
        this.awardOimg = awardOimg;
    }

    @Override
    public String toString() {
        return "TtReplacementRecord{" +
                "id=" + id +
                ", uid=" + uid +
                ", uname='" + uname + '\'' +
                ", oids='" + oids + '\'' +
                ", awardOid=" + awardOid +
                ", awardOname='" + awardOname + '\'' +
                ", awardOprice=" + awardOprice +
                ", awardOimg='" + awardOimg + '\'' +
                ", time=" + time +
                '}';
    }
}
