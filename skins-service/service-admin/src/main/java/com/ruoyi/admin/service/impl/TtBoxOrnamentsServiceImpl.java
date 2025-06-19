package com.ruoyi.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.controller.TtBoxOrnamentsController.batchAddParam;
import com.ruoyi.admin.mapper.TtBoxMapper;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.admin.util.core.fight.LotteryMachine;
import com.ruoyi.admin.util.core.fight.PrizePool;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.other.TtBoxA;
import com.ruoyi.domain.other.TtBoxOrnaments;
import com.ruoyi.admin.mapper.TtBoxOrnamentsMapper;
import com.ruoyi.admin.service.TtBoxOrnamentsService;
import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.domain.vo.TtBoxDataVO;
import com.ruoyi.domain.vo.TtBoxOrnamentsDataVO;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.domain.vo.box.BoxGlobalData;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TtBoxOrnamentsServiceImpl extends ServiceImpl<TtBoxOrnamentsMapper, TtBoxOrnaments> implements TtBoxOrnamentsService {

    @Autowired
    private TtBoxService boxService;

    @Autowired
    private TtBoxMapper boxMapper;

    @Autowired
    private TtOrnamentMapper ornamentMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private LotteryMachine lotteryMachine;

    @Override
    public List<TtBoxOrnamentsDataVO> selectTtBoxOrnamentsList(Integer boxId) {
        // 宝箱内物品明细
        return baseMapper.selectTtBoxOrnamentsList(boxId);
    }

    @Override
    public String saveBoxOrnaments(TtBoxOrnaments ttBoxOrnaments) {
        boolean save = this.save(ttBoxOrnaments);
        boxService.delCache(ttBoxOrnaments.getBoxId());
        boxService.isReplenishment(ttBoxOrnaments.getBoxId());

        Assert.isTrue(save, "添加库存失败！");
        updatePrizePool(ttBoxOrnaments.getBoxId());
        return "";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateBoxOrnamentsById(TtBoxOrnamentsDataVO ttBoxOrnamentsDataVO) {

        Integer boxId = ttBoxOrnamentsDataVO.getBoxId(),
                boxOrnamentsId = ttBoxOrnamentsDataVO.getId();
        Long ornamentsId = ttBoxOrnamentsDataVO.getOrnamentId();
        TtBoxOrnaments ttBoxOrnaments = TtBoxOrnaments.builder()
                .id(boxOrnamentsId)
                .boxId(boxId)
                .ornamentId(ornamentsId)
                .build();
        BeanUtils.copyBeanProp(ttBoxOrnaments, ttBoxOrnamentsDataVO);
        ttBoxOrnaments.setLevel(ttBoxOrnamentsDataVO.getOrnamentsLevelId());
        ttBoxOrnaments.setUpdateBy(SecurityUtils.getUsername());
        ttBoxOrnaments.setUpdateTime(DateUtils.getNowDate());
        boolean update = this.updateById(ttBoxOrnaments);
        Assert.isTrue(update, "更新失败！");

        // todo 旧的开箱机制，全部更新以后可以删除
        // boxService.delCache(boxId);
        // boxService.isReplenishment(boxId);

        // 更新redis奖池
        updatePrizePool(boxId);

        return "";
    }

    private void updatePrizePool(Integer boxId) {

        log.info("开始更新宝箱【{}】爆率", boxId);

        LambdaQueryWrapper<TtBoxOrnaments> ttBoxOrnamentsQuery = new LambdaQueryWrapper<>();
        ttBoxOrnamentsQuery.eq(TtBoxOrnaments::getBoxId, boxId);
        List<TtBoxOrnaments> list = this.list(ttBoxOrnamentsQuery);

        String anchorOddsKey = "prize_pool:" + boxId + ":01";
        String realOddsKey = "prize_pool:" + boxId + ":02";
        HashMap<String, Integer> realOddsSpace = new HashMap<>();
        HashMap<String, Integer> anchorOddsSpace = new HashMap<>();
        Integer realGoodsNumber = 0;
        Integer anchorGoodsNumber = 0;

        for (TtBoxOrnaments item : list) {
            realOddsSpace.put(String.valueOf(item.getOrnamentId()), item.getRealOdds());
            anchorOddsSpace.put(String.valueOf(item.getOrnamentId()), item.getAnchorOdds());
            realGoodsNumber = realGoodsNumber + item.getRealOdds();
            anchorGoodsNumber = anchorGoodsNumber + item.getAnchorOdds();
        }

        //同步保存缓存池
        PrizePool anchorPrizePool = PrizePool.builder()
                .key("prize_pool:" + boxId + ":01")
                .boxId(String.valueOf(boxId))
                .playerType("01")
                .goodsNumber(anchorGoodsNumber)
                .boxSpace(anchorOddsSpace)
                .build();

        PrizePool realPrizePool = PrizePool.builder()
                .key("prize_pool:" + boxId + ":02")
                .boxId(String.valueOf(boxId))
                .playerType("02")
                .goodsNumber(realGoodsNumber)
                .boxSpace(realOddsSpace)
                .build();

        lotteryMachine.getPrizePools().put(anchorPrizePool.getKey(), anchorPrizePool);
        lotteryMachine.getPrizePools().put(realPrizePool.getKey(), realPrizePool);

        redisCache.setCacheMap(realOddsKey, realOddsSpace, 600, TimeUnit.SECONDS);
        redisCache.setCacheMap(anchorOddsKey, anchorOddsSpace, 600, TimeUnit.SECONDS);

        log.info("更新宝箱【{}】爆率成功", boxId);
        log.info("更新宝箱【{}】爆率成功", boxId);

    }

    @Override
    public String removeBoxOrnamentsByIds(Integer boxId, List<Long> list) {
        this.removeByIds(list);
        boxService.delCache(boxId);
        boxService.isReplenishment(boxId);
        return "";
    }

    @Override
    public AjaxResult getProfitMargin(Integer boxId) {
        TtBox ttBox = boxService.getById(boxId);
        List<TtBoxOrnamentsDataVO> list = this.selectTtBoxOrnamentsList(boxId);
        int oddsTotalNum = list.stream().map(TtBoxOrnamentsDataVO::getOdds).mapToInt(Integer::intValue).sum();
        BigDecimal oddsAmountConsumed = ttBox.getPrice().multiply(BigDecimal.valueOf(oddsTotalNum));

        BigDecimal oddsAggregateAmount = list.stream()
                .filter(i -> i.getUsePrice() != null && i.getOdds() != null)
                .map(ttBoxOrnamentsDataVO -> ttBoxOrnamentsDataVO.getUsePrice()
                .multiply(BigDecimal.valueOf(ttBoxOrnamentsDataVO.getOdds()))).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal oddsTotalProfit = oddsAmountConsumed.subtract(oddsAggregateAmount);
        BigDecimal oddsTotalProfitMargin = getTotalProfitMargin(oddsTotalProfit, oddsAmountConsumed);
        int realOddsTotalNum = list.stream().map(TtBoxOrnamentsDataVO::getRealOdds).mapToInt(Integer::intValue).sum();
        BigDecimal realOddsAmountConsumed = ttBox.getPrice().multiply(BigDecimal.valueOf(realOddsTotalNum));
        BigDecimal realOddsAggregateAmount = list.stream().map(ttBoxOrnamentsDataVO -> ttBoxOrnamentsDataVO.getUsePrice()
                .multiply(BigDecimal.valueOf(ttBoxOrnamentsDataVO.getRealOdds())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal realOddsTotalProfit = realOddsAmountConsumed.subtract(realOddsAggregateAmount);
        BigDecimal realOddsTotalProfitMargin = getTotalProfitMargin(realOddsTotalProfit, realOddsAmountConsumed);
        int anchorOddsTotalNum = list.stream().map(TtBoxOrnamentsDataVO::getAnchorOdds).mapToInt(Integer::intValue).sum();
        BigDecimal anchorOddsAmountConsumed = ttBox.getPrice().multiply(BigDecimal.valueOf(anchorOddsTotalNum));
        BigDecimal anchorOddsAggregateAmount = list.stream().map(ttBoxOrnamentsDataVO -> ttBoxOrnamentsDataVO.getUsePrice()
                .multiply(BigDecimal.valueOf(ttBoxOrnamentsDataVO.getAnchorOdds()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal anchorOddsTotalProfit = anchorOddsAmountConsumed.subtract(anchorOddsAggregateAmount);
        BigDecimal anchorOddsTotalProfitMargin = getTotalProfitMargin(anchorOddsTotalProfit, anchorOddsAmountConsumed);
        AjaxResult success = AjaxResult.success();
        success.put("oddsTotalNum", oddsTotalNum);
        success.put("oddsTotalProfit", oddsTotalProfit);
        success.put("oddsTotalProfitMargin", oddsTotalProfitMargin + "%");
        success.put("realOddsTotalNum", realOddsTotalNum);
        success.put("realOddsTotalProfit", realOddsTotalProfit);
        success.put("realOddsTotalProfitMargin", realOddsTotalProfitMargin + "%");
        success.put("anchorOddsTotalNum", anchorOddsTotalNum);
        success.put("anchorOddsTotalProfit", anchorOddsTotalProfit);
        success.put("anchorOddsTotalProfitMargin", anchorOddsTotalProfitMargin + "%");
        return success;
    }

    @Override
    public String batchAdd(Integer boxId, List<Long> ornamentsIds) {
        List<Long> tempList = new ArrayList<>(ornamentsIds);
        List<TtBoxOrnaments> ttBoxOrnamentsList = new ArrayList<>();
        for (Long ornamentsId : tempList) {
            TtBoxOrnaments ttBoxOrnaments = TtBoxOrnaments.builder().build();
            ttBoxOrnaments.setBoxId(boxId);
            ttBoxOrnaments.setOrnamentId(ornamentsId);
            ttBoxOrnaments.setCreateBy(SecurityUtils.getUsername());
            ttBoxOrnaments.setCreateTime(DateUtils.getNowDate());
            ttBoxOrnamentsList.add(ttBoxOrnaments);
        }
        this.saveBatch(ttBoxOrnamentsList, 1);
        boxService.delCache(boxId);
        boxService.isReplenishment(boxId);
        return "";
    }

    @Override
    public AjaxResult batchAdd(batchAddParam param) {

        // List<Integer> tempList = param.getOrnamentsIds();
        List<Long> tempList = param.getOrnamentIds();
        List<TtBoxOrnaments> ttBoxOrnamentsList = new ArrayList<>();

        for (Long ornamentId : tempList) {
            TtBoxOrnaments boxOrn = TtBoxOrnaments.builder()
                    .boxId(param.getBoxId())
                    .ornamentId(ornamentId)
                    // .marketHashName((String) temp.get("market_hash_name"))
                    // .ornamentsZbtId((String) temp.get("ornaments_zbt_id"))
                    // .ornamentsYyId((String) temp.get("ornaments_yy_id"))
                    .createBy(SecurityUtils.getUsername())
                    .createTime(DateUtils.getNowDate())
                    .build();

            ttBoxOrnamentsList.add(boxOrn);
        }

        this.saveBatch(ttBoxOrnamentsList);

        // TODO: 2024/3/29 更新抽奖机奖品空间

        return AjaxResult.success("批量填货成功，请手动修改饰品数量！");
    }

    @Override
    public List<SimpleOrnamentVO> simpleBoxDetail(Integer boxId) {
        return baseMapper.simpleBoxDetail(boxId);
    }

    @Override
    public R globalData(Integer boxId) {

        //检查，空箱子直接返回
        List<TtBoxOrnaments> list = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(TtBoxOrnaments::getBoxId, boxId)
                .list();
        if (ObjectUtil.isEmpty(list) || list.isEmpty()) return R.ok(new BoxGlobalData());

        // 宝箱统计
        BoxGlobalData globalData = boxMapper.globalData(boxId);

        // 计算利润率
        // 通用
        BigDecimal commonAmountConsumed = globalData.getCommonAmountConsumed();
        if (commonAmountConsumed.compareTo(BigDecimal.ZERO)==0){
            globalData.setCommonProfit(null);
            globalData.setCommonProfitMargin(null);
        }else {
            BigDecimal commonAggregateAmount = globalData.getCommonAggregateAmount();
            BigDecimal commonProfit = commonAmountConsumed.subtract(commonAggregateAmount);
            BigDecimal commonProfitMargin = commonProfit.divide(commonAmountConsumed, 4, RoundingMode.HALF_UP);

            globalData.setCommonProfit(commonProfit);
            globalData.setCommonProfitMargin(commonProfitMargin);
        }

        // 主播
        BigDecimal anchorAmountConsumed = globalData.getAnchorAmountConsumed();
        if (anchorAmountConsumed.compareTo(BigDecimal.ZERO)==0){
            globalData.setAnchorProfit(null);
            globalData.setAnchorProfitMargin(null);
        }else {
            BigDecimal anchorAggregateAmount = globalData.getAnchorAggregateAmount();
            BigDecimal anchorProfit = anchorAmountConsumed.subtract(anchorAggregateAmount);
            BigDecimal anchorProfitMargin = anchorProfit.divide(anchorAmountConsumed, 4, RoundingMode.HALF_UP);

            globalData.setAnchorProfit(anchorProfit);
            globalData.setAnchorProfitMargin(anchorProfitMargin);
        }

        return R.ok(globalData);
    }

    private BigDecimal getTotalProfitMargin(BigDecimal totalProfit, BigDecimal amountConsumed) {
        if (BigDecimal.ZERO.compareTo(amountConsumed) == 0) return BigDecimal.ZERO;
        return totalProfit.divide(amountConsumed, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }
}
