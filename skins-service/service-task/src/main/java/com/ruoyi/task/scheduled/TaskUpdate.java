package com.ruoyi.task.scheduled;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.TtPromotionLevelService;
import com.ruoyi.admin.service.TtUserAmountRecordsService;
import com.ruoyi.admin.service.TtUserCreditsRecordsService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.common.constant.sys.MoneyType;
import com.ruoyi.domain.entity.TtPromotionLevel;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.task.DTO.pWelfareMQData;
import com.ruoyi.domain.vo.*;
import com.ruoyi.domain.task.TtTaskDoing;
import com.ruoyi.domain.task.constant.TaskCompletionState;
import com.ruoyi.domain.vo.TtUserAmountRecords.PersonBlendErcashVO;
import com.ruoyi.domain.vo.sys.SimpleTtUserVO;
import com.ruoyi.domain.vo.task.SimpleAmountRecordVO;
import com.ruoyi.domain.vo.task.SimpleBlendErcashRecordVO;
import com.ruoyi.domain.vo.task.SimpleCreditsRecordVO;
import com.ruoyi.task.service.TtTaskDoingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ruoyi.domain.common.constant.TtAccountRecordSource.*;
import static com.ruoyi.domain.common.constant.sys.MoneyType.CREDITS;
import static com.ruoyi.domain.common.constant.sys.MoneyType.GOLD;
import static java.math.BigDecimal.ROUND_HALF_UP;

@Slf4j
@Component("TaskUpdate")      // 1.主要用于标记配置类，兼备Component的效果。
//@EnableScheduling   // 2.开启定时任务
public class TaskUpdate {

    @Autowired
    private TtUserService userService;

    @Autowired
    private TtTaskDoingService ttTaskDoingService;

    @Autowired
    private TtUserService ttUserService;

    @Autowired
    private TtUserCreditsRecordsService ttUserCreditsRecordsService;

    @Autowired
    private TtUserCreditsRecordsMapper ttUserCreditsRecordsMapper;

    @Autowired
    private TtUserAmountRecordsService ttUserAmountRecordsService;

    @Autowired
    private TtUserAmountRecordsMapper ttUserAmountRecordsMapper;

    @Autowired
    private TtUserBlendErcashMapper ttUserBlendErcashMapper;

    @Autowired
    private TtUserMapper userMapper;

    @Autowired
    private TtPromotionUpdateMapper ttPromotionUpdateMapper;

    @Autowired
    private TtPromotionLevelService promotionLevelService;

    // 刷新每日任务
    //@Scheduled(cron = "0 0 0 * * ?")
    // private void refreshDayTask() {
    //
    //     log.info("刷新{}任务", "每日流水奖励");
    //
    //     Timestamp now = new Timestamp(System.currentTimeMillis());
    //
    //     // {每日流水奖励}任务数据重置
    //     LambdaUpdateWrapper<TtTaskDoing> ttTaskDoingUpdate = new LambdaUpdateWrapper<>();
    //     ttTaskDoingUpdate
    //             .eq(TtTaskDoing::getTaskId, 2)
    //             .set(TtTaskDoing::getBeginTime, now)
    //             .set(TtTaskDoing::getCompeteTime, null)
    //             .set(TtTaskDoing::getCompletionState, TaskCompletionState.DOING.getCode())
    //             .set(TtTaskDoing::getProgress, 0);
    //     ttTaskDoingService.update(ttTaskDoingUpdate);
    //
    // }

