package com.ruoyi.playingmethod.scheduled;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.config.DeleteFlag;
import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.admin.service.TtFightService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.fight.FightSeat;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.FightBoutData;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.playingmethod.service.ApiBindBoxService;
import com.ruoyi.playingmethod.service.ApiFightService;
import com.ruoyi.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Slf4j
@Component("RebootTask")
public class RebootTask {

    @Autowired
    ISysConfigService configService;

    @Autowired
    TtUserService userService;

    @Autowired
    ApiBindBoxService apiBindBoxService;

    @Autowired
    TtBoxService boxService;

    @Autowired
    TtFightService fightService;

    @Autowired
    ApiFightService apiFightService;



    /**
     * 机器人对战
     */
    public void rebootFight(){

        log.info("扫描对战房间==>");
        List<TtFight> fightList = fightService.list(Wrappers.lambdaQuery(TtFight.class)
                .eq(TtFight::getStatus, "0")
                .last("limit 1"));

        if (fightList.isEmpty()){
            log.info("无等待中的对战房间，停止任务==>");
            return;
        }

        TtFight fight = fightList.get(0);
        log.info("存在等待的对战房间【{}】，开始机器人对战任务==>", fight.getId());

        //判断是否创建已经超过40秒
        if (System.currentTimeMillis() - fight.getCreateTime().getTime() < 45 * 1000){
            log.info("对战房间【{}】不足45秒，停止任务...", fight.getId());
        }

        rebootFightJoinFight(fight);

    }



        /**
         * 机器人对战
         */
    public void rebootFightJoinFight(TtFight fight){

        log.info("开始机器人对战...");

        //获取一个随机对战机器人
        TtUser reboot = getRandomReboot("fightRebootIds");

        if (reboot == null){
            log.info("未获取到对战机器人，停止机器人对战...");
            return;
        }

        //如果是三人房并且房间目前只有一个人的情况下只需要添加一个机器人
        if (fight.getPlayerNum() == 3){
            if (fight.getSeatList().size() == 1){
                joinOneReboot(fight, reboot);
                log.info("三人对战房间，先添加一个机器人【{}】", reboot.getUserId());
                return;
            }

            //如果是三人并且已经有两个人，需要判断之前的机器人不是当前随机的机器人
            boolean repeatFlag = false;
            if (fight.getSeatList().size() == 2){
                for (FightSeat seat : fight.getSeatList()) {
                    if (seat.getPlayerId().equals(reboot.getUserId())){
                        repeatFlag = true;
                    }
                }
            }

            //如果重复了再随机三次确保换个机器人，如果还是存在重复的话不再重试（所以配置对战机器人至少两个以上）
            int i = 0;
            while (repeatFlag){
                if (i++ >= 3){ //最多尝试三次随机
                    return;
                }

                TtUser reboot1 = getRandomReboot("fightRebootIds");
                log.info("三人房机器人重复【{}】，第【{}】次刷新：【{}】", reboot.getUserId(), i, reboot1.getUserId());
                if (!reboot1.getUserId().equals(reboot.getUserId())){
                    reboot = reboot1;
                    repeatFlag = false;
                    log.info("刷新到不重复机器人【{}】，开始对战逻辑==>", reboot1.getUserId());
                }
            }
        }

        //给机器人充值余额，避免对战余额不足
        reboot.setAccountAmount(new BigDecimal(10000));
        reboot.setAccountCredits(new BigDecimal(10000));
        userService.updateUserById(reboot);

            //开始机器人对战
            try {
                apiFightService.joinFight(fight.getId(), reboot);
                apiFightService.seatrReady(fight.getId(), reboot);

                //saveFightBoutData-1
                FightBoutData boutData = new FightBoutData();
                boutData.setFightId(fight.getId());
                boutData.setBoutNum(1);
                boutData.setExpirationTime(16000);
                apiFightService.saveFightBoutData(boutData);

                apiFightService.fightBegin(fight.getId(), reboot);

                for (Integer i = 0; i < fight.getRoundNumber() - 1; i++) {
                    Thread.sleep(500L);
                    boutData.setBoutNum(i + 1);
                    apiFightService.saveFightBoutData(boutData);
                }


                apiFightService.fightEnd(fight.getId());
            }catch (Exception e){
                log.error("机器人对战异常，停止任务==>", e);
            }

//        }

        log.info("机器人对战结束==>");


    }

    private void joinOneReboot(TtFight fight, TtUser reboot) {
        apiFightService.joinFight(fight.getId(), reboot);
        apiFightService.seatrReady(fight.getId(), reboot);

        //saveFightBoutData-1
        FightBoutData boutData = new FightBoutData();
        boutData.setFightId(fight.getId());
        boutData.setBoutNum(1);
        boutData.setExpirationTime(16000);
        apiFightService.saveFightBoutData(boutData);
    }


    /**
     * 机器人开箱
     */
    public void rebootOpenBox(){

        log.info("开始机器人盲盒开箱任务==>");

        //获取一个随机开箱机器人
        TtUser reboot = getRandomReboot("openBoxRebootIds");
        if (reboot == null){
            log.info("未获取到随机机器人用户，停止机器人开箱...");
            return;
        }

        //获取随机箱子
        TtBox randomBox = getRandomBox();
        if (randomBox == null){
            log.info("未获取到随机盲盒宝箱，停止机器人开箱...");
            return;
        }

        //机器人开箱
        R r = apiBindBoxService.blindBoxReboot(reboot, randomBox, getRandomOpenNum());

        log.info("结束机器人盲盒开箱任务==>【{}】", r);

    }

    //获取随机机器人
    private TtUser getRandomReboot(String cacheKey) {
        String rebootIds = configService.selectConfigByKey(cacheKey);
        String[] split = rebootIds.replace("，", ",").split(",");

        if (split.length < 1){
            return null;
        }

        int i = new Random().nextInt(split.length);
        Integer rebootId = Integer.valueOf(split[i]);
        return userService.getById(rebootId);
    }

    //获取随机开箱数
    private Integer getRandomOpenNum() {
        return new Random().nextInt(2) + 1;
    }

    //获取随机箱子
    private TtBox getRandomBox() {
        List<TtBox> boxList = boxService.list(Wrappers.lambdaQuery(TtBox.class)
                .eq(TtBox::getStatus, 0)
                .eq(TtBox::getDelFlag, DeleteFlag.NORMAL));

        if (boxList.isEmpty()){
            return null;
        }

        int i = new Random().nextInt(boxList.size());
        return boxList.get(i);

    }

}
