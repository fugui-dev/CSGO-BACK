package com.ruoyi.playingmethod.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.mapper.TtBoxRecordsMapper;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.admin.service.TtOrnamentsLevelService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.domain.common.constant.TtboxRecordSource;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtOrnamentsLevel;
import com.ruoyi.domain.other.TtReplacementRecord;
import com.ruoyi.domain.vo.UserPackSackDataVO;
import com.ruoyi.playingmethod.mapper.ApiReplacementRecordMapper;
import com.ruoyi.playingmethod.service.IApiReplacementRecordService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.user.model.dto.SmeltRequest;
import com.ruoyi.user.model.vo.SmeltVO;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ruoyi.admin.config.RedisConstants.USER_PLAY_COMMON;

/**
 * 业务端汰换逻辑实现
 *
 * @author junhai
 */
@Service
public class ApiReplacementRecordServiceImpl implements IApiReplacementRecordService {

    @Autowired
    private ApiReplacementRecordMapper apiReplacementRecordMapper;

    @Autowired
    private TtBoxRecordsService boxRecordsService;

    @Autowired
    private TtBoxRecordsMapper ttBoxRecordsMapper;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private TtOrnamentMapper ttOrnamentMapper;

    @Autowired
    private TtOrnamentService ornamentService;

    @Autowired
    private TtUserService userService;

    @Autowired
    private TtOrnamentsLevelService ornamentsLevelService;

    @Autowired
    private RedisLock redisLock;

    // 概率配置数组
    private static final double[][] percent = {
            {0.5, 0.6},
            {0.6, 0.7},
            {0.7, 0.8},
            {0.8, 0.9},
            {0.9, 1},
            {1, 2}
    };