    // 发放每日流水（充值网）排行榜前十奖励
    //@Scheduled(cron = "2 0 0 * * ?")
    // @Scheduled(cron = "0/8 * * * * ?")
    // private void amountRankPrize() {
    //
    //     log.info("发放每日流水排行榜前十奖励");
    //
    //     Calendar c = Calendar.getInstance();
    //     c.set(Calendar.HOUR_OF_DAY, 0);
    //     c.set(Calendar.MINUTE, 0);
    //     c.set(Calendar.SECOND, 0);
    //     c.set(Calendar.MILLISECOND, 0);
    //     Timestamp end = new Timestamp(c.getTimeInMillis());
    //     c.add(Calendar.DAY_OF_MONTH, -1);
    //     Timestamp begin = new Timestamp(c.getTimeInMillis());
    //
    //     // 绿币排名
    //     Page<TtUserAccountRecordsRankVO> rank = ttUserAmountRecordsService.rank(begin, end, 1, 10);
    //
    //     LambdaUpdateWrapper<TtUser> wrapper = new LambdaUpdateWrapper<>();
    //     for (TtUserAccountRecordsRankVO vo : rank.getRecords()) {
    //         switch (vo.getAccountRank()) {
    //             case 1:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "888");
    //                 break;
    //             case 2:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "388");
    //                 break;
    //             case 3:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "288");
    //                 break;
    //             case 4:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "188");
    //                 break;
    //             case 5:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "88");
    //                 break;
    //             case 6:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "78");
    //                 break;
    //             case 7:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "68");
    //                 break;
    //             case 8:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "58");
    //                 break;
    //             case 9:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "48");
    //                 break;
    //             case 10:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "38");
    //                 break;
    //         }
    //     }
    // }

    // 发放每日流水（流水网）排行榜前十奖励
    //@Scheduled(cron = "2 0 0 * * ?")
    // private void creditsRankPrize() {
    //
    //     log.info("发放每日流水排行榜前十奖励");
    //
    //     Calendar c = Calendar.getInstance();
    //     c.set(Calendar.HOUR_OF_DAY, 0);
    //     c.set(Calendar.MINUTE, 0);
    //     c.set(Calendar.SECOND, 0);
    //     c.set(Calendar.MILLISECOND, 0);
    //     Timestamp end = new Timestamp(c.getTimeInMillis());
    //     c.add(Calendar.DAY_OF_MONTH, -1);
    //     Timestamp begin = new Timestamp(c.getTimeInMillis());
    //
    //     Page<TtUserCreditsRecordsRankVO> rank = ttUserCreditsRecordsService.rank(begin, end, 1, 10);
    //     LambdaUpdateWrapper<TtUser> wrapper = new LambdaUpdateWrapper<>();
    //     for (TtUserCreditsRecordsRankVO vo : rank.getRecords()) {
    //         switch (vo.getCreditsRank()) {
    //             case 1:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "888");
    //                 break;
    //             case 2:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "388");
    //                 break;
    //             case 3:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "288");
    //                 break;
    //             case 4:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "188");
    //                 break;
    //             case 5:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "88");
    //                 break;
    //             case 6:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "78");
    //                 break;
    //             case 7:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "68");
    //                 break;
    //             case 8:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "58");
    //                 break;
    //             case 9:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "48");
    //                 break;
    //             case 10:
    //                 creditsRankPrizeF(wrapper, vo.getUserId(), "38");
    //                 break;
    //         }
    //     }
    // }

