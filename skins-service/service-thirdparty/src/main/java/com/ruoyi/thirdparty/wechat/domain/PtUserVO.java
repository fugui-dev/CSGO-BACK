package com.ruoyi.thirdparty.wechat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 平台用户信息管理对象 tt_user
 * 
 * @author junhai
 * @date 2023-08-12
 */
public class PtUserVO extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** steam id */
    @Excel(name = "steam id")
    private Long steamid;

    /** 用户名称 */
    @Excel(name = "用户名称")
    private String name;

    /** 用户头像 */
    @Excel(name = "用户头像")
    private String portrait;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 登录密码 */
    @Excel(name = "登录密码")
    private String password;

    /** 登录密码错误次数(最多四次) */
    @Excel(name = "登录密码错误次数(最多四次)")
    private Integer passwordError;

    /** 邮箱账号 */
    @Excel(name = "邮箱账号")
    private String email;

    /** 用户余额 */
    @Excel(name = "用户余额")
    private BigDecimal bean;

    /** 商城余额 */
    @Excel(name = "商城余额")
    private BigDecimal storeBean;

    /** 上级id */
    @Excel(name = "上级id")
    private Long superiorId;

    /** 邀请码 */
    @Excel(name = "邀请码")
    private String invitationCode;

    /** 交易链接 */
    @Excel(name = "交易链接")
    private String transactionLink;

    /** 用户类型 1普通用户 2主播 3推广 4官方机器人 */
    @Excel(name = "用户类型 1普通用户 2主播 3推广 4官方机器人")
    private Integer type;

    /** 推广等级 */
    @Excel(name = "推广等级")
    private Long spreadGrade;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private BigDecimal spreadAmount;

    /** 爆率等级 */
    @Excel(name = "爆率等级")
    private Long winningGrade;

    /** 当前爆率是否增加概率 */
    @Excel(name = "当前爆率是否增加概率")
    private Integer winningAdd;

    /** 上次爆率等级结束时间 */
    @Excel(name = "上次爆率等级结束时间")
    private Long winningTime;

    /** 充值是否可更改爆率等级 1是 0否 */
    @Excel(name = "充值是否可更改爆率等级 1是 0否")
    private Integer winningStatus;

    /** 加入时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "加入时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date time;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endTime;

    /** vip到期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "vip到期时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date vipTime;

    /** vip服务 1独特的优质会员标识 2私人资料 3免费赠品 4优质会员箱子 */
    @Excel(name = "vip服务 1独特的优质会员标识 2私人资料 3免费赠品 4优质会员箱子")
    private String vipService;

    /** ip地址 */
    @Excel(name = "ip地址")
    private String ip;

    /** 登录状态 1正常 0禁止 */
    @Excel(name = "登录状态 1正常 0禁止")
    private Integer status;

    /** 充值提货状态 1正常 0禁止 */
    @Excel(name = "充值提货状态 1正常 0禁止")
    private Integer status2;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Integer rewardNewUser;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Long lotteryTimes;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private BigDecimal shipmentPrice;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private BigDecimal rechargeHour;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private BigDecimal recharge12hour;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private BigDecimal recharge24hour;

    /** 最后登录IP */
    @Excel(name = "最后登录IP")
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date loginDate;

    /** 删除标志（0代表存在 2代表删除） */
    private Long delFlag;

    /** 真实姓名 */
    @Excel(name = "真实姓名")
    private String realName;

    /** 身份证号码 */
    @Excel(name = "身份证号码")
    private String idNumber;

    /** 是否实名认证0未认证，1已认证 */
    @Excel(name = "是否实名认证0未认证，1已认证")
    private Long isRealCheck;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setSteamid(Long steamid)
    {
        this.steamid = steamid;
    }

    public Long getSteamid()
    {
        return steamid;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    public void setPortrait(String portrait)
    {
        this.portrait = portrait;
    }

    public String getPortrait()
    {
        return portrait;
    }
    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPhone()
    {
        return phone;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPassword()
    {
        return password;
    }
    public void setPasswordError(Integer passwordError)
    {
        this.passwordError = passwordError;
    }

    public Integer getPasswordError()
    {
        return passwordError;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getEmail()
    {
        return email;
    }
    public void setBean(BigDecimal bean)
    {
        this.bean = bean;
    }

    public BigDecimal getBean()
    {
        return bean;
    }
    public void setStoreBean(BigDecimal storeBean)
    {
        this.storeBean = storeBean;
    }

    public BigDecimal getStoreBean()
    {
        return storeBean;
    }
    public void setSuperiorId(Long superiorId)
    {
        this.superiorId = superiorId;
    }

    public Long getSuperiorId()
    {
        return superiorId;
    }
    public void setInvitationCode(String invitationCode)
    {
        this.invitationCode = invitationCode;
    }

    public String getInvitationCode()
    {
        return invitationCode;
    }
    public void setTransactionLink(String transactionLink)
    {
        this.transactionLink = transactionLink;
    }

    public String getTransactionLink()
    {
        return transactionLink;
    }
    public void setType(Integer type)
    {
        this.type = type;
    }

    public Integer getType()
    {
        return type;
    }
    public void setSpreadGrade(Long spreadGrade)
    {
        this.spreadGrade = spreadGrade;
    }

    public Long getSpreadGrade()
    {
        return spreadGrade;
    }
    public void setSpreadAmount(BigDecimal spreadAmount)
    {
        this.spreadAmount = spreadAmount;
    }

    public BigDecimal getSpreadAmount()
    {
        return spreadAmount;
    }
    public void setWinningGrade(Long winningGrade)
    {
        this.winningGrade = winningGrade;
    }

    public Long getWinningGrade()
    {
        return winningGrade;
    }
    public void setWinningAdd(Integer winningAdd)
    {
        this.winningAdd = winningAdd;
    }

    public Integer getWinningAdd()
    {
        return winningAdd;
    }
    public void setWinningTime(Long winningTime)
    {
        this.winningTime = winningTime;
    }

    public Long getWinningTime()
    {
        return winningTime;
    }
    public void setWinningStatus(Integer winningStatus)
    {
        this.winningStatus = winningStatus;
    }

    public Integer getWinningStatus()
    {
        return winningStatus;
    }
    public void setTime(Date time)
    {
        this.time = time;
    }

    public Date getTime()
    {
        return time;
    }
    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }
    public void setVipTime(Date vipTime)
    {
        this.vipTime = vipTime;
    }

    public Date getVipTime()
    {
        return vipTime;
    }
    public void setVipService(String vipService)
    {
        this.vipService = vipService;
    }

    public String getVipService()
    {
        return vipService;
    }
    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public String getIp()
    {
        return ip;
    }
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getStatus()
    {
        return status;
    }
    public void setStatus2(Integer status2)
    {
        this.status2 = status2;
    }

    public Integer getStatus2()
    {
        return status2;
    }
    public void setRewardNewUser(Integer rewardNewUser)
    {
        this.rewardNewUser = rewardNewUser;
    }

    public Integer getRewardNewUser()
    {
        return rewardNewUser;
    }
    public void setLotteryTimes(Long lotteryTimes)
    {
        this.lotteryTimes = lotteryTimes;
    }

    public Long getLotteryTimes()
    {
        return lotteryTimes;
    }
    public void setShipmentPrice(BigDecimal shipmentPrice)
    {
        this.shipmentPrice = shipmentPrice;
    }

    public BigDecimal getShipmentPrice()
    {
        return shipmentPrice;
    }
    public void setRechargeHour(BigDecimal rechargeHour)
    {
        this.rechargeHour = rechargeHour;
    }

    public BigDecimal getRechargeHour()
    {
        return rechargeHour;
    }
    public void setRecharge12hour(BigDecimal recharge12hour)
    {
        this.recharge12hour = recharge12hour;
    }

    public BigDecimal getRecharge12hour()
    {
        return recharge12hour;
    }
    public void setRecharge24hour(BigDecimal recharge24hour)
    {
        this.recharge24hour = recharge24hour;
    }

    public BigDecimal getRecharge24hour()
    {
        return recharge24hour;
    }
    public void setLoginIp(String loginIp)
    {
        this.loginIp = loginIp;
    }

    public String getLoginIp()
    {
        return loginIp;
    }
    public void setLoginDate(Date loginDate)
    {
        this.loginDate = loginDate;
    }

    public Date getLoginDate()
    {
        return loginDate;
    }
    public void setDelFlag(Long delFlag)
    {
        this.delFlag = delFlag;
    }

    public Long getDelFlag()
    {
        return delFlag;
    }
    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public String getRealName()
    {
        return realName;
    }
    public void setIdNumber(String idNumber)
    {
        this.idNumber = idNumber;
    }

    public String getIdNumber()
    {
        return idNumber;
    }
    public void setIsRealCheck(Long isRealCheck)
    {
        this.isRealCheck = isRealCheck;
    }

    public Long getIsRealCheck()
    {
        return isRealCheck;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("steamid", getSteamid())
                .append("name", getName())
                .append("portrait", getPortrait())
                .append("phone", getPhone())
                .append("password", getPassword())
                .append("passwordError", getPasswordError())
                .append("email", getEmail())
                .append("bean", getBean())
                .append("storeBean", getStoreBean())
                .append("superiorId", getSuperiorId())
                .append("invitationCode", getInvitationCode())
                .append("transactionLink", getTransactionLink())
                .append("type", getType())
                .append("spreadGrade", getSpreadGrade())
                .append("spreadAmount", getSpreadAmount())
                .append("winningGrade", getWinningGrade())
                .append("winningAdd", getWinningAdd())
                .append("winningTime", getWinningTime())
                .append("winningStatus", getWinningStatus())
                .append("time", getTime())
                .append("endTime", getEndTime())
                .append("vipTime", getVipTime())
                .append("vipService", getVipService())
                .append("ip", getIp())
                .append("status", getStatus())
                .append("status2", getStatus2())
                .append("rewardNewUser", getRewardNewUser())
                .append("lotteryTimes", getLotteryTimes())
                .append("shipmentPrice", getShipmentPrice())
                .append("rechargeHour", getRechargeHour())
                .append("recharge12hour", getRecharge12hour())
                .append("recharge24hour", getRecharge24hour())
                .append("loginIp", getLoginIp())
                .append("loginDate", getLoginDate())
                .append("delFlag", getDelFlag())
                .append("realName", getRealName())
                .append("idNumber", getIdNumber())
                .append("isRealCheck", getIsRealCheck())
                .toString();
    }
}
