package com.ruoyi.user.controller;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.domain.dto.packSack.DecomposeLogCondition;
import com.ruoyi.domain.dto.packSack.DeliveryParam;
import com.ruoyi.domain.dto.packSack.PackSackCondition;
import com.ruoyi.domain.dto.packSack.DecomposeParam;
import com.ruoyi.domain.vo.TtBoxRecordsDataVO;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.vo.client.PackSackGlobalData;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.user.model.dto.SmeltRequest;
import com.ruoyi.user.model.vo.SmeltVO;
import com.ruoyi.user.service.ApiUserPackSackService;
import com.ruoyi.domain.vo.UserPackSackDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.ruoyi.admin.config.RedisConstants.USER_PLAY_COMMON;

@Api(tags = "玩家背包")
@RestController
@RequestMapping("/api/userPackSack")
public class ApiUserPackSackController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final ApiUserPackSackService userPackSackService;
    private final TtUserService userService;

    @Autowired
    private RedisLock redisLock;

    public ApiUserPackSackController(ISysConfigService sysConfigService,
                                     ApiUserPackSackService userPackSackService,
                                     TtUserService userService) {
        this.sysConfigService = sysConfigService;
        this.userPackSackService = userPackSackService;
        this.userService = userService;
    }

    @ApiOperation("用户申请提货")
    @PostMapping("/delivery")
    public R delivery(@RequestBody @Validated DeliveryParam param) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }

        Boolean lock = redisLock.tryLock(USER_PLAY_COMMON + "user_id:" + getUserId(), 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        // 获取用户信息
        TtUser ttUser = userService.getById(getUserId());
        if ("1".equals(ttUser.getDeliveryStatus())) {
            return R.fail(false, "您的帐号提货功能已被禁用，请联系管理员！");
        }
        if (ttUser.getUserType().equals("02")){
            if (ttUser.getIsRealCheck().equals("0")) {
                return R.fail("请先进行实名认证！");
            }
            if (BigDecimal.ZERO.compareTo(ttUser.getTotalRecharge()) >= 0){
                return R.fail("为防止机器人恶意注册，请充值后提货！");
            }
        }

        return userPackSackService.delivery(param, ttUser);
    }

    @ApiOperation("分解饰品")
    @UpdateUserCache
    @PostMapping("/decompose")
    public R<Object> decompose(@RequestBody @Validated DecomposeParam param) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        if (!param.getIsAll() && ObjectUtils.isEmpty(param.getPackSackIds())) {
            return R.fail("请传入待分解饰品ID");
        }

        Boolean lock = redisLock.tryLock(USER_PLAY_COMMON + "user_id:" + getUserId(), 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        TtUser ttUser = userService.getById(getUserId());
        int result = userPackSackService.decompose(param, ttUser);
        redisLock.unlock(USER_PLAY_COMMON + "user_id:" + getUserId());
        return R.ok("成功分解" + result + "件饰品");
    }

    @ApiOperation("饰品分解记录")
    //@UpdateUserCache
    @PostMapping("/decomposeLog")
    public PageDataInfo<TtBoxRecordsDataVO> decomposeLog(@RequestBody @Validated DecomposeLogCondition param) {
        param.setUserId(null);
        param.setUserId(getUserId().intValue());
        // TtUser ttUser = userService.getById(getUserId());
        List<TtBoxRecordsDataVO> ttBoxRecords = userPackSackService.decomposeLog(param);
        return getPageData(ttBoxRecords);
    }

    @ApiOperation("玩家背包")
    @PostMapping("/getPackSack")
    public R<List<UserPackSackDataVO>> getPackSack(@RequestBody @Validated PackSackCondition condition) {
        Long userId = getUserId();
        if (ObjectUtil.isNull(userId)) {
            return R.fail("登录过期。");
        }
        if (!Objects.isNull(condition.getOrderByType())) {
            if (Objects.isNull(condition.getOrderByFie())) {
                return R.fail("排序类型不能为空");
            }
        }
        condition.setUidList(Arrays.asList(userId.intValue()));
        condition.setStatusList(Arrays.asList(0));
        return userPackSackService.clientPackSack(condition);
    }

    @ApiOperation("玩家背包-已申请提取的")
    @PostMapping("/extractOrn")
    public R<List<UserPackSackDataVO>> extractOrn(@RequestBody @Validated PackSackCondition condition) {
        Long userId = getUserId();
        if (ObjectUtil.isNull(userId)) {
            return R.fail("登录过期。");
        }
        if (!Objects.isNull(condition.getOrderByType())) {
            if (Objects.isNull(condition.getOrderByFie())) {
                return R.fail("排序类型不能为空");
            }
        }
        condition.setUidList(Arrays.asList(userId.intValue()));
        condition.setStatusList(Arrays.asList(2));
        return userPackSackService.clientPackSack(condition);
    }

    @ApiOperation("玩家背包统计数据")
    @GetMapping("/packSackGlobalData")
    public R<PackSackGlobalData> packSackGlobalData() {
        Long userId = getUserId();
        if (ObjectUtil.isNull(userId)) {
            return R.fail("登录过期。");
        }
        return userPackSackService.packSackGlobalData(userId.intValue());
    }
}
