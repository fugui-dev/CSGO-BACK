package com.ruoyi.playingmethod.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.fight.FightDetailParam;
import com.ruoyi.domain.dto.fight.FightOnMyOwnParam;
import com.ruoyi.domain.other.CreateFightBody;
import com.ruoyi.domain.other.FightBoutData;
import com.ruoyi.domain.other.TtBoxVO;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.vo.ApiFightListDataVO;
import com.ruoyi.domain.vo.FightResultDataVO;
import com.ruoyi.domain.vo.TtBoxOrnamentsDataVO;
import com.ruoyi.domain.vo.fight.FightResultVO;
import com.ruoyi.domain.vo.fight.TtFightVO;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;
import com.ruoyi.playingmethod.model.vo.ApiFightRankingVO;
import com.ruoyi.playingmethod.model.vo.TtFightBoxOrnamentsDataVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface ApiFightService extends IService<TtFight> {

    /**
     * 创建房间 -》支付费用 -》加入座位 -》添加准备
     * @param createFightParamVO
     * @param ttUser
     * @return
     */
    R<Object> createFight(CreateFightBody createFightParamVO, TtUser ttUser);

    /**
     * 加入房间 -》 支付费用 -》加入座位
     * @param fightId
     * @param player
     * @return
     */
    R<Object> joinFight(Integer fightId, TtUser player);

    /**
     * 获取所有对战宝箱
     * @param boxTypeId
     * @return
     */
    List<TtBoxVO> getFightBoxList(Integer boxTypeId);

    /**
     * 获取对战宝箱详情
     * @param boxId
     * @return
     */
    List<TtFightBoxOrnamentsDataVO> getFightBoxDetail(Integer boxId);

    List<ApiFightListDataVO> getFightList(String model, String status, Integer userId, Integer fightId);

    FightResultDataVO getFightRecord(Integer fightId, Integer round, Integer rounds);

    /**
     * 比赛开始 -》计算所有回合战斗结果 -》ws推送数据 -》关闭ws
     * @param fightId
     * @return
     */
    R fightBegin(Integer fightId, TtUser player);

    /**
     * 观战 -》获取所有数据，以及当前回合
     * @param fightId
     * @return
     */
    R audience(Integer fightId);

    /**
     * 比赛结束 -》修改房间状态 -》更新背包饰品显示状态 -》推送房间数据
     * @param fightId
     * @return
     */
    R fightEnd(Integer fightId);

    /**
     * 比赛准备 -》修改房间状态 -》推送房间数据
     * @param fightId
     * @param player
     * @return
     */
    R seatrReady(Integer fightId, TtUser player);



    R fightRoomExit(Integer fightId, TtUser player);


    /**
     * 获取我参加的战斗列表
     * @param param
     * @return
     */
    List<ApiFightListDataVO> getFightList(FightOnMyOwnParam param);



    R fightDetail(FightDetailParam param);


    R earlierHistory(FightDetailParam param);

    /**
     * 获取排行榜
     * @param date
     * @return
     */
    List<ApiFightRankingVO> getFightRankingByDate(String date);


    /**
     * 获取用户的对战宝箱总价
     * @param userId
     * @param date
     * @return
     */
    BigDecimal getTotalBoxPriceByDate(Long userId,String date);

    /**
     * 保存战斗当前回合数
     * @param fightBoutData
     * @return
     */
    R<Boolean> saveFightBoutData(FightBoutData fightBoutData);

    /**
     * 获取战斗当前回合数
     * @param fightId
     * @return
     */
    R<Integer> getFightBoutNum(Integer fightId);

}