    @Override
    @Transactional
    public AjaxResult synthesizeItems(LoginUser user, List<Long> itemIds) {
        int size = 0;
        int size1 = 0;
        if (itemIds != null) {
            size = itemIds.size();
        }

        int allCount = size + size1;
        if (allCount < 3 || allCount > 9) {
            return AjaxResult.error("汰换数量在3~9个");
        }

        BigDecimal totalPrice = new BigDecimal(0);

        //查询饰品列表
        // 1.先查询宝箱记录里面的饰品
        if(!itemIds.isEmpty()){
            // 从我的背包中查询饰品总价值
            List<UserPackSackDataVO> boxs = apiReplacementRecordMapper
                    .selectUserPackSack(user.getUserId().intValue(), itemIds);
            for (int i = 0; i < boxs.size(); i++) {
                BigDecimal bigDecimal = boxs.get(i).getOrnamentsPrice();
                totalPrice = totalPrice.add(bigDecimal);
            }
        }
        //累加装备总价值
        String status = String.valueOf(TtboxRecordStatus.SMELT.getCode());
        //更新记录的状态 updateStatusByIds
        if(!itemIds.isEmpty()){
            ttBoxRecordsMapper.updateStatusByIds(itemIds, status);
        }

        //获取最小汰换金额
        BigDecimal minPrice = new BigDecimal(sysConfigService.selectConfigByKey("minExchangeAmount"));
        // 汰换饰品合计总价不得低于
        if (totalPrice.compareTo(minPrice) < 0) {
            return AjaxResult.error("汰换饰品合计总价不得低于" + minPrice);
        }

        //获取饰品的id
        Long id = getOId(totalPrice);
        //获取饰品详情
        TtOrnament ttOrnament = ttOrnamentMapper.selectOrnamentById(id);
        //进行汰换的日志记录
        TtReplacementRecord replacementRecord = new TtReplacementRecord();
        replacementRecord.setUid(user.getUserId());
        replacementRecord.setUname(user.getUserData().getNickName());
        replacementRecord.setAwardOid(ttOrnament.getId());
        replacementRecord.setAwardOname(ttOrnament.getName());
        replacementRecord.setAwardOimg(ttOrnament.getImageUrl());
        replacementRecord.setAwardOprice(ttOrnament.getUsePrice());
        replacementRecord.setTime(DateUtils.getNowDate());
        replacementRecord.setCreateTime(DateUtils.getNowDate());
        apiReplacementRecordMapper.insertTtReplacementRecord(replacementRecord);
        //将汰换的奖品加入背包
        TtBoxRecords ttBoxRecords = new TtBoxRecords();
        ttBoxRecords.setSource(TtboxRecordSource.REPLACEMENT.getCode());
        ttBoxRecords.setUserId(user.getUserId().intValue());
        ttBoxRecords.setHolderUserId(user.getUserId().intValue());
        ttBoxRecords.setBoxName("汰换奖品");
        ttBoxRecords.setOrnamentName(ttOrnament.getName());
        ttBoxRecords.setImageUrl(ttOrnament.getImageUrl());
        ttBoxRecords.setOrnamentId(ttOrnament.getId());
        ttBoxRecords.setOrnamentsPrice(ttOrnament.getUsePrice());
        ttBoxRecords.setStatus(0);
        ttBoxRecords.setCreateTime(DateUtils.getNowDate());
        boxRecordsService.save(ttBoxRecords);

        return AjaxResult.success(replacementRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<SmeltVO> smelt(SmeltRequest smeltRequest) {
        Integer userId = SecurityUtils.getUserId().intValue();
        TtUser user = userService.getById(userId);

        String lockKey = USER_PLAY_COMMON + "user_id:" + user.getUserId();
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("操作频繁，请重试！");
        }

        //1.价值匹配
        List<TtBoxRecords> userPackSelectList = boxRecordsService.listByIds(smeltRequest.getPackageIds());
        TtOrnament ornament = ornamentService.getOne(Wrappers.lambdaQuery(TtOrnament.class)
                .eq(TtOrnament::getId, smeltRequest.getTargetOrnament()), false);
//        Assert.isTrue(ornament != null && ornament.getIsPutaway().equals("1"), "目标饰品已下架！");


        //总价值
        BigDecimal totalSelectPrice = BigDecimal.ZERO;
        for (TtBoxRecords boxRecord : userPackSelectList) {
            Assert.isTrue(Objects.equals(boxRecord.getStatus(), TtboxRecordStatus.IN_PACKSACK_ON.getCode()), "请刷新背包重试！");
            Assert.isTrue(Objects.equals(boxRecord.getUserId(), userId), "所选饰品非自己饰品！");
            totalSelectPrice = totalSelectPrice.add(boxRecord.getOrnamentsPrice());
            boxRecord.setStatus(TtboxRecordStatus.SMELT.getCode());
        }

        //获取最小汰换金额
        BigDecimal minPrice = new BigDecimal(sysConfigService.selectConfigByKey("minExchangeAmount"));
        // 汰换饰品合计总价不得低于
        if (totalSelectPrice.compareTo(minPrice) < 0) {
            return R.fail("熔炼饰品合计总价不得低于" + minPrice);
        }

        //如果目标价值大于选中总价值，不可熔炼
        if (ornament.getUsePrice().compareTo(totalSelectPrice) > 0){
            return R.fail("价值不足，无法熔炼！");
        }

        //查询一个默认等级出来
        TtOrnamentsLevel oneLevel = ornamentsLevelService.getOne(Wrappers.lambdaQuery(TtOrnamentsLevel.class)
                .last("limit 1"));

        //2.分解投入饰品，追加新饰品
        //进行汰换的日志记录
        TtReplacementRecord replacementRecord = new TtReplacementRecord();
        replacementRecord.setUid(user.getUserId().longValue());
        replacementRecord.setUname(user.getNickName());
        replacementRecord.setAwardOid(ornament.getId());
        replacementRecord.setAwardOname(ornament.getName());
        replacementRecord.setAwardOimg(ornament.getImageUrl());
        replacementRecord.setAwardOprice(ornament.getUsePrice());
        replacementRecord.setTime(DateUtils.getNowDate());
        replacementRecord.setCreateTime(DateUtils.getNowDate());
        apiReplacementRecordMapper.insertTtReplacementRecord(replacementRecord);
        //将汰换的奖品加入背包
        TtBoxRecords boxRecord = new TtBoxRecords();
        boxRecord.setSource(TtboxRecordSource.REPLACEMENT.getCode());
        boxRecord.setUserId(user.getUserId());
        boxRecord.setHolderUserId(user.getUserId());
        boxRecord.setBoxName("汰换/熔炼奖品");
        boxRecord.setOrnamentName(ornament.getName());
        boxRecord.setOrnamentsPrice(ornament.getUsePrice());
        boxRecord.setImageUrl(ornament.getImageUrl());
        boxRecord.setOrnamentId(ornament.getId());
        boxRecord.setOrnamentsPrice(ornament.getUsePrice());
        boxRecord.setStatus(TtboxRecordStatus.IN_PACKSACK_ON.getCode());
        boxRecord.setCreateTime(DateUtils.getNowDate());

        boxRecord.setOrnamentsLevelId(oneLevel.getId());
        boxRecord.setOrnamentLevelImg(oneLevel.getLevelImg());

        boolean save = boxRecordsService.save(boxRecord);

        //更新之前的背包饰品（不建议放一起用save0rUpdate）
        boolean batchUpdate = boxRecordsService.updateBatchById(userPackSelectList);

        Assert.isTrue(save && batchUpdate, "系统繁忙请重试！");

        SmeltVO smeltVO = new SmeltVO();
        BeanUtils.copyProperties(boxRecord, smeltVO);

        return R.ok(smeltVO);
    }


    /**
     * 随机产生饰品id
     *
     * @param bean
     * @return
     */
    public Long getOId(BigDecimal bean) {
        List<TtOrnament> ornament = new ArrayList<>();
        initArray(bean, ornament);
        Random random = new Random();
        int randomIndex = random.nextInt(ornament.size());
        return ornament.get(randomIndex).getId();
    }

    /**
     * 初始化 饰品列表
     *
     * @param bean
     * @param ornaments
     */
    private void initArray(BigDecimal bean, List<TtOrnament> ornaments) {
        for (double[] range : percent) {
            BigDecimal start = bean.multiply(BigDecimal.valueOf(range[0]));
            BigDecimal end = bean.multiply(BigDecimal.valueOf(range[1]));

            Map<String, BigDecimal> map = new HashMap<>();
            map.put("start", start);
            map.put("end", end);

            int configQuantity = 1;

            if (configQuantity > 0) {
                List<TtOrnament> eligibleOrnaments = apiReplacementRecordMapper.findByPriceRange(map);
                shuffleList(eligibleOrnaments);
                int quantityToAdd = Math.min(configQuantity, eligibleOrnaments.size());

                for (int i = 0; i < quantityToAdd; i++) {
                    ornaments.add(eligibleOrnaments.get(i));
                }
            }
        }
    }

    /**
     * 采用了 Fisher-Yates 随机洗牌算法，
     *
     * @param list
     */
    private void shuffleList(List<TtOrnament> list) {
        Random random = new Random();
        int n = list.size();
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            TtOrnament temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }
}