    // 发放每日最大金额出货排行榜前十奖励
    //@Scheduled(cron = "2 0 0 * * ?")
    // @Scheduled(cron = "0/8 * * * * ?")
    public void propRankOfDay() {

        log.info("发放每日出货排行榜前十奖励");

        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp end = new Timestamp(c.getTimeInMillis());
        c.add(Calendar.DAY_OF_MONTH, -1);
        Timestamp begin = new Timestamp(c.getTimeInMillis());

        List<TtUserPackSackDataVO> rank = userService.propRankOfDay(begin, end,10);

        LambdaUpdateWrapper<TtUser> wrapper = new LambdaUpdateWrapper<>();
        for (TtUserPackSackDataVO vo : rank) {
            switch (vo.getPriceRank()) {
                case 1:
                    rankPrizeF(CREDITS, wrapper, vo.getHolderUserId(), "888", RANK_PROD_ORN);
                    break;
                case 2:
                    rankPrizeF(CREDITS, wrapper, vo.getHolderUserId(), "388", RANK_PROD_ORN);
                    break;
                case 3:
                    rankPrizeF(CREDITS, wrapper, vo.getHolderUserId(), "288", RANK_PROD_ORN);
                    break;
                case 4:
                    rankPrizeF(CREDITS, wrapper, vo.getHolderUserId(), "188", RANK_PROD_ORN);
                    break;
                case 5:
                    rankPrizeF(CREDITS, wrapper, vo.getHolderUserId(), "88", RANK_PROD_ORN);
                    break;
                case 6:
                    rankPrizeF(CREDITS, wrapper, vo.getHolderUserId(), "78", RANK_PROD_ORN);
                    break;
                case 7:
                    rankPrizeF(CREDITS, wrapper, vo.getHolderUserId(), "68", RANK_PROD_ORN);
                    break;
                case 8:
                    rankPrizeF(CREDITS, wrapper, vo.getHolderUserId(), "58", RANK_PROD_ORN);
                    break;
                case 9:
                    rankPrizeF(CREDITS, wrapper, vo.getHolderUserId(), "48", RANK_PROD_ORN);
                    break;
                case 10:
                    rankPrizeF(CREDITS, wrapper, vo.getHolderUserId(), "38", RANK_PROD_ORN);
                    break;
            }
        }

        log.info("发放每日出货排行榜前十奖励 完成");
    }

    // 发放综合流水排行榜前十奖励
//    @Scheduled(cron = "2 15 0 * * ?")
    // @Scheduled(cron = "0 0/1 * * * ?")
    //@UpdateUserCache
    public void blendErcashRankPrize() {

        log.info("发放综合流水排行榜前五十名金币奖励。");

        // 正式时间区间-------------------------------------
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp end = new Timestamp(c.getTimeInMillis());
        c.add(Calendar.DAY_OF_MONTH, -1);
        Timestamp begin = new Timestamp(c.getTimeInMillis());

        // 测试时间区间---------------------------------
        // Calendar c = Calendar.getInstance();
        // Timestamp end = new Timestamp(c.getTimeInMillis());
        // c.set(Calendar.HOUR_OF_DAY, 0);
        // c.set(Calendar.MINUTE, 0);
        // c.set(Calendar.SECOND, 0);
        // c.set(Calendar.MILLISECOND, 0);
        // c.add(Calendar.MINUTE, -2);
        // Timestamp begin = new Timestamp(c.getTimeInMillis());
        //-------------------------------------------------

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginT = dateFormat.format(begin);
        String endT = dateFormat.format(end);
        if (StringUtils.isBlank(beginT) || StringUtils.isBlank(beginT)){
            beginT = null;
            endT = null;
        }

        List<Integer> source = Arrays.asList(
                GAME_TYPE_01.getCode(),
                GAME_TYPE_02.getCode(),
                // GAME_TYPE_03.getCode(),
                GAME_TYPE_04.getCode()
        );
        List<UserBERankVO> rank = ttUserAmountRecordsMapper.blendErcashRank(
                source,
                beginT,
                endT,
                0,
                10);

        log.info("当日排行榜奖励：【{}】", rank);

        LambdaUpdateWrapper<TtUser> wrapper = new LambdaUpdateWrapper<>();
        for (UserBERankVO vo : rank) {
            switch (vo.getBeRank()) {
                case 1:
                    rankPrizeF(GOLD, wrapper, vo.getUserId(), "188", RANK_BLEND_ERCASH);
                    break;
                case 2:
                    rankPrizeF(GOLD, wrapper, vo.getUserId(), "88", RANK_BLEND_ERCASH);
                    break;
                case 3:
                    rankPrizeF(GOLD, wrapper, vo.getUserId(), "48", RANK_BLEND_ERCASH);
                    break;
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    rankPrizeF(GOLD, wrapper, vo.getUserId(), "18", RANK_BLEND_ERCASH);
                    break;
                default:
                    //11到50都是38
//                    rankPrizeF(GOLD, wrapper, vo.getUserId(), "38", RANK_BLEND_ERCASH);
                    break;
            }
        }

        log.info("发放综合流水排行榜前50名金币奖励 完成");
    }

