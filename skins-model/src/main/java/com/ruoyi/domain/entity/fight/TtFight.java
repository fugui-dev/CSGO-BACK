package com.ruoyi.domain.entity.fight;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.domain.entity.fight.FightSeat;
import com.ruoyi.domain.vo.fight.FightBoxVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_fight",autoResultMap = true)
public class TtFight implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "创建者ID")
    private Integer userId;

    @Excel(name = "座位")
    @TableField(value = "seats",typeHandler = JacksonTypeHandler.class)
    private List<FightSeat> seats;

    // 1、欧皇 2、非酋
    @Excel(name = "对战模式")
    private String model;

    @Excel(name = "对战人数")
    private Integer playerNum;

    /**
     * 观战人数
     */
    @TableField(exist = false)
    private Integer spectatorNum;

    @Excel(name = "回合数")
    private Integer roundNumber;

    @TableField(value = "winner_ids",typeHandler = JacksonTypeHandler.class)
    private List<Integer> winnerIds;

    @Excel(name = "选择宝箱数据")
    @TableField(value = "box_data",typeHandler = JacksonTypeHandler.class)
    private Map<String, FightBoxVO> boxData;

    @Excel(name = "创建宝箱价格总数")
    private BigDecimal boxPriceTotal;

    // 对战状态（0准备 1进行中 2结束 3超时强制结束）
    @Excel(name = "对战状态")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    // 开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp beginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Excel(name = "备注")
    private String remark;

    @Excel(name = "总出货价格")
    private BigDecimal openTotalPrice;

    private Integer delFlag;
}
