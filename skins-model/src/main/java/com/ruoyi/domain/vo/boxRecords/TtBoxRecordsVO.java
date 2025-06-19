package com.ruoyi.domain.vo.boxRecords;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
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
public class TtBoxRecordsVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("持有者_用户ID")
    private Integer holderUserId;

    // 持有者昵称
    private String holderUserNickName;

    // 持有者头像
    private String avatar;

    @ApiModelProperty("宝箱ID")
    private Integer boxId;

    @ApiModelProperty("宝箱名称")
    private String boxName;

    @ApiModelProperty("宝箱价格")
    private BigDecimal boxPrice;

    @ApiModelProperty("饰品ID")
    @TableField("ornament_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    @ApiModelProperty("饰品名称")
    private String ornamentName;

    @ApiModelProperty("饰品级别ID")
    private Integer ornamentsLevelId;

    @ApiModelProperty("饰品级别图片")
    private String ornamentLevelImg;

    @ApiModelProperty("市场唯一hash名称")
    @TableField("market_hash_name")
    private String marketHashName;

    @ApiModelProperty("zbt ID")
    @TableField("ornaments_zbt_id")
    private String ornamentsZbtId;

    @ApiModelProperty("yy ID")
    @TableField("ornaments_yy_id")
    private String ornamentsYyId;

    @ApiModelProperty("饰品价格")
    private BigDecimal ornamentsPrice;

    @ApiModelProperty("饰品图片")
    private String imageUrl;


    // 0、在背包显示 1、已分解
    @ApiModelProperty("状态")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // 0、对战
    @ApiModelProperty("饰品来源")
    private Integer source;

    @ApiModelProperty("对战ID")
    private Integer fightId;

    @ApiModelProperty("对战模式：回合数")
    private Integer fightRoundNumber;

    @ApiModelProperty("Roll房ID")
    private Integer rollId;

    // 用于对战模式，游戏完全结束才显示
    @TableField("is_show")
    private Integer isShow;

    @ApiModelProperty("是否赠品")
    private Boolean isOpenBox2Gift;
}
