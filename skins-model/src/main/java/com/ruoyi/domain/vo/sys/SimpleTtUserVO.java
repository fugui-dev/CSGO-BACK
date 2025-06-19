package com.ruoyi.domain.vo.sys;

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
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class SimpleTtUserVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "用户ID")
    @TableId(value = "user_id",type = IdType.AUTO)
    private Integer userId;

    @Excel(name = "用户账号")
    private String userName;

    @Excel(name = "用户昵称")
    private String nickName;

    //01主播 02普通用户
    private String userType;

    @Excel(name = "用户邮箱")
    private String email;

    @Excel(name = "手机号码")
    private String phoneNumber;

    @Excel(name = "头像地址")
    private String avatar;

    // 充值金额
    private BigDecimal recharge;

    // 金币消费
    private BigDecimal amountConsume;

    // 弹药消费
    private BigDecimal creditsConsume;

    // 综合消费
    private BigDecimal beConsume;

    // @Excel(name = "账户金额")
    // private BigDecimal accountAmount;
    //
    // @Excel(name = "账户弹药")
    // private BigDecimal accountCredits;

    // @Excel(name = "邀请码")
    // private String invitationCode;

    @Excel(name = "上级ID")
    private Integer parentId;

    @Excel(name = "VIP等级")
    private Integer vipLevel;

    @Excel(name = "推广等级")
    private Integer promotionLevel;

    private String status;

    private String deliveryStatus;

    // @Excel(name = "steam账号ID")
    // private Long steamId;
    //
    // @Excel(name = "steam交易链接")
    // @TableField("transaction_link")
    // private String transactionLink;

    // @Excel(name = "真实姓名")
    // private String realName;
    //
    // @Excel(name = "身份证号码")
    // private String idNum;
    //
    // @Excel(name = "实名认证流程号")
    // private String certifyId;
    //
    // private String isRealCheck;

    @Excel(name = "最后登录IP")
    private String loginIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginDate;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Excel(name = "备注")
    private String remark;

    @TableField(select = false)
    private String delFlag;
}