    // 发放推广福利奖励
    // @Scheduled(cron = "0/30 * * * * ?")    //一分钟发一次 今天截至此刻的的奖励
//    @Scheduled(cron = "2 15 0 * * ?")
    public void pWelfarePrize() {

        log.info("发放推广福利奖励。");

        // 正式时间段 ----------------------------------
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp end = new Timestamp(c.getTimeInMillis());
        c.add(Calendar.DAY_OF_MONTH, -1);
        Timestamp begin = new Timestamp(c.getTimeInMillis());
        // -----------------------------------------------

        // 测试时间段（今天00：00：00 - 现在）-------------
        // Calendar c = Calendar.getInstance();
        // Timestamp end = new Timestamp(c.getTimeInMillis());
        // c.set(Calendar.HOUR_OF_DAY, 0);
        // c.set(Calendar.MINUTE, 0);
        // c.set(Calendar.SECOND, 0);
        // c.set(Calendar.MILLISECOND, 0);
        // Timestamp begin = new Timestamp(c.getTimeInMillis());
        // 测试时间段------------------------------------

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginT = dateFormat.format(begin);
        String endT = dateFormat.format(end);

        if (StringUtils.isBlank(beginT) || StringUtils.isBlank(endT)) {
            log.warn("推广福利统计失败！");
            return;
        }

        // 查询今天下级有消费的boss
        List<Integer> bossIdList = ttUserAmountRecordsMapper.bossByHasConsumeEmployee(beginT, endT);

        // 循环所有boss发放福利
        for (Integer bossId : bossIdList){
            pWelfarePrizeToBoss(bossId,beginT,endT);
        }

        log.info("每日推广福利发放完成。");

    }

