package com.ruoyi.playingmethod.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.service.TtRollUserService;
import com.ruoyi.common.rabbitmq.config.DelayedQueueConfig;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.domain.dto.roll.GetRollOpenPrizeParam;
import com.ruoyi.domain.dto.roll.GetRollPlayersParam;
import com.ruoyi.domain.dto.roll.GetRollPrizePool;
import com.ruoyi.domain.entity.roll.TtRollUser;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import com.ruoyi.domain.vo.roll.RollJackpotOrnamentsVO;
import com.ruoyi.domain.vo.roll.RollUserVO;
import com.ruoyi.playingmethod.service.ApiRollService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.domain.vo.RollDetailsDataVO;
import com.ruoyi.domain.vo.RollListDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ruoyi.admin.config.RedisConstants.USER_PLAY_COMMON;

@Api(tags = "ROLL房模式")
@RestController
@RequestMapping("/api/roll")
public class ApiRollController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final TtUserService userService;
    private final ApiRollService apiRollService;

    @Autowired
    private TtRollUserService rollUserService;

    @Autowired
    private RedisLock redisLock;

    public ApiRollController(ISysConfigService sysConfigService,
                             TtUserService userService,
                             ApiRollService apiRollService) {
        this.sysConfigService = sysConfigService;
        this.userService = userService;
        this.apiRollService = apiRollService;
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class JoinRollParam {
        @NotNull(message = "roll房id不能为空。")
        private Integer rollId;
        private String rollPassword;
    }

    @ApiOperation("加入ROLL房")
    @RepeatSubmit(message = "操作过于频繁，请稍后重试！", interval = 3000)
    @PostMapping("/joinRoll")
    public R joinRoll(@RequestBody @Validated JoinRollParam param) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) return R.fail("网站维护中......");
        String rollMaintenance = sysConfigService.selectConfigByKey("rollMaintenance");
        if ("1".equals(rollMaintenance)) return R.fail("Roll房功能正在维护中......");

        Long userId = getUserId();
        if (ObjectUtil.isEmpty(userId)) return R.fail(401, "登录过期，请重新登录。");
        TtUser player = userService.getById(userId);
        if (ObjectUtil.isEmpty(player)) return R.fail("异常的用户状态。");

        Boolean lock = redisLock.tryLock(USER_PLAY_COMMON + "user_id:" + userId, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("访问频繁，请重试！");
        }

        return apiRollService.joinRoll(param, player);
        // return StringUtils.isEmpty(msg) ? R.ok("成功加入Roll房！") : R.fail(msg, "加入失败！");
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class GetRollListParam {

        private String rollName;

        // 0官方 1主播
        @ApiModelProperty("0官方 1主播")
        private Integer rollType;

        // 0未开奖 1已开奖
        @ApiModelProperty("0未开奖 1已开奖")
        private Integer rollStatus;

        private Integer userId;

        @Min(value = 1, message = "最小1")
        private Integer page;

        @ApiModelProperty("偏移量数量")
        @Min(value = 1, message = "最小1")
        @Max(value = 20, message = "最大20")
        private Integer size;

        @ApiModelProperty("偏移量起始位置")
        private Integer limit;
    }

    @ApiOperation("获取ROLL房列表")
    @PostMapping("/getRollList")
    public R getRollList(@RequestBody GetRollListParam param) {
        // Long userId = getUserId();
        // if (ObjectUtil.isNull(userId)) return R.fail("登录过期");
        // param.setUserId(userId.intValue());

        List<RollListDataVO> list = apiRollService.getRollList(param);

        return R.ok(list);
    }

    /**
     * 我参与的ROLL房列表
     */
    // @GetMapping("/getMyPartRollList")
    public PageDataInfo<RollListDataVO> getMyPartRollList(@RequestBody GetRollListParam param) {
//        startPage();
        List<RollListDataVO> list = apiRollService.getRollList(param);
        return getPageData(list);
    }

    @ApiOperation("获取ROLL房详情")
    @GetMapping("/getRollDetails/{rollId}")
    public R<RollDetailsDataVO> getRollDetails(@PathVariable("rollId") Integer rollId) {
        return apiRollService.getRollDetails(rollId);
    }

    @ApiOperation("获取ROLL房参与人员详情")
    @PostMapping("/getRollPlayers")
    public R<List<RollUserVO>> getRollPlayers(@RequestBody @Validated GetRollPlayersParam param) {
        return apiRollService.getRollPlayers(param);
    }

    @ApiOperation("是否在当前ROLL房中")
    @GetMapping("/inRoll")
    public R<Boolean> inRoll(@RequestParam("rollId") Integer rollId) {
        Long userId = getUserId();

        List<TtRollUser> list = rollUserService.list(Wrappers.lambdaQuery(TtRollUser.class)
                .eq(TtRollUser::getRollId, rollId)
                .eq(TtRollUser::getUserId, userId)
                .last("limit 1"));

        if (list.isEmpty()){
            return R.ok(false);
        }

        return R.ok(true);
    }

    @ApiOperation("获取ROLL房奖池详情")
    @PostMapping("/getRollPrizePool")
    public R<List<RollJackpotOrnamentsVO>> getRollPrizePool(@RequestBody @Validated GetRollPrizePool param) {
        return apiRollService.getRollPrizePool(param);
    }

    @ApiOperation("获取ROLL开奖详情")
    @PostMapping("/getRollOpenPrize")
    public R<List<TtBoxRecordsVO>> getRollOpenPrize(@RequestBody @Validated GetRollOpenPrizeParam param) {
        return apiRollService.getRollOpenPrize(param);
    }

    @ApiOperation(value = "开奖测试，正式环境删除",hidden = true)
    @GetMapping("/endRoll/{rollId}")
    public R endRoll(@PathVariable("rollId") Integer rollId){
        return apiRollService.endROLL(rollId);
    }

    // @Autowired
    private RabbitTemplate rabbitTemplate;

    // @GetMapping("/endRoll/{rollId}")
    public String mqTest(@PathVariable("rollId") Integer rollId) {

        // Calendar c = Calendar.getInstance();
        // long timeInMillis = c.getTimeInMillis();
        // c.add(Calendar.SECOND, 3);
        // long timeInMillis1 = c.getTimeInMillis();
        // long l = timeInMillis1 - timeInMillis;

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setExpiration("3000"); // 消息的过期属性，单位 ms
        Message message = new Message("这条消息 4 秒后过期".getBytes(), messageProperties);

        rabbitTemplate.convertAndSend(
                DelayedQueueConfig.OPEN_ROLL_EXCHANGE,
                DelayedQueueConfig.OPEN_ROLL_KEY,
                message
        );

        return "1";
    }

}
