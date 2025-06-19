package com.ruoyi.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.admin.mapper.TtUserBlendErcashMapper;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.common.service.CommonNoticeService;
import com.ruoyi.user.mapper.ApiTaskCenterMapper;
import com.ruoyi.user.model.TtTaskCenterUser;
import com.ruoyi.user.model.dto.YesterdayExpenditureDTO;
import com.ruoyi.user.model.vo.ApiTaskCenterVO;
import com.ruoyi.user.service.ApiTaskCenterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ApiTaskCenterServiceImpl implements ApiTaskCenterService {

    @Autowired
    private ApiTaskCenterMapper apiTaskCenterMapper;

    @Autowired
    private TtUserMapper ttUserMapper;

    @Autowired
    private TtUserBlendErcashMapper ttUserBlendErcashMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    CommonNoticeService commonNoticeService;

    @Override
    public List<ApiTaskCenterVO> selectApiTaskCenterVOList(Long userId) {
        return apiTaskCenterMapper.selectApiTaskCenterVOList(userId);
    }

    @Override
    public String selectTaskTypeByTaskId(Integer taskId) {
        return apiTaskCenterMapper.selectTaskTypeByTaskId(taskId);
    }

    @Override
    public TtTaskCenterUser selectTtTaskCenterUserByUserIdAndType(Long userId, String type) {
        return apiTaskCenterMapper.selectTtTaskCenterUserByUserIdAndType(userId, type);
    }

    @Transactional
    @Override
    public List<TtTaskCenterUser> updateYesterdayBonusPoints() {
        // 清空tt_task_center_user表中活动类型为昨日消费奖励的数据
        apiTaskCenterMapper.deleteYesterdayExpenditureBonusPoints();

        List<YesterdayExpenditureDTO> yesterdayExpenditureDTOList = apiTaskCenterMapper.getYesterdayExpenditure();
        if (yesterdayExpenditureDTOList.size() == 0) {
            return null;
        }
        // 获取参数设置中消费返弹药比例
        String rechargePointsRebateRatio = configService.selectConfigByKey("expenditure.points.rebate.ratio");
        String rechargeThreshold = configService.selectConfigByKey("expenditure.points.rebate.threshold");
        BigDecimal ratio = new BigDecimal(rechargePointsRebateRatio); //比例
        BigDecimal threshold = new BigDecimal(rechargeThreshold); //门槛
        List<TtTaskCenterUser> ttTaskCenterUserList = new ArrayList<>();
        for (YesterdayExpenditureDTO yesterdayRechargeDTO : yesterdayExpenditureDTOList) {
            //达到门槛才能累计
            if (yesterdayRechargeDTO.getTotalRecharge().compareTo(threshold) >= 0){
                TtTaskCenterUser ttTaskCenterUser = new TtTaskCenterUser();
                ttTaskCenterUser.setUserId(yesterdayRechargeDTO.getUserId());
                ttTaskCenterUser.setType("0"); // 0为昨日充值任务
                ttTaskCenterUser.setCredit(yesterdayRechargeDTO.getTotalRecharge().multiply(ratio));
                ttTaskCenterUserList.add(ttTaskCenterUser);
            }
        }
        // 批量增加昨日的弹药奖励数据
        apiTaskCenterMapper.insertYesterdayExpenditureBonusPoints(ttTaskCenterUserList);

        return ttTaskCenterUserList;
    }

    @Override
    public void autoTopUpRebate() {
        log.info("自动统计发放昨天充值福利...");

        //获取到昨天充值的奖励数据
        List<TtTaskCenterUser> taskCenterUserList = updateYesterdayBonusPoints();

        if (taskCenterUserList == null || taskCenterUserList.isEmpty()){
            log.info("昨日无充值，充值返利任务结束==>");
            return;
        }
        //开始赠送
        threadPoolTaskExecutor.execute(()->{
            for (TtTaskCenterUser taskCenterUser : taskCenterUserList) {
                try {
                    Long userId = taskCenterUser.getUserId();
                    BigDecimal topUpRebateMoney = taskCenterUser.getCredit(); //返利金额
                    TtUser user = ttUserMapper.selectTtUserById(userId);
                    Asserts.notNull(user, "未找到对应用户！");

                    //新增综合流水记录
                    TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                            .userId(userId.intValue())
                            .amount(topUpRebateMoney.compareTo(BigDecimal.ZERO) >= 0 ? topUpRebateMoney : null)
                            .finalAmount(topUpRebateMoney.compareTo(BigDecimal.ZERO) > 0 ? user.getAccountAmount().add(topUpRebateMoney) : null)
                            .total(topUpRebateMoney)
                            .type(TtAccountRecordType.INPUT.getCode())
                            .source(TtAccountRecordSource.DAY_TOTAL_CHARGE.getCode())
                            .remark(TtAccountRecordSource.DAY_TOTAL_CHARGE.getMsg())
                            .createTime(new Timestamp(System.currentTimeMillis()))
                            .build();
                    int insert = ttUserBlendErcashMapper.insert(blendErcash);

                    //更新用户账户余额
                    ttUserMapper.updateAccountAmount(userId, topUpRebateMoney);

                    commonNoticeService.sendTopUpRebateNotice(userId.toString(), topUpRebateMoney);

                    log.info("发放用户充值返利结果【{}】==>【{}】", insert, blendErcash);

                }catch (Exception e){
                    log.error("充值福利【{}】发放到账户出现异常==>", taskCenterUser, e);
                }
            }

            log.info("充值返利任务结束==>");
        });
    }

    /**
     * 之前是弹药，后来改金币了
     * @param userId
     * @param type
     * @return
     */
    @Transactional
    @Override
    public AjaxResult getReward(Long userId, String type) {
        // 根据ID查询奖励金币
        BigDecimal credit = apiTaskCenterMapper.selectCreditByUserIdAndType(userId, type);

        // 将奖励金币写入到tt_user表
        TtUser ttUser = ttUserMapper.selectById(userId);
        ttUser.setAccountCredits(ttUser.getAccountCredits().add(credit));
        int row = ttUserMapper.updateById(ttUser);

        // 将奖励金币写入tt_user_blend_ercash表
        LambdaQueryWrapper<TtUserBlendErcash> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TtUserBlendErcash::getUserId, userId)
                .orderByDesc(TtUserBlendErcash::getCreateTime)
                .last("LIMIT 1");
        TtUserBlendErcash lastTtUserBlendErcash = ttUserBlendErcashMapper.selectOne(queryWrapper);
        TtUserBlendErcash ttUserBlendErcash = new TtUserBlendErcash();
        ttUserBlendErcash.setUserId(userId.intValue());
        ttUserBlendErcash.setCredits(credit);
        ttUserBlendErcash.setCreateTime(new Timestamp(new Date().getTime()));
        if (!Objects.isNull(lastTtUserBlendErcash) && !Objects.isNull(lastTtUserBlendErcash.getFinalCredits())) {
            ttUserBlendErcash.setFinalCredits(lastTtUserBlendErcash.getFinalCredits().add(credit));
        } else {
            ttUserBlendErcash.setFinalCredits(credit);
        }
        ttUserBlendErcash.setTotal(credit);
        ttUserBlendErcash.setSource(6);
        ttUserBlendErcash.setType(1);
        ttUserBlendErcash.setRemark("昨日消费返弹药");
        ttUserBlendErcashMapper.insert(ttUserBlendErcash);
        // 标记为已领取
        apiTaskCenterMapper.markAsClaimedByUserIdAndType(userId, type);
        return row > 0 ? AjaxResult.success("成功领取" + credit + "弹药") : AjaxResult.error("领取失败");
    }

    @Override
    public void autoRankAward() {

    }
}