    // 根据时间区间内下级的消费发放推广福利
    public boolean pWelfarePrizeToBoss(Integer bossId,String beginTimeStr,String endTimeStr){

        // 解析时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date beginTime = null;
        try {
            beginTime = dateFormat.parse(beginTimeStr);
        } catch (ParseException e) {
            log.warn("日期解析异常，【{}】发奖失败。",bossId);
            return false;
        }

        // 查询所有下级id
        List<Integer> allEmployeesId = userMapper.allEmployeesByParents(Arrays.asList(bossId));

        // 查询下级的最近一个绑定时间
        List<TeamDetailVO> mit = ttPromotionUpdateMapper.latelyUpdate(allEmployeesId);
        if (mit.size() < allEmployeesId.size()) log.warn("下级已绑定上级，但未写入更新日志，请及时检查！！！");
        // 如果最近绑定时间大于本次查询的起始时间，以最近绑定时间为准
        List<Integer> empIds1 = new ArrayList<>();
        List<TeamDetailVO> empIds2 = new ArrayList<>();
        for (TeamDetailVO item : mit) {
            Timestamp latelyTime = item.getBeginTime();
            if (latelyTime.compareTo(new Timestamp(beginTime.getTime())) < 0) {
                empIds1.add(item.getEmployeeId());
            } else {
                empIds2.add(item);
            }
        }

        List<PersonBlendErcashVO> personBEList1 = new ArrayList<>();
        if (!empIds1.isEmpty()){
            // 当日没有 更换绑定的下级消费统计
            personBEList1 = ttUserBlendErcashMapper.personsTotalConsumeByTime(
                    empIds1,
                    beginTimeStr,
                    endTimeStr,
                    0
            );
        }

        // 当日有 更换绑定的下级消费统计
        List<PersonBlendErcashVO> personBEList2 = new ArrayList<>();
        if (!empIds2.isEmpty()){
            for (TeamDetailVO vo : empIds2){
                String beginT = dateFormat.format(vo.getBeginTime());
                PersonBlendErcashVO personBE = ttUserBlendErcashMapper.personTotalConsumeByTime(
                        vo.getEmployeeId(),
                        beginT,
                        endTimeStr,
                        0
                );
                if (ObjectUtil.isEmpty(personBE)) continue;
                personBEList2.add(personBE);
            }
        }

        personBEList1.addAll(personBEList2);

        // 发奖
        BigDecimal totalConsume = BigDecimal.ZERO;
        for (PersonBlendErcashVO vo : personBEList1){
            totalConsume = totalConsume.add(vo.getTotal().abs());
        }
        // 获取用户推广等级对应比例
        TtUser user = userService.getById(bossId);
//        if (user == null ||user.getPromotionLevel() == null) return false;
//
//        TtPromotionLevel promotionLevel = promotionLevelService.getById(user.getPromotionLevel());
//        if (promotionLevel == null ||promotionLevel.getCommissions() == null) return false;
//
//        BigDecimal prize = promotionLevel.getCommissions();

        //直接使用用户的佣金比例，不参考推广等级
        BigDecimal commissionRate = user.getCommissionRate();
        if (commissionRate == null || BigDecimal.ZERO.compareTo(commissionRate) == 0) return false;
        BigDecimal rankMoney = commissionRate.multiply(totalConsume);

        // 更新账户弹药奖励
        LambdaUpdateWrapper<TtUser> updateWrapper = new LambdaUpdateWrapper<>();
        rankPrizeF(CREDITS,updateWrapper,bossId,rankMoney.toString(), P_WELFARE);

        return false;

    }

    // 奖励
    public void rankPrizeF(MoneyType prizeMoneyType,
                            LambdaUpdateWrapper<TtUser> wrapper,
                            Integer userId,
                            String value,
                            TtAccountRecordSource source) {

        if (prizeMoneyType.equals(GOLD)) {

            TtUser user = userService.getById(userId);
            BigDecimal money = new BigDecimal(value);

            wrapper.clear();
            wrapper
                    .eq(TtUser::getUserId, userId)
                    .setSql("account_amount = account_amount + " + value);
            userService.update(wrapper);

            // 收支日志
            TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                    .userId(user.getUserId())

                    .amount(ObjectUtil.isNotEmpty(money) ? money : null)
                    .finalAmount(user.getAccountAmount().add(money))

                    .credits(null)
                    .finalCredits(null)

                    .total(money)  //收支合计

                    .type(TtAccountRecordType.INPUT.getCode())
                    .source(source.getCode())
                    .remark(source.getMsg())

                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()))
                    .build();

            int insert = ttUserBlendErcashMapper.insert(blendErcash);

        } else if (prizeMoneyType.equals(CREDITS)) {

            TtUser user = userService.getById(userId);
            BigDecimal money = new BigDecimal(value);

            wrapper.clear();
            wrapper
                    .eq(TtUser::getUserId, userId)
                    .setSql("account_credits = account_credits + " + value);
            userService.update(wrapper);

            // 收支日志
            TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                    .userId(user.getUserId())

                    .amount(null)
                    .finalAmount(null)

                    .credits(ObjectUtil.isNotEmpty(money) ? money : null)
                    .finalCredits(user.getAccountCredits().add(money))

                    .total(money)  //收支合计

                    .type(TtAccountRecordType.INPUT.getCode())
                    .source(source.getCode())
                    .remark(source.getMsg())

                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()))
                    .build();

            int insert = ttUserBlendErcashMapper.insert(blendErcash);

        } else {
            log.warn("排行榜奖励，非法的货币类型。");
        }

    }

}
