package com.ruoyi.domain.entity.fight;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
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
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_fight", autoResultMap = true)
public class TtFight implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "创建者ID")
    private Integer userId;

    @Excel(name = "座位")
    @TableField(value = "seats")
    private String seats;

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

    @TableField(value = "winner_ids")
    private String winnerIds;

    @Excel(name = "选择宝箱数据")
    @TableField(value = "box_data")
    private String boxData;

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

    @TableField(exist = false)
    private List<FightSeat> seatList;

    @TableField(exist = false)
    private Map<String, FightBoxVO> boxDataMap;

    @TableField(exist = false)
    private List<Integer> winnerList;


    public List<FightSeat> getSeatList() {

        if (this.seats == null || this.seats.isEmpty()) {
            return new ArrayList<>();
        }
        return JSONUtil.toList(this.seats, FightSeat.class);
    }

    public Map<String, FightBoxVO> getBoxDataMap() {

        if (this.boxData == null || this.boxData.isEmpty()) {
            return new HashMap<>();
        }
        return JSON.parseObject(this.boxData, new TypeReference<Map<String, FightBoxVO>>() {
        });
    }

    public List<Integer> getWinnerList() {

        if (this.winnerIds == null || this.winnerIds.isEmpty()) {
            return new ArrayList<>();
        }

        return JSONUtil.toList(this.winnerIds, Integer.class);
    }
}
