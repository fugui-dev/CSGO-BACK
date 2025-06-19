package com.ruoyi.user.controller;

import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.user.service.ApiShoppingService;
import com.ruoyi.domain.other.ApiShoppingBody;
import com.ruoyi.domain.vo.ApiShoppingDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ruoyi.admin.config.RedisConstants.USER_PLAY_COMMON;

@Api(tags = "商城")
@RestController
@RequestMapping("/api/shopping")
public class ApiShoppingController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final ApiShoppingService apiShoppingService;
    private final TtUserService userService;

    @Autowired
    private RedisLock redisLock;

    public ApiShoppingController(ISysConfigService sysConfigService,
                                 ApiShoppingService apiShoppingService,
                                 TtUserService userService) {
        this.sysConfigService = sysConfigService;
        this.apiShoppingService = apiShoppingService;
        this.userService = userService;
    }

    @ApiOperation("获取不同类型的商品")
    @Anonymous
    @GetMapping("/getShoppingQuery")
    public R<List<Map<String, String>>> getShoppingQuery(@RequestParam("value") String value){
        List<SysDictData> dictCache = null;
        if ("0".equals(value)) dictCache = DictUtils.getDictCache("ornaments_type_name");
        if ("1".equals(value)) dictCache = DictUtils.getDictCache("ornaments_rarity_name");
        if ("2".equals(value)) dictCache = DictUtils.getDictCache("ornaments_exterior_name");
        if ("3".equals(value)) dictCache = DictUtils.getDictCache("ornaments_quality_name");
        if (StringUtils.isNull(dictCache)) return R.fail("数据异常！！");
        List<Map<String, String>> list = dictCache.stream().map(sysDictData -> {
            Map<String, String> map = new HashMap<>();
            map.put(sysDictData.getDictLabel(), sysDictData.getDictValue());
            return map;
        }).collect(Collectors.toList());
        return R.ok(list);
    }

    @ApiOperation("商品列表")
    @GetMapping("/list")
    @Anonymous
    public PageDataInfo<ApiShoppingDataVO> list(@Validated ApiShoppingBody shoppingBody) {
        List<ApiShoppingDataVO> list = apiShoppingService.list(shoppingBody);
        return getPageData(list);
    }

    @ApiOperation("商品兑换")
    @PostMapping("/exchange")
    @UpdateUserCache
    public R exchange(Long ornamentsId) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String shoppingMaintenance = sysConfigService.selectConfigByKey("shoppingMaintenance");
        if ("1".equals(shoppingMaintenance)) {
            return R.fail("商城功能正在维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());

        Boolean lock = redisLock.tryLock(USER_PLAY_COMMON + "user_id:" + ttUser.getUserId(), 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        return apiShoppingService.exchange(ttUser, ornamentsId);
        // return StringUtils.isEmpty(msg) ? R.ok("兑换成功！") : R.fail(msg);
    }

    @ApiOperation("弹药转换")
    @UpdateUserCache
    @PostMapping("/integratingConversion")
    public R<Boolean> integratingConversion(BigDecimal credits) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) return R.fail("网站维护中......");
        TtUser ttUser = userService.getById(getUserId());
        String msg = apiShoppingService.integratingConversion(ttUser, credits);
        return StringUtils.isEmpty(msg) ? R.ok(true, "弹药转换成功！") : R.fail(msg);
    }
}
