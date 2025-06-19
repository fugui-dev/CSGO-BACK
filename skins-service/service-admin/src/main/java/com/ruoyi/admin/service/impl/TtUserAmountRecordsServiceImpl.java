package com.ruoyi.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.TtUserAmountRecordsService;
import com.ruoyi.admin.service.TtUserBlendErcashService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.sys.MoneyType;
import com.ruoyi.domain.dto.promo.UserPlayInfoDTO;
import com.ruoyi.domain.dto.userRecord.AmountRecordsDetailCondition;
import com.ruoyi.domain.entity.TtCommissionRecord;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.recorde.TtUserAmountRecords;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtUserAmountRecordsBody;
import com.ruoyi.domain.vo.TeamDetailVO;
import com.ruoyi.domain.vo.TtUserAccountRecordsRankVO;
import com.ruoyi.domain.vo.TtUserAmountRecords.PWelfareVO;
import com.ruoyi.domain.vo.TtUserAmountRecords.PersonBlendErcashVO;
import com.ruoyi.domain.vo.TtUserAmountRecords.UserAmountDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.math.BigDecimal.ROUND_HALF_UP;

@Slf4j
@Service
public class TtUserAmountRecordsServiceImpl extends ServiceImpl<TtUserAmountRecordsMapper, TtUserAmountRecords> implements TtUserAmountRecordsService {


    @Autowired
    private TtUserCreditsRecordsMapper ttUserCreditsRecordsMapper;

    @Autowired
    private TtUserBlendErcashMapper ttUserBlendErcashMapper;

    @Autowired
    private TtUserBlendErcashService ttUserBlendErcashService;

    @Autowired
    private TtUserAmountRecordsMapper ttUserAmountRecordsMapper;

    @Autowired
    private TtPromotionUpdateMapper ttPromotionUpdateMapper;

    @Autowired
    private TtUserMapper userMapper;

    @Override
    public List queryList(TtUserAmountRecordsBody param) {

//        param.setLimit((param.getPage() - 1) * param.getSize());

//        Page<TtUserAmountRecords> pageInfo = new Page<>(param.getPage(), param.getSize());

        List<TtUserAmountRecords> list = baseMapper.queryList(param);
//        Integer totalSize = baseMapper.totalSize(param);

//        pageInfo.setRecords(list);
//        pageInfo.setTotal(totalSize);

        return list;
    }

    @Override
    public Page<TtUserAccountRecordsRankVO> rank(Timestamp begin, Timestamp end, Integer page, Integer size) {

        Page<TtUserAccountRecordsRankVO> pageInfo = new Page<>(page, size);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginT = dateFormat.format(begin);
        String endT = dateFormat.format(end);

        List<TtUserAccountRecordsRankVO> rank = baseMapper.rank(beginT, endT, page - 1, size);
        pageInfo.setRecords(rank);
        pageInfo.setTotal(rank.size());
        return pageInfo;

    }

    // TODO 梳理充值网推广奖励明细
    @Override
    public R pWelfareRecords(Integer uid, Integer page, Integer size) {

        // 测试时间段（今天00：00：00 - 现在）-------------
        Calendar c = Calendar.getInstance();
        Timestamp end = new Timestamp(c.getTimeInMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp begin = new Timestamp(c.getTimeInMillis());
        // 测试时间段------------------------------------

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginT = dateFormat.format(begin);
        String endT = dateFormat.format(end);

        if (StringUtils.isBlank(beginT) || StringUtils.isBlank(endT)) {
            log.warn("推广福利统计失败！");
            return R.fail("时间 ！！！");
        }

        Page<TtUserBlendErcash> pageInfo = new Page<>(page, size);
        pageInfo.setOptimizeCountSql(false);

        LambdaQueryWrapper<TtUserBlendErcash> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(TtUserBlendErcash::getUserId, uid)
                .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.P_WELFARE.getCode())
                .orderByDesc(TtUserBlendErcash::getCreateTime);

        // 历史收益明细
        pageInfo = ttUserBlendErcashService.page(pageInfo, wrapper);
        PWelfareVO pWelfareVO = PWelfareVO.builder()
                .timeTotal(BigDecimal.ZERO)
                .details(pageInfo)
                .build();

        // 今日预计收益
        BigDecimal pWelfareByTime = pWelfarePrizeToBoss(uid, beginT, endT);
        pWelfareVO.setTodayPredict(pWelfareByTime);

        // BigDecimal pWelfareByTime = ttUserAmountRecordsMapper.pWelfareByTime(uid,beginT, endT);
        // if (ObjectUtil.isEmpty(pWelfareByTime)) pWelfareByTime = BigDecimal.ZERO;
        // pWelfareByTime = pWelfareByTime.abs().setScale(2,BigDecimal.ROUND_HALF_UP);
        // pWelfareVO.setTodayPredict(pWelfareByTime);

        // 历史总收益
        BigDecimal pWHistoryTotal = baseMapper.pWHistoryTotal(uid);
        pWelfareVO.setHistoryTotal(pWHistoryTotal);

        return R.ok(pWelfareVO);
    }

