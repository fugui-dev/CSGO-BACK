package com.ruoyi.playingmethod.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.service.*;
import com.ruoyi.admin.util.RandomUtils;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.domain.common.constant.*;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBonus;
import com.ruoyi.domain.other.TtBonusReceiveRecord;
import com.ruoyi.playingmethod.entity.vo.GetActiveVO;
import com.ruoyi.playingmethod.utils.DateScopeUtils;
import io.jsonwebtoken.lang.Assert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ruoyi.admin.config.RedisConstants.USER_PLAY_COMMON;

@RestController
@RequestMapping("/api/realnameGift")
@Api("实名福利")
public class ApiRealNameGiftController extends BaseController {

    @Autowired
    private TtUserBlendErcashService userBlendErcashService;

    @Autowired
    TtBoxRecordsService boxRecordsService;

    @Autowired
    TtOrnamentService ornamentService;

    @Autowired
    private TtUserService userService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisLock redisLock;

    private Boolean closeRealNameFlag = true;

    private final BigDecimal gift1 = new BigDecimal("2.00");

    private final BigDecimal gift2 = new BigDecimal("10.00");

    private final Long gift3 = 1896399662L; //奖励饰品ID

    @ApiOperation("实名福利列表")
    @GetMapping("/list")
    public R<List<GetActiveVO>> list(){
        //获取配置
//        String realNameGift1Key1 = "realNameGift1Key1";
//        String realNameGift1Key2 = "realNameGift1Key2";
//        String realNameGift1Key3 = "realNameGift1Key3";
//        BigDecimal gift1 = redisCache.getCacheObject(realNameGift1Key1);
//        BigDecimal gift2 = redisCache.getCacheObject(realNameGift1Key2);
//        Long gift3 = redisCache.getCacheObject(realNameGift1Key3);
        TtOrnament ttOrnament = ornamentService.getOne(Wrappers.lambdaQuery(TtOrnament.class)
                .eq(TtOrnament::getId, gift3));

        GetActiveVO one = new GetActiveVO("新人实名金币奖励", gift1, 0, null);
        GetActiveVO two = new GetActiveVO("新人实名弹药奖励", gift2, 0, null);
        GetActiveVO three = new GetActiveVO("新人实名饰品奖励", BigDecimal.ZERO, 0, ttOrnament);

        Long userId = getUserId();

        //查询已经领取
        List<TtUserBlendErcash> list = userBlendErcashService.list(Wrappers.lambdaQuery(TtUserBlendErcash.class)
                .eq(TtUserBlendErcash::getUserId, userId)
                .eq(TtUserBlendErcash::getType, TtAccountRecordType.INPUT.getCode())
                .in(TtUserBlendErcash::getSource, TtAccountRecordSource.REAL_NAME_GIFT_MONEY1.getCode(), TtAccountRecordSource.REAL_NAME_GIFT_MONEY2.getCode()));
        for (TtUserBlendErcash ercash : list) {
            if (ercash.getSource().equals(TtAccountRecordSource.REAL_NAME_GIFT_MONEY1.getCode())){
                one.setGetStatus(1);
                one.setAwardMoney(ercash.getAmount());
            }
            if (ercash.getSource().equals(TtAccountRecordSource.REAL_NAME_GIFT_MONEY2.getCode())){
                two.setGetStatus(1);
                two.setAwardMoney(ercash.getCredits());
            }
        }


        //查询已经领取过饰品
        List<TtBoxRecords> boxRecordsList = boxRecordsService.list(Wrappers.lambdaQuery(TtBoxRecords.class)
                .eq(TtBoxRecords::getUserId, userId)
                .eq(TtBoxRecords::getSource, TtboxRecordSource.REAL_NAME_GIFT.getCode()));
        if (!boxRecordsList.isEmpty()){
            three.setGetStatus(1);

            Long ornamentId = boxRecordsList.get(0).getOrnamentId();
            TtOrnament ornament = ornamentService.getById(ornamentId);
            three.setOrnament(ornament);
        }


        List<GetActiveVO> returnList = new ArrayList<>();
        returnList.add(one);
        returnList.add(two);
        returnList.add(three);
        return R.ok(returnList);
    }

    @ApiOperation("实名福利福利1")
    @GetMapping("/gift1")
    public R<Map> gift1(){

        if (closeRealNameFlag){
            return R.fail("活动已下线！");
        }

        Long userId = getUserId();

        String lockKey = USER_PLAY_COMMON + "user_id:" + userId;
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        TtUser user = userService.getById(userId);

        Assert.isTrue("1".equals(user.getIsRealCheck()), "您还未实名！");

        //查询已经领取
        List<TtUserBlendErcash> list = userBlendErcashService.list(Wrappers.lambdaQuery(TtUserBlendErcash.class)
                .eq(TtUserBlendErcash::getUserId, userId)
                .eq(TtUserBlendErcash::getType, TtAccountRecordType.INPUT.getCode())
                .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.REAL_NAME_GIFT_MONEY1.getCode()));

