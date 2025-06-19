package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.domain.entity.roll.TtRollUser;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import com.ruoyi.domain.vo.roll.SimpleRollOrnamentVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class RollDetailsDataVO {

    private String rollName;

    private Boolean hasPW;
    // private String pw;

    private String description;

    // 房间类型
    private String rollType;

    private String rollTypeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Integer currentPeopleNum;

    private Integer peopleNum;

    private Integer ornamentsNum;

    private BigDecimal totalOrnamentsPrice;

    // 状态
    private String rollStatus;

    //充值门槛
    private BigDecimal minRecharge;

    // 参与人员列表
    // private List<SimpleRollUserVO> playerList;

    // 奖池
    private List<SimpleRollOrnamentVO> jackpotOrnamentsDataList;

    // 获奖名单
    // private List<TtBoxRecordsVO> openPrizeList;
}