    public BigDecimal pWelfarePrizeToBoss(Integer bossId,String beginTimeStr,String endTimeStr){

        // 解析时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date beginTime = null;
        try {
            beginTime = dateFormat.parse(beginTimeStr);
        } catch (ParseException e) {
            log.warn("日期解析异常。");
            return BigDecimal.ZERO;
        }

        // 查询所有下级id
        List<Integer> allEmployeesId = userMapper.allEmployeesByParents(Arrays.asList(bossId));

        // 查询下级的最近一个绑定时间
        if (allEmployeesId.isEmpty()) return BigDecimal.ZERO;
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

        TtUser ttUser = userMapper.selectTtUserById(bossId.longValue());
        if ("01".equals(ttUser.getUserType())) {
            return totalConsume.multiply(new BigDecimal("0.045")).setScale(2,ROUND_HALF_UP);
        } else {
            return totalConsume.multiply(new BigDecimal("0.01")).setScale(2,ROUND_HALF_UP);
        }
    }

    @Override
    public List<UserAmountDetailVO> userAccountDetail(AmountRecordsDetailCondition param) {

        param.setLimit((param.getPage() - 1) * param.getSize());

        List<UserAmountDetailVO> res = ttUserBlendErcashMapper.userAccountDetail(param);

        if (param.getMoneyType().equals(MoneyType.GOLD.getCode())) {

        } else if (param.getMoneyType().equals(MoneyType.CREDITS.getCode())) {

            res.stream().forEach(item->{
                item.setAmount(item.getCredits());
            });

        }

        return res;
    }

    //流水网返佣记录查询
    @Override
    public R<PWelfareVO> pCommissionRecords(Integer uid, Integer page, Integer size) {

        // 测试时间段（今天00：00：00 - 现在）-------------
        Calendar c = Calendar.getInstance();
        Timestamp end = new Timestamp(c.getTimeInMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp begin = new Timestamp(c.getTimeInMillis());
        // 测试时间段------------------------------------

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginT = dateFormat.format(begin);
        String endT = dateFormat.format(end);

        if (StringUtils.isBlank(beginT) || StringUtils.isBlank(endT)) {
            log.warn("推广福利统计失败！");
            return R.fail("时间 ！！！");
        }

        Page<TtUserBlendErcash> pageInfo = new Page<>(page, size);
        pageInfo.setOptimizeCountSql(false);

        LambdaQueryWrapper<TtUserBlendErcash> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(TtUserBlendErcash::getUserId, uid)
                .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.PROMOTION_COMMISSION_WELFARE.getCode())
                .orderByDesc(TtUserBlendErcash::getId);

        // 历史收益明细
        pageInfo = ttUserBlendErcashService.page(pageInfo, wrapper);
        PWelfareVO pWelfareVO = PWelfareVO.builder()
                .timeTotal(BigDecimal.ZERO)
                .details(pageInfo)
                .build();

        // 今日预计收益
        pWelfareVO.setTodayPredict(BigDecimal.ZERO);
        UserPlayInfoDTO infoDTO = ttUserBlendErcashMapper.calcPlayTotalByTimeScopeAndUser(beginT, endT, uid);
        if (infoDTO != null && infoDTO.getCommissionRate() != null){
            TtUser user = userMapper.selectById(uid);
            pWelfareVO.setTodayPredict(infoDTO.getTotalAmount().multiply(user.getCommissionRate()));
        }


        // 历史总收益
//        BigDecimal pWHistoryTotal = baseMapper.pWHistoryTotal(uid);
//        pWelfareVO.setHistoryTotal(pWHistoryTotal);

        return R.ok(pWelfareVO);
    }
}
