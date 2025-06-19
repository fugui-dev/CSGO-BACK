package com.ruoyi.domain.entity.sys;

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
import java.util.List;

import static com.baomidou.mybatisplus.annotation.FieldStrategy.IGNORED;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_user")
public class TtUser implements Serializable {

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

    @Excel(name = "密码")
    private String password;

    @Excel(name = "账户金额")
    private BigDecimal accountAmount;

    @Excel(name = "账户弹药")
    private BigDecimal accountCredits;

    @Excel(name = "总充值")
    private BigDecimal totalRecharge;


    @Excel(name = "邀请码")
    private String invitationCode;

    @Excel(name = "上级ID")
    @TableField(updateStrategy = IGNORED)
    private Integer parentId;

    @Excel(name = "VIP等级")
    private Integer vipLevel;

    @Excel(name = "推广等级")
    private Integer promotionLevel;

    private String status;

    private String deliveryStatus;

    @Excel(name = "steam账号ID")
    private Long steamId;

    @Excel(name = "steam交易链接")
    @TableField("transaction_link")
    private String transactionLink;

    @Excel(name = "真实姓名")
    private String realName;

    @Excel(name = "身份证号码")
    private String idNum;

    @Excel(name = "实名认证流程号")
    private String certifyId;

    private String isRealCheck;

    @TableField("bd_channel_id")
    @Excel(name = "百度推广渠道id")
    private Long bdChannelId;

    @TableField("bd_channel_url")
    @Excel(name = "百度推广渠道Url")
    private String bdChannelUrl;

    /**
     * 佣金比例
     */
    private BigDecimal commissionRate;

    //是否开启主播秀（不出蓝）
    @TableField("is_anchor_show")
    private Boolean isAnchorShow;

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

    //主播自动充值开关 0不开启 1开启
    private Integer autoRecharge;

    //0正常
    @TableField(select = false)
    private String delFlag;

    @TableField(exist = false)
    private List<TtUser> children;
}
