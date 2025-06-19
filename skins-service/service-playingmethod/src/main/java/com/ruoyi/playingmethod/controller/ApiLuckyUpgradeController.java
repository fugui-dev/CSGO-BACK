package com.ruoyi.playingmethod.controller;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.dto.upgrade.UpgradeCondition;
import com.ruoyi.domain.other.ApiLuckyUpgradeBody;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtUpgradeRecord;
import com.ruoyi.domain.other.UpgradeBodyA;
import com.ruoyi.domain.vo.ApiLuckyOrnamentsDataVO;
import com.ruoyi.playingmethod.service.ApiLuckyUpgradeService;
import com.ruoyi.playingmethod.service.ApiUpgradeRecordService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.user.service.ApiUserPackSackService;
import io.jsonwebtoken.lang.Assert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ruoyi.admin.config.RedisConstants.USER_PLAY_COMMON;

@Api(tags = "幸运升级模式")
@RestController
@RequestMapping("/api/luckyUpgrade")
public class ApiLuckyUpgradeController extends BaseController {

    @Autowired
    private ApiUserPackSackService userPackSackService;

    @Autowired
    private ApiUpgradeRecordService upgradeRecordService;

    private final TtUserService userService;
    private final ApiLuckyUpgradeService apiLuckyUpgradeService;
    private final ISysConfigService sysConfigService;

    @Autowired
    private RedisLock redisLock;

    public ApiLuckyUpgradeController(TtUserService userService,
                                     ApiLuckyUpgradeService apiLuckyUpgradeService,
                                     ISysConfigService sysConfigService) {
        this.userService = userService;
        this.apiLuckyUpgradeService = apiLuckyUpgradeService;
        this.sysConfigService = sysConfigService;
    }

    public R checkLogin(){
        Long userId;
        try {
            userId = getUserId();
            if (ObjectUtil.isEmpty(userId)) AjaxResult.error(401,"登录过期，请重新登录。");
            return R.ok(userId);
        }catch (Exception e){
            return R.fail(401,"登录过期，请重新登录。");
        }
    }

    @ApiOperation("获取可升级的饰品列表")
    @Anonymous
    @GetMapping("/getOrnamentsList")
    public TableDataInfo getOrnamentsList(ApiLuckyUpgradeBody apiLuckyUpgradeBody){
        //兼容处理分页
        if (apiLuckyUpgradeBody != null){
            if (apiLuckyUpgradeBody.getSize() != null && apiLuckyUpgradeBody.getPage() != null && apiLuckyUpgradeBody.getPage() > 0){
                PageUtils.startPage(apiLuckyUpgradeBody.getPage(), apiLuckyUpgradeBody.getSize());
            }
        }
        if (apiLuckyUpgradeBody != null && apiLuckyUpgradeBody.getPriceMin()!=null && apiLuckyUpgradeBody.getPriceMin().compareTo(BigDecimal.ZERO) == 0){
            apiLuckyUpgradeBody.setPriceMin(new BigDecimal("0.01"));
        }
        List<ApiLuckyOrnamentsDataVO> list = apiLuckyUpgradeService.getOrnamentsList(apiLuckyUpgradeBody);
        return getDataTable(list);
    }

    @ApiOperation(value = "幸运升级2", notes = "该玩法相对比幸运升级1，多了每次融入背包内饰品提升幸运度（游戏币+幸运度最高达到75）")
    @UpdateUserCache
    @PostMapping("/upgrade2")
    public R<TtUpgradeRecord> upgrade2(@RequestBody @Validated UpgradeBodyA upgradeParam){

        //1.计算背包价值+金币价值是否无误，校验余额是否充足
        //2.删除背包物品，扣除金币余额，扣除弹药余额
        //3.返回升级结果

        Assert.notNull(upgradeParam.getPackageOrnamentId(), "请选择要投入的背包饰品！");

        R check = checkLogin();
        if (!check.getCode().equals(200)) return R.fail(401,"登录过期，请重新登录。");
        Integer userId = ((Long)check.getData()).intValue();

        String lockKey = USER_PLAY_COMMON + "user_id:" + userId;
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        TtUser ttUser = userService.getById(userId);

        if (BigDecimal.ZERO.compareTo(ttUser.getTotalRecharge()) >= 0){
            return R.fail("为防止机器人恶意注册，请充值后开启玩法！");
        }

        R r = apiLuckyUpgradeService.upgrade2(ttUser, upgradeParam);

        redisLock.unlock(lockKey);

        return r;
    }

    @ApiOperation("幸运升级")
    @UpdateUserCache
    @PostMapping("/upgrade")
    public R upgrade(@RequestBody @Validated UpgradeBodyA upgradeParam){
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) return R.fail("网站维护中......");

        R check = checkLogin();
        if (!check.getCode().equals(200)) return R.fail(401,"登录过期，请重新登录。");
        Integer userId = ((Long)check.getData()).intValue();


        Boolean lock = redisLock.tryLock(USER_PLAY_COMMON + "user_id:" + userId, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        TtUser ttUser = userService.getById(userId);
        if (BigDecimal.ZERO.compareTo(ttUser.getTotalRecharge()) >= 0){
            return R.fail("为防止机器人恶意注册，请充值后开启玩法！");
        }
//        BigDecimal add = ttUser.getAccountAmount().add(ttUser.getAccountCredits());

        //仅校验金币扣款
//        if (add.compareTo(upgradeParam.getPrice()) <= 0) {
//            return R.fail("余额不足!");
//        }

        return apiLuckyUpgradeService.upgrade(ttUser, upgradeParam);
    }

    @ApiOperation("获取幸运升级历史记录")
    @PostMapping("/getUpgradeRecord")
    public R getUpgradeRecord(@RequestBody @Validated UpgradeCondition param){
        return upgradeRecordService.historyDetail(param);
    }
}
