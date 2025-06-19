package com.ruoyi.promo.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.mapper.TtUserBlendErcashMapper;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.dto.promo.UserPlayInfoDTO;
import com.ruoyi.domain.entity.TtCommissionRecord;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.promo.domain.dto.TotalUserExpenditureDTO;
import com.ruoyi.promo.mapper.PromoTurnoverMapper;
import com.ruoyi.promo.service.CommissionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component("PromoTask")
public class PromoTask {

    @Autowired
    private PromoTurnoverMapper promoTurnoverMapper;

    @Autowired
    private TtUserBlendErcashMapper userBlendErcashMapper;

    @Autowired
    private CommissionRecordService commissionRecordService;

    @Autowired
    private TtUserService userService;

    private Map<Integer, BigDecimal> commissionMap = new HashMap<>();

    /**
     * tt_commission_record 佣金计算-定时任务
     * 计算昨日佣金-消费流水网
     */
    public void calcYesterdayPlayCommission(){
        //计算昨天所有流水()
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("开始计算昨天流水佣金==>，当前时间：{}", format.format(new Date()));

        // 当天零点时间戳
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date lastSecond = new Date(c.getTimeInMillis() - 1000); //0点减一秒 = 昨天最后一秒

        //昨天最后一秒，结束时间
        String endTime = format.format(lastSecond);

        //昨天零点时间戳（起始时间）
        c.add(Calendar.DATE, -1);
        String beginTime = format.format(c.getTime());

        //查询数据库
        List<UserPlayInfoDTO> dtoList = userBlendErcashMapper.calcPlayTotalByTimeScope(beginTime, endTime);
        log.info("当日佣金记录：【{}】", dtoList);

        //保存佣金数据信息
        boolean saveBatch = saveCommissionData(c, dtoList);
        log.info("日期【{}】流水网佣金汇总记录操作结果?【{}】，记录数【{}】", endTime, saveBatch, dtoList.size());

        //发放到个人账户
        if (!saveBatch){
            log.warn("保存佣金数据失败，停止发放...");
            return;
        }

        for (UserPlayInfoDTO infoDTO : dtoList) {
            pushCreditsToUser(infoDTO);

        }

    }

    private void pushCreditsToUser(UserPlayInfoDTO infoDTO) {
        BigDecimal commission = infoDTO.getTotalAmount().multiply(infoDTO.getCommissionRate());
        if (BigDecimal.ZERO.compareTo(commission) >= 0 ){
            log.warn("用户【{}】佣金为0，发放停止！", infoDTO);
            return;
        }

        TtUser user = userService.selectTtUserById(Long.valueOf(infoDTO.getUserId()));
        if (user == null){
            log.warn("用户【{}】不存在，发放停止！", infoDTO.getUserId());
            return;
        }

        //记录
        TtUserBlendErcash userBlendErcash = new TtUserBlendErcash();
        userBlendErcash.setUserId(infoDTO.getUserId());
        userBlendErcash.setAmount(null);
        userBlendErcash.setFinalAmount(null);
        userBlendErcash.setCredits(commission);
        userBlendErcash.setFinalCredits(commission.add(user.getAccountCredits()));
        userBlendErcash.setTotal(commission);
        userBlendErcash.setSource(TtAccountRecordSource.PROMOTION_COMMISSION_WELFARE.getCode());
        userBlendErcash.setType(TtAccountRecordType.INPUT.getCode());
        userBlendErcash.setCreateTime(new Timestamp(System.currentTimeMillis()));
        userBlendErcash.setRemark(TtAccountRecordSource.PROMOTION_COMMISSION_WELFARE.getMsg());
        int insert = userBlendErcashMapper.insert(userBlendErcash);
        if (insert != 1){
            return;
        }

        //用户账户更新
        LambdaUpdateWrapper<TtUser> wrapper = Wrappers.lambdaUpdate(TtUser.class)
                .eq(TtUser::getUserId, user.getUserId())
                .setSql("account_credits = account_credits + " + commission);
        boolean update = userService.update(wrapper);

        log.info("发放用户【{}】流水推广奖励成功？【{}】，佣金【{}】", update, user.getUserId(), commission);
    }

    private boolean saveCommissionData(Calendar c, List<UserPlayInfoDTO> dtoList) {
        ArrayList<TtCommissionRecord> list = new ArrayList<>(dtoList.size());
        for (UserPlayInfoDTO infoDTO : dtoList) {
            TtCommissionRecord commissionRecord = new TtCommissionRecord();
            commissionRecord.setUserId(infoDTO.getUserId());
            commissionRecord.setCommission(infoDTO.getCommissionRate().multiply(infoDTO.getTotalAmount()));
            commissionRecord.setSummaryTime(c.getTime());
            commissionRecord.setClaimStatus("0");
            commissionRecord.setClaimTime(null);
            commissionRecord.setCreate_time(new Date());
            commissionRecord.setTotalAmount(infoDTO.getTotalAmount());
            commissionRecord.setCommissionRate(infoDTO.getCommissionRate());
            list.add(commissionRecord);
        }
        boolean saveBatch = commissionRecordService.saveBatch(list);
        return saveBatch;
    }

    /**
     * 计算上月佣金-充值流水
     */
    public void calculateLastMonthCommission() {
        // 汇总所有用户上个月的充值总额
        List<TotalUserExpenditureDTO> lastMonthTotalUserExpenditure = promoTurnoverMapper.getLastMonthTotalUserExpenditure();
        for (TotalUserExpenditureDTO totalUserExpenditureDTO : lastMonthTotalUserExpenditure) {
            // 向上递归给各级代理分配佣金
            if (!Objects.isNull(totalUserExpenditureDTO.getParentId())) {
                distributeCommission(totalUserExpenditureDTO.getParentId(),
                        totalUserExpenditureDTO.getCommissionRate(),
                        totalUserExpenditureDTO.getAmount());
            }
        }
    }

    /**
     * 分配佣金
     */
    private void distributeCommission(Integer parentId,
                                      BigDecimal commissionRate,
                                      BigDecimal amount) {
        TtUser ttUser = promoTurnoverMapper.findById(parentId);
        BigDecimal parentCommissionRate = ttUser.getCommissionRate();
        BigDecimal commission = parentCommissionRate.multiply(amount).subtract(commissionRate.multiply(amount));
        log.info("代理：" + parentId + "，佣金比例：" + parentCommissionRate + "，获得佣金：" + commission);
        // 计算并存储该级代理的佣金
        // 将相同ID的代理佣金相加
        // 判断是否还有上级代理，有继续递归
        if (!Objects.isNull(ttUser.getParentId())) {
            distributeCommission(ttUser.getParentId(),
                    ttUser.getCommissionRate(),
                    amount);
        }
    }
}
