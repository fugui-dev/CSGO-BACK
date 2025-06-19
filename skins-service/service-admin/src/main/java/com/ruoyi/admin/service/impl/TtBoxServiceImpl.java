package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.ruoyi.admin.config.RedisConstants;
import com.ruoyi.admin.mapper.TtBoxTypeMapper;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.other.TtBoxOrnaments;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.admin.mapper.TtBoxMapper;
import com.ruoyi.admin.mapper.TtBoxOrnamentsMapper;
import com.ruoyi.admin.mapper.TtBoxRecordsMapper;
import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.admin.util.RandomUtils;
import com.ruoyi.domain.other.TtBoxType;
import com.ruoyi.domain.vo.BoxCacheDataVO;
import com.ruoyi.domain.other.TtBoxBody;
import com.ruoyi.domain.vo.TtBoxDataVO;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TtBoxServiceImpl extends ServiceImpl<TtBoxMapper, TtBox> implements TtBoxService {

    private final RedisCache redisCache;
    private final TtBoxRecordsMapper boxRecordsMapper;
    private final TtBoxOrnamentsMapper boxOrnamentsMapper;

    @Autowired
    private TtBoxTypeMapper ttBoxTypeMapper;

    // @Autowired
    // private LotteryMachine lotteryMachine;

    public TtBoxServiceImpl(RedisCache redisCache,
                            TtBoxRecordsMapper boxRecordsMapper,
                            TtBoxOrnamentsMapper boxOrnamentsMapper) {
        this.redisCache = redisCache;
        this.boxRecordsMapper = boxRecordsMapper;
        this.boxOrnamentsMapper = boxOrnamentsMapper;
    }

    @Override
    public PageDataInfo<TtBoxDataVO> selectTtBoxList(TtBoxBody ttBoxBody) {

        List<TtBoxDataVO> resultList = baseMapper.selectTtBoxList(ttBoxBody);
        List<TtBoxDataVO> list = resultList.stream().peek(ttBoxDataVO -> {
            BigDecimal amountConsumed = ttBoxDataVO.getAmountConsumed();
            BigDecimal aggregateAmount = ttBoxDataVO.getAggregateAmount();
            if (StringUtils.isNotNull(amountConsumed) && StringUtils.isNotNull(aggregateAmount) && amountConsumed.compareTo(BigDecimal.ZERO) !=0){

                BigDecimal totalProfit = amountConsumed.subtract(aggregateAmount);
                BigDecimal totalProfitMargin = totalProfit
                        .divide(amountConsumed, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            ttBoxDataVO.setProfit(totalProfit);
            ttBoxDataVO.setProfitMargin(totalProfitMargin + "%");

            }
        }).collect(Collectors.toList());
        PageDataInfo<TtBoxDataVO> pageDataInfo = new PageDataInfo<>();
        pageDataInfo.setCode(HttpStatus.SUCCESS);
        pageDataInfo.setMsg("查询成功");
        pageDataInfo.setRows(list);
        pageDataInfo.setTotal(new PageInfo<>(resultList).getTotal());
        return pageDataInfo;
    }

    @Override
    public String updateTtBoxById(TtBoxDataVO ttBoxDataVO) {

        String boxImg01 = ttBoxDataVO.getBoxImg01(), boxImg02 = ttBoxDataVO.getBoxImg02();
        TtBox ttBox = TtBox.builder().build();
        BeanUtils.copyBeanProp(ttBox, ttBoxDataVO);
        ttBox.setUpdateBy(SecurityUtils.getUsername());
        ttBox.setUpdateTime(DateUtils.getNowDate());
        ttBox.setBoxImg01(RuoYiConfig.getDomainName() + boxImg01);
        ttBox.setBoxImg02(RuoYiConfig.getDomainName() + boxImg02);
        this.updateById(ttBox);

        //删除该宝箱的奖池
        //BASE_POOL_KEY + item.getBoxId() + ":" + "01"

        return "";
    }

    @Override
    public void isReplenishment(Integer boxId) {
        // int realListSize = redisCache.getCacheList(RedisConstants.OPEN_BOX_ODDS + boxId + RedisConstants.REAL_ODDS_SUFFIX).size();
        // int anchorListSize = redisCache.getCacheList(RedisConstants.OPEN_BOX_ODDS + boxId + RedisConstants.ANCHOR_ODDS_SUFFIX).size();
        // if (realListSize == 0) initializeStock(boxId, 1);
        // if (anchorListSize == 0) initializeStock(boxId, 2);
    }

    @Override
    public void delCache(Integer boxId) {
        redisCache.deleteObject(RedisConstants.OPEN_BOX_ODDS + boxId + RedisConstants.REAL_ODDS_SUFFIX);
        redisCache.deleteObject(RedisConstants.OPEN_BOX_ODDS + boxId + RedisConstants.ANCHOR_ODDS_SUFFIX);
    }

    @Override
    public BoxCacheDataVO statisticsBoxData(Integer boxId, Date date) {
        BoxCacheDataVO boxCacheDataVO = BoxCacheDataVO.builder().build();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = StringUtils.isNull(date) ? dateFormat.format(DateUtils.getNowDate()) : dateFormat.format(date);
        TtBox ttBox = this.getById(boxId);
        List<Integer> ornamentsIdList = redisCache.getCacheList(RedisConstants.OPEN_BOX_ODDS + boxId + RedisConstants.REAL_ODDS_SUFFIX);
        List<TtBoxOrnaments> list = new LambdaQueryChainWrapper<>(boxOrnamentsMapper).eq(TtBoxOrnaments::getBoxId, boxId).list();
        int sum = list.stream().mapToInt(TtBoxOrnaments::getRealOdds).sum();
        List<TtBoxRecords> boxRecordsList = boxRecordsMapper.selectBoxRecordsByDate(boxId, formattedDate);
        BigDecimal todayArisePriceTotal = boxRecordsList.stream().map(TtBoxRecords::getOrnamentsPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal todayProfit = ttBox.getPrice().multiply(BigDecimal.valueOf(boxRecordsList.size())).subtract(todayArisePriceTotal);
        boxCacheDataVO.setRemainingNum(ornamentsIdList.size() + "/" + sum);
        boxCacheDataVO.setTodayOpenNum(boxRecordsList.size());
        boxCacheDataVO.setTodayArisePriceTotal(todayArisePriceTotal);
        boxCacheDataVO.setTodayProfit(todayProfit);
        return boxCacheDataVO;
    }

    @Override
    public List<Long> getRealList(Integer boxId, int flag) {
        List<TtBoxOrnaments> boxOrnamentsDataList = new LambdaQueryChainWrapper<>(boxOrnamentsMapper)
                .eq(TtBoxOrnaments::getBoxId, boxId)
                .list();

        Map<Long, Integer> realDataParam = new HashMap<>();
        Map<Long, Integer> anchorDataParam = new HashMap<>();
        for (TtBoxOrnaments boxOrnaments : boxOrnamentsDataList) {
            Long ornamentsId = boxOrnaments.getOrnamentId();
            Integer realOdds = boxOrnaments.getRealOdds();
            realDataParam.put(ornamentsId, realOdds);
            Integer anchorOdds = boxOrnaments.getAnchorOdds();
            anchorDataParam.put(ornamentsId, anchorOdds);
        }
        if (flag == 0) return RandomUtils.toList(realDataParam);
        if (flag == 1) return RandomUtils.toList(anchorDataParam);
        return null;
    }

    // private void initializeStock(Integer boxId, int flag) {
    //     List<TtBoxOrnaments> boxOrnamentsDataList = new LambdaQueryChainWrapper<>(boxOrnamentsMapper).eq(TtBoxOrnaments::getBoxId, boxId).list();
    //     if (ObjectUtils.isEmpty(boxOrnamentsDataList)) return;
    //     List<Integer> realList = getRealList(boxId, 0);
    //     List<Integer> anchorList = getRealList(boxId, 1);
    //     if (flag == 1 && !realList.isEmpty()) {
    //         redisCache.setCacheList(RedisConstants.OPEN_BOX_ODDS + boxId + RedisConstants.REAL_ODDS_SUFFIX, realList);
    //     } else if (flag == 2 && !anchorList.isEmpty()) {
    //         redisCache.setCacheList(RedisConstants.OPEN_BOX_ODDS + boxId + RedisConstants.ANCHOR_ODDS_SUFFIX, anchorList);
    //     }
    // }
}
