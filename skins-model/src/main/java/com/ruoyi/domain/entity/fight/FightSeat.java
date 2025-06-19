package com.ruoyi.domain.entity.fight;

import cn.hutool.core.util.ObjectUtil;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FightSeat {

    private Integer fightId;

    // 座位号
    private Integer code;

    private Integer playerId;

    // 玩家状态（0空位 1入座 2就绪）
    private Integer status;

    // 头像
    private String avatar;

    private String nickName;

    private BigDecimal awardTotalPrices;

    public FightSeat(Integer fightId, Integer code) {
        this.fightId = fightId;
        this.code = code;
        this.status = 0;
    }

    public FightSeat(Integer code) {
        this.code = code;
        this.status = 0;
    }

    // 入座
    public FightSeat sitDown(Integer playerId) {
        if (ObjectUtil.isEmpty(code)) return null;
        if (!status.equals(0)) return null;
        this.playerId = playerId;
        status = 1;
        return this;
    }

    // 离开座位
    public Boolean sitUp() {
        if (ObjectUtil.isEmpty(this.playerId) || ObjectUtil.isEmpty(this.code)) {
            return false;
        }
        if (!this.status.equals(1)) return false;
        this.playerId = null;
        this.status = 0;
        this.nickName = "";
        this.avatar = "";
        return true;
    }

    // 准备
    public FightSeat ready() {
        if (ObjectUtil.isEmpty(code) || ObjectUtil.isEmpty(playerId)) return null;
        if (status.equals(0)) return null;
        this.status = 2;
        return this;
    }

    // 取消准备
    public FightSeat readyCancel() {
        if (ObjectUtil.isEmpty(code) || ObjectUtil.isEmpty(playerId)) return null;
        if (status.equals(0)) return null;
        if (status.equals(1)) return this;
        this.status = 1;
        return this;
    }
}