        if (!list.isEmpty()){
            return R.fail("该奖励已领取！");
        }

        //写入记录
        BigDecimal awardMoney = gift1;
        TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                .userId(userId.intValue())
                .amount(awardMoney.compareTo(BigDecimal.ZERO) > 0 ? awardMoney : null)
                .finalAmount(awardMoney.compareTo(BigDecimal.ZERO) > 0 ? user.getAccountAmount().add(awardMoney) : null)
                .total(awardMoney)
                .type(TtAccountRecordType.INPUT.getCode())
                .source(TtAccountRecordSource.REAL_NAME_GIFT_MONEY1.getCode())
                .remark(TtAccountRecordSource.REAL_NAME_GIFT_MONEY1.getMsg())
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        boolean save = userBlendErcashService.save(blendErcash);

        //加钱
        user.setAccountAmount(user.getAccountAmount().add(awardMoney));
        userService.updateById(user);

        return R.ok();
    }

    @ApiOperation("实名福利福利2")
    @GetMapping("/gift2")
    public R<Map> gift2(){

        if (closeRealNameFlag){
            return R.fail("活动已下线！");
        }

        Long userId = getUserId();

        String lockKey = USER_PLAY_COMMON + "user_id:" + userId;
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        TtUser user = userService.getById(userId);

        Assert.isTrue("1".equals(user.getIsRealCheck()), "您还未实名！");

        //查询已经领取
        List<TtUserBlendErcash> list = userBlendErcashService.list(Wrappers.lambdaQuery(TtUserBlendErcash.class)
                .eq(TtUserBlendErcash::getUserId, userId)
                .eq(TtUserBlendErcash::getType, TtAccountRecordType.INPUT.getCode())
                .eq(TtUserBlendErcash::getSource, TtAccountRecordSource.REAL_NAME_GIFT_MONEY2.getCode()));

        if (!list.isEmpty()){
            return R.fail("该奖励已领取！");
        }

        //写入记录
        BigDecimal awardMoney = gift2;
        TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                .userId(userId.intValue())
                .credits(awardMoney.compareTo(BigDecimal.ZERO) > 0 ? awardMoney : null)
                .finalCredits(awardMoney.compareTo(BigDecimal.ZERO) > 0 ? user.getAccountCredits().add(awardMoney) : null)
                .total(awardMoney)
                .type(TtAccountRecordType.INPUT.getCode())
                .source(TtAccountRecordSource.REAL_NAME_GIFT_MONEY2.getCode())
                .remark(TtAccountRecordSource.REAL_NAME_GIFT_MONEY2.getMsg())
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        boolean save = userBlendErcashService.save(blendErcash);

        //加弹药
        user.setAccountCredits(user.getAccountCredits().add(awardMoney));
        userService.updateById(user);

        return R.ok();
    }

    @ApiOperation("实名福利福利3")
    @GetMapping("/gift3")
    public R gift3(){

        if (closeRealNameFlag){
            return R.fail("活动已下线！");
        }

        //查询背包是否存在？
        Long userId = getUserId();

        String lockKey = USER_PLAY_COMMON + "user_id:" + userId;
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        TtUser user = userService.getById(userId);
        Assert.isTrue("1".equals(user.getIsRealCheck()), "您还未实名！");

        List<TtBoxRecords> list = boxRecordsService.list(Wrappers.lambdaQuery(TtBoxRecords.class)
                .eq(TtBoxRecords::getUserId, userId)
//                .eq(TtBoxRecords::getOrnamentId, gift3)
                .eq(TtBoxRecords::getSource, TtboxRecordSource.REAL_NAME_GIFT.getCode())
                .last("limit 1"));

        if (!list.isEmpty()){
            return R.fail("已领取过该奖励！");
        }

        //领取奖励
        TtOrnament ornament = ornamentService.getOne(Wrappers.lambdaQuery(TtOrnament.class)
                .eq(TtOrnament::getId, gift3));
        if (ornament == null){
            return R.fail("不存在的饰品！");
        }

        TtBoxRecords boxrecord = new TtBoxRecords();
        boxrecord.setUserId(userId.intValue());
        boxrecord.setOrnamentId(gift3);
        boxrecord.setMarketHashName(ornament.getMarketHashName());
        boxrecord.setOrnamentName(ornament.getName());
        boxrecord.setOrnamentsPrice(ornament.getUsePrice());
        boxrecord.setImageUrl(ornament.getImageUrl());
        boxrecord.setStatus(TtboxRecordStatus.IN_PACKSACK_ON.getCode());
        boxrecord.setCreateTime(new Date());
        boxrecord.setSource(TtboxRecordSource.REAL_NAME_GIFT.getCode());
        boxrecord.setHolderUserId(userId.intValue());
        boxrecord.setIsOpenBox2Gift(false);
        boolean save = boxRecordsService.save(boxrecord);

        return R.ok();
    }


}