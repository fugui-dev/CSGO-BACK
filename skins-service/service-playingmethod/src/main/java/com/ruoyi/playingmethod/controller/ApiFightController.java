package com.ruoyi.playingmethod.controller;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.admin.service.*;
import com.ruoyi.admin.util.core.fight.LotteryMachine;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.dto.fight.FightDetailParam;
import com.ruoyi.domain.dto.fight.FightOnMyOwnParam;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.*;
import com.ruoyi.domain.vo.ApiFightListDataVO;
import com.ruoyi.domain.vo.FightResultDataVO;
import com.ruoyi.domain.vo.TtBoxOrnamentsDataVO;
import com.ruoyi.domain.vo.fight.FightResultVO;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;
import com.ruoyi.playingmethod.model.vo.ApiFightRankingVO;
import com.ruoyi.playingmethod.model.vo.TtFightBoxOrnamentsDataVO;
import com.ruoyi.playingmethod.scheduled.RebootTask;
import com.ruoyi.playingmethod.service.ApiFightService;
import com.ruoyi.playingmethod.websocket.WsFightHall;
import com.ruoyi.playingmethod.websocket.constant.SMsgKey;
import com.ruoyi.playingmethod.websocket.util.WsResult;
import com.ruoyi.system.service.ISysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ruoyi.admin.config.RedisConstants.USER_PLAY_COMMON;

@Api(tags = "对战模式", hidden = true)
@Slf4j
@RestController
@RequestMapping("/api/fight")
public class ApiFightController extends BaseController {

    private final TtFightResultService fightResultService;
    private final ISysConfigService sysConfigService;
    private final TtUserService userService;
    private final ApiFightService apiFightService;
    private final RedisCache redisCache;

    @Autowired
    private TtBoxOrnamentsService boxOrnamentsService;

    @Autowired
    private TtOrnamentService ttOrnamentService;

    @Autowired
    private LotteryMachine lotteryMachine;

    @Autowired
    private TtBoxService boxService;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private RebootTask rebootTask;

    public ApiFightController(TtFightResultService fightResultService,
                              ISysConfigService sysConfigService,
                              TtUserService userService,
                              ApiFightService apiFightService,
                              RedisCache redisCache) {
        this.fightResultService = fightResultService;
        this.sysConfigService = sysConfigService;
        this.userService = userService;
        this.apiFightService = apiFightService;
        this.redisCache = redisCache;
    }

    public R checkLogin() {
        Long userId;
        try {
            userId = getUserId();
            if (ObjectUtil.isEmpty(userId)) return R.fail(401, "登录过期，请重新登录。");
            return R.ok(userId);
        } catch (Exception e) {
            return R.fail("登录过期，请重新登录。");
        }
    }

    /**
     * Step1 创建对战房间
     */
    @ApiOperation("创建对战房间")
    @UpdateUserCache
    @PostMapping("/createFight")
    public R createFight(@Validated @RequestBody CreateFightBody createFightParam) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String fightMaintenance = sysConfigService.selectConfigByKey("fightMaintenance");
        if ("1".equals(fightMaintenance)) {
            return R.fail("开箱对战功能正在维护中......");
        }
        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) {
            return checkLogin;
        }
        Integer userId = ((Long) checkLogin.getData()).intValue();
        TtUser player = userService.getById(userId);

        Boolean lock = redisLock.tryLock(USER_PLAY_COMMON + "user_id:" + userId, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        if (BigDecimal.ZERO.compareTo(player.getTotalRecharge()) >= 0){
            return R.fail("为防止机器人恶意注册，请充值后开启玩法！");
        }

        return apiFightService.createFight(createFightParam, player);
    }

    /**
     * Step2 加入房间（占座），先行HTTP
     */
    @ApiOperation("加入对战房间")
    @PostMapping("/joinFightRoom")
    public R joinFightRoom(@RequestParam("fightId") Integer fightId) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String fightMaintenance = sysConfigService.selectConfigByKey("fightMaintenance");
        if ("1".equals(fightMaintenance)) {
            return R.fail("开箱对战功能正在维护中......");
        }
        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;
        Integer userId = ((Long) checkLogin.getData()).intValue();

        Boolean lock = redisLock.tryLock(USER_PLAY_COMMON + "user_id:" + userId, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        TtUser player = userService.getById(userId);
        if (BigDecimal.ZERO.compareTo(player.getTotalRecharge()) >= 0){
            return R.fail("为防止机器人恶意注册，请充值后开启玩法！");
        }

        return apiFightService.joinFight(fightId, player);
    }

    /**
     * 退出房间
     */
    @ApiOperation("退出房间")
    @PostMapping("/fightRoomExit")
    public R fightRoomExit(@RequestParam("fightId") Integer fightId) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) return R.fail("网站维护中......");
        String fightMaintenance = sysConfigService.selectConfigByKey("fightMaintenance");
        if ("1".equals(fightMaintenance)) return R.fail("开箱对战功能正在维护中......");

        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;
        Integer userId = ((Long) checkLogin.getData()).intValue();

        TtUser player = userService.getById(userId);

        return apiFightService.fightRoomExit(fightId, player);
    }

    /**
     * Step3 玩家准备游戏
     */
    @ApiOperation("玩家准备游戏")
    @UpdateUserCache
    @PostMapping("/seatrReady")
    public R seatrReady(@RequestParam("fightId") Integer fightId) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String fightMaintenance = sysConfigService.selectConfigByKey("fightMaintenance");
        if ("1".equals(fightMaintenance)) {
            return R.fail("开箱对战功能正在维护中......");
        }

        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;
        Integer userId = ((Long) checkLogin.getData()).intValue();

        TtUser player = userService.getById(userId);

        return apiFightService.seatrReady(fightId, player);
    }

    /**
     * Step4 开始游戏（前端满人自动开始）
     */
    @ApiOperation("开始游戏")
    @PostMapping("/fightBegin")
    public R fightBegin(@RequestParam("fightId") Integer fightId) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String fightMaintenance = sysConfigService.selectConfigByKey("fightMaintenance");
        if ("1".equals(fightMaintenance)) {
            return R.fail("开箱对战功能正在维护中......");
        }

        Boolean lock = redisLock.tryLock(USER_PLAY_COMMON + "user_id:" + getUserId(), 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;
        Integer userId = ((Long) checkLogin.getData()).intValue();

        TtUser player = userService.getById(userId);

        return apiFightService.fightBegin(fightId, player);
    }


    /**
     * 邀请机器人加入房间
     * @param fightId
     * @return
     */
    @ApiOperation("邀请平台机器人加入对战房间")
    @PostMapping("/inviteJoinFightRoom")
    public R inviteJoinFightRoom(@RequestParam("fightId") Integer fightId) {
        TtFight fight = apiFightService.getById(fightId);
        rebootTask.rebootFightJoinFight(fight);
        return R.ok(null, "邀请成功！");
    }

    /**
     * 观战
     */
    @ApiOperation("观战")
    @PostMapping("/audience")
    public R audience(@RequestParam("fightId") Integer fightId) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String fightMaintenance = sysConfigService.selectConfigByKey("fightMaintenance");
        if ("1".equals(fightMaintenance)) {
            return R.fail("开箱对战功能正在维护中......");
        }

        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;

        return apiFightService.audience(fightId);
    }

    /**
     * 游戏结束标记
     */
    @ApiOperation("游戏结束标记")
    @PostMapping("/fightEnd")
    public R fightEnd(@RequestParam("fightId") Integer fightId) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) return R.fail("网站维护中......");
        String fightMaintenance = sysConfigService.selectConfigByKey("fightMaintenance");
        if ("1".equals(fightMaintenance)) return R.fail("开箱对战功能正在维护中......");

        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;

        return apiFightService.fightEnd(fightId);
    }

    /**
     * 抽奖机测试
     */
    // @ApiOperation("抽奖机测试")
    // @GetMapping("LotteryMachineTest")
    public R LotteryMachineTest() {
        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;
        Integer userId = ((Long) checkLogin.getData()).intValue();
        TtUser player = userService.getById(userId);

        TtBox box = boxService.getById(2L);
        String ornamentId = lotteryMachine.singleLottery(player, box);

        return R.ok(ornamentId);
    }

    @ApiOperation("我参与的历史对战记录")
    @PostMapping("/fightOnMyOwn")
    public R fightOnMyOwn(@RequestBody FightOnMyOwnParam param) {
        // List<Integer> illegalStatus = Arrays.asList();
        Long userId = getUserId();
        if (ObjectUtil.isNull(userId)) return R.fail(401,"登录过期。");

        param.setPlayerId(userId.intValue());

        List<ApiFightListDataVO> list = apiFightService.getFightList(param);
        return R.ok(list);
    }

    @ApiOperation("历史对战详情")
    @PostMapping("/fightDetail")
    public R fightDetail(@RequestBody FightDetailParam param) {
        return apiFightService.fightDetail(param);
    }

    @ApiOperation("获取指定记录更早的历史对战")
    @PostMapping("/earlierHistory")
    public R earlierHistory(@RequestBody FightDetailParam param) {
        return apiFightService.earlierHistory(param);
    }

    //--------------- old ---------------

    // @PostMapping("/joinFight")
    // @UpdateUserCache
    // public R<Object> joinFight(@RequestParam("fightId") Integer fightId,
    //                            @RequestParam("joinSeatNum") Integer joinSeatNum,
    //                            @RequestParam("rounds") Integer rounds) {
    //     String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
    //     if ("1".equals(websiteMaintenance)) return R.fail("网站维护中......");
    //     String fightMaintenance = sysConfigService.selectConfigByKey("fightMaintenance");
    //     if ("1".equals(fightMaintenance)) return R.fail("开箱对战功能正在维护中......");
    //
    //     Long userId;
    //     try {
    //         userId = getUserId();
    //     } catch (Exception e) {
    //         return R.fail(401, "登录过期，请重新登录。");
    //     }
    //     if (ObjectUtil.isEmpty(userId)) return R.fail(401, "登录过期，请重新登录。");
    //
    //     TtUser ttUser = userService.getById(userId);
    //     log.info("api:{} 参数fightId{}\n 参数joinSeatNum{}\n 参数rounds{}", fightId, joinSeatNum, rounds);
    //     return apiFightService.joinFight(fightId, ttUser);
    // }

    // @ApiOperation("获取宝箱类型")
    // @GetMapping("/getBoxType/{page}/{size}")
    // @Anonymous
    // public R getBoxType(@PathVariable("page") Integer page, @PathVariable("size") Integer size) {
    //
    //     return R.ok(null);
    // }

    @ApiOperation("获取对战宝箱详情")
    @GetMapping("/simpleBoxDetail")
    @Anonymous
    public R<List<TtFightBoxOrnamentsDataVO>> simpleBoxDetail(@RequestParam(value = "boxId") Integer boxId) {
        List<TtFightBoxOrnamentsDataVO> fightBoxDetail = apiFightService.getFightBoxDetail(boxId);
        if (ObjectUtil.isEmpty(fightBoxDetail) || fightBoxDetail.isEmpty()) {
            return R.ok(null, "没有匹配的数据。");
        }
        return R.ok(fightBoxDetail);
    }

    @ApiOperation("获取对战宝箱")
    @GetMapping("/getFightBoxList")
    @Anonymous
    public R<List<TtBoxVO>> getFightBoxList(@RequestParam(value = "boxTypeId", required = false) Integer boxTypeId) {
        List<TtBoxVO> boxData = apiFightService.getFightBoxList(boxTypeId);
        if (ObjectUtil.isEmpty(boxData) || boxData.isEmpty()) return R.ok(null, "没有匹配的数据。");
        return R.ok(boxData);
    }

    @ApiOperation("获取对战列表")
    @Anonymous
    @GetMapping("/getFightList")
    public PageDataInfo<ApiFightListDataVO> getFightList(@RequestParam(required = false) String model,
                                                         @RequestParam(required = false) String status) {
        startPage();
        List<ApiFightListDataVO> list = apiFightService.getFightList(model, status, null, null);
        return getPageData(list);
    }

    @ApiOperation("获取对战数据")
    @GetMapping("/getFightData/{fightId}")
    public R<ApiFightListDataVO> getFightData(@PathVariable("fightId") Integer fightId) {
        ApiFightListDataVO apiFightListDataVO = apiFightService.getFightList(null, null, null, fightId).get(0);
        return R.ok(apiFightListDataVO);
    }

    // @ApiOperation("获取对战结果")
    // @GetMapping("/getFightResult/{fightId}")
    // public R<FightResultDataVO> getFightResult(@PathVariable("fightId") Integer fightId) {
    //     FightResultDataVO fightResultDataVO = fightResultService.getFightResult(fightId);
    //     return R.ok(fightResultDataVO);
    // }

    @ApiOperation("获取对战结果")
    @GetMapping("/getFightResult/{fightId}")
    public R<FightResultVO> getFightResult(@PathVariable("fightId") Integer fightId) {
        FightResultVO fightResultVO = fightResultService.getFightResult(fightId);
        return R.ok(fightResultVO);
    }

    @ApiOperation("存储对战回合数据")
    @PostMapping("/saveFightBoutData")
    public R<Boolean> saveFightBoutData(@RequestBody FightBoutData fightBoutData) {
        return apiFightService.saveFightBoutData(fightBoutData);
    }

    @ApiOperation("获取对战回合数")
    @PostMapping("/getFightBoutNum/{fightId}")
    public R<Integer> getFightBoutNum(@PathVariable Integer fightId) {
        return apiFightService.getFightBoutNum(fightId);
    }

    /**
     * 获取当前回合开箱结果
     */
    // @GetMapping("/getFightRecord")
    public R<FightResultDataVO> getFightRecord(@RequestParam(value = "fightId", required = false) Integer fightId,
                                               @RequestParam(value = "round", required = false) Integer round,
                                               @RequestParam(value = "rounds", required = false) Integer rounds) {


        log.info("获取当前回合开箱结果 api /{} 参数fightId{} round{} rounds{}", "getFightRecord", fightId, round, rounds);

        // 检查是否已经开始游戏
        TtFight fight = apiFightService.getById(fightId);
        if (ObjectUtil.isEmpty(fight)) return R.fail("不存在的对局");
        if (fight.getStatus().equals(0)) return R.fail("对局尚未开始，请稍后重试。");

        FightResultDataVO fightResultDataVO = apiFightService.getFightRecord(fightId, round, rounds);
        if (ObjectUtil.isEmpty(fightResultDataVO)) {
            return R.fail("对局fightId 没有产生结果");
        }
        return R.ok(fightResultDataVO);
    }

    @ApiOperation("获取对战排行榜")
    @Anonymous
    @GetMapping("/fightRanking")
    public AjaxResult getFightBoutNum() {

        // 创建日期格式化对象，指定日期格式
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // 获取今天日期
        Date today = new Date();
        String formattedTodayDate = formatter.format(today);
        List<ApiFightRankingVO> todayFightRanking = apiFightService.getFightRankingByDate(formattedTodayDate);




        // 获取昨天日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();
        String formattedYesterdayDate = formatter.format(yesterday);
        List<ApiFightRankingVO> yesterdayFightRanking = apiFightService.getFightRankingByDate(formattedYesterdayDate);

        Map<String, Object> map = new HashMap<>();
        map.put("todayFightRanking", todayFightRanking);
        map.put("yesterdayFightRanking", yesterdayFightRanking);

        R checkLogin = checkLogin();
        if (checkLogin.getCode().equals(200)){

            Long userId = ((Long) checkLogin.getData());
            BigDecimal totalBoxPriceByToday = apiFightService.getTotalBoxPriceByDate(userId, formattedTodayDate);

            map.put("todayTotalBoxPrice", totalBoxPriceByToday);

            BigDecimal totalBoxPriceByDateYesterday = apiFightService.getTotalBoxPriceByDate(userId, formattedYesterdayDate);
            map.put("yesterdayTotalBoxPrice", totalBoxPriceByDateYesterday);
        }

        return AjaxResult.success(map);
    }
}
