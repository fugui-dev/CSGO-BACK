package com.ruoyi.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.mapper.TtOrderMapper;
import com.ruoyi.admin.service.TtDeliveryRecordService;
import com.ruoyi.admin.service.TtUserAmountRecordsService;
import com.ruoyi.admin.service.TtVipLevelService;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.delivery.TtDeliveryRecord;
import com.ruoyi.domain.entity.recorde.TtUserAmountRecords;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.*;
import com.ruoyi.domain.vo.TtDeliveryRecordDataVO;
import com.ruoyi.domain.vo.TtUserPackSackDataVO;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.user.model.vo.VipLevelInfoVO;
import com.ruoyi.user.service.ApiUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
@Slf4j
public class ApiUserController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final TtUserService userService;
    private final ApiUserService apiUserService;
    private final TtOrderMapper ttOrderMapper;
    private final TtDeliveryRecordService ttDeliveryRecordService;
    private final TtUserAmountRecordsService userAmountRecordsService;
    private final TtDeliveryRecordService deliveryRecordService;

    private final TtVipLevelService ttVipLevelService;

    public ApiUserController(ISysConfigService sysConfigService,
                             TtUserService userService,
                             TtDeliveryRecordService ttDeliveryRecordService,
                             ApiUserService apiUserService, TtOrderMapper ttOrderMapper,
                             TtUserAmountRecordsService userAmountRecordsService,
                             TtDeliveryRecordService deliveryRecordService,
                             TtVipLevelService ttVipLevelService) {
        this.sysConfigService = sysConfigService;
        this.userService = userService;
        this.ttDeliveryRecordService = ttDeliveryRecordService;
        this.apiUserService = apiUserService;
        this.ttOrderMapper = ttOrderMapper;
        this.userAmountRecordsService = userAmountRecordsService;
        this.deliveryRecordService = deliveryRecordService;
        this.ttVipLevelService = ttVipLevelService;
    }

    @ApiOperation("头像上传")
    @UpdateUserCache
    @PostMapping(value = "/profilePictureUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Object> profilePictureUpload(@RequestPart("file") MultipartFile file) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        if (StringUtils.isNull(file)) {
            return R.fail("未选择头像文件！");
        }
        TtUser ttUser = userService.getById(getUserId());
        String msg = apiUserService.profilePictureUpload(ttUser, file);
        return StringUtils.isEmpty(msg) ? R.ok("头像上传成功！") : R.fail(msg);
    }

    @ApiOperation("更新个人信息")
    @UpdateUserCache
    @PostMapping("/updateUserDetails")
    public R<Object> updateUserDetails(@RequestBody ApiUpdateUserDetailsBody updateUserDetailsBody) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());

        String msg = apiUserService.updateUserDetails(ttUser, updateUserDetailsBody);

        return StringUtils.isEmpty(msg) ? R.ok("个人信息更新成功！") : R.fail(msg);
    }

    @ApiOperation("绑定推广人")
    @UpdateUserCache
    @PostMapping("/bindBoss")
    public R bindBoss(@RequestBody ApiUpdateUserDetailsBody updateUserDetailsBody) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());

        return apiUserService.bindBoss(ttUser, updateUserDetailsBody);
    }

    @ApiOperation("修改密码")
    // @UpdateUserCache
    @PostMapping("/changePW")
    public R<Object> changePW(@RequestBody @Validated ApiUpdateUserDetailsBody updateUserDetailsBody, HttpServletRequest request) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());

        String token = CacheConstants.LOGIN_TOKEN_KEY+getToken();

        return apiUserService.changePW(ttUser,updateUserDetailsBody,token);
    }

    @ApiOperation("忘记密码")
    @Anonymous
    @PostMapping("/forgetPassword")
    public R<Boolean> forgetPassword(@RequestBody ApiForgetPasswordBody apiForgetPasswordBody) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String msg = apiUserService.forgetPassword(apiForgetPasswordBody);
        return StringUtils.isEmpty(msg) ? R.ok(true, "密码修改成功成功！") : R.fail(msg);
    }

    @ApiOperation("实名认证")
    @RepeatSubmit(interval = 60000, message = "操作过于频繁，请60秒后重试！")
    @PostMapping("/realNameAuthentication2")
    public R<String> realNameAuthentication(@RequestBody RealNameAuthenticationBody realNameAuthenticationBody) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());
        String msg = apiUserService.realNameAuthentication(ttUser, realNameAuthenticationBody);
        return msg.startsWith("alipays") ? R.ok(msg) : R.fail(msg);
    }

    @ApiOperation("实名认证-2要素认证")
    @RepeatSubmit(interval = 3000, message = "操作过于频繁，请60秒后重试！")
    @PostMapping("/realNameAuthentication")
    public R<String> realNameAuthentication2(@RequestBody RealNameAuthenticationBody realNameAuthenticationBody) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());
        return apiUserService.realNameAuthentication2(ttUser, realNameAuthenticationBody);
    }

    @ApiOperation("认证成功")
    @UpdateUserCache
    @GetMapping("/authenticationOk")
    public R<Boolean> authenticationOk() {
        TtUser ttUser = userService.getById(getUserId());
        String msg = apiUserService.authenticationOk(ttUser);
        return StringUtils.isEmpty(msg) ? R.ok(true, "认证成功！") : R.fail(false, msg);
    }

    @ApiOperation("获取下级人数")
    // @UpdateUserCache
    @GetMapping("/rechargeCount")
    public R<Integer> rechargeCount() {
        TtUser ttUser = userService.getById(getUserId());
        return R.ok(userService.count(new QueryWrapper<TtUser>().eq("parent_id", ttUser.getUserId())));
    }

    @ApiOperation("获取下级提取数")
    @UpdateUserCache
    @GetMapping("/getExtracts")
    public R<BigDecimal> getExtracts() {
        TtUser ttUser = userService.getById(getUserId());
        List<TtUser> ttUsers = userService.list(new QueryWrapper<TtUser>().eq("parent_id", ttUser.getUserId()));
        // 判断是否有下级用户
        if (ttUsers == null || ttUsers.size() == 0 ) {
            return R.ok(new BigDecimal(0));
        }
        List<Integer> collect = ttUsers.stream().map(i -> i.getUserId()).collect(Collectors.toList());
        List<TtDeliveryRecord> ttDeliveryRecords = ttDeliveryRecordService.list(new QueryWrapper<TtDeliveryRecord>().in("user_id", collect).eq("status","10"));
        BigDecimal bigDecimal = new BigDecimal(0);
        ttDeliveryRecords.forEach(o ->{
            bigDecimal.add(o.getOrnamentsPrice());
        });

        return R.ok(bigDecimal);
    }

    @ApiOperation("获取下级充值总金额")
    // @UpdateUserCache
    @GetMapping("/getOrdersAmounts")
    public R<BigDecimal> getOrdersAmounts() {
        TtUser ttUser = userService.getById(getUserId());
        List<TtUser> ttUsers = userService.list(new QueryWrapper<TtUser>().eq("parent_id", ttUser.getUserId()));
        // 判断是否有下级用户
        if (ttUsers == null || ttUsers.size() == 0 ) {
            return R.ok(new BigDecimal(0));
        }
        List<Integer> collect = ttUsers.stream().map(i -> i.getUserId()).collect(Collectors.toList());
        List<TtOrder> ttOrders = ttOrderMapper.selectList(new QueryWrapper<TtOrder>().in("user_id", collect).and(k -> k.eq("status", "3").or().eq("status", "4")));

        BigDecimal bigDecimal = new BigDecimal(0);
        ttOrders.forEach(o ->{
            bigDecimal.add(o.getGoodsPrice());
        });

        return R.ok(bigDecimal);
    }

    @ApiOperation("获取下级流水详情")
    @GetMapping("/getLsjlList")
    public PageDataInfo<TtUserAmountRecords> getLsjlList(TtUserAmountRecordsBody ttUserAmountRecordsBody) {
        TtUser ttUser = userService.getById(getUserId());
        startPage();
        List<TtUser> ttUsers = userService.list(new QueryWrapper<TtUser>().eq("parent_id", ttUser.getUserId()));
        // 判断是否有下级用户
        if (ttUsers == null || ttUsers.size() == 0 ) {
            return getPageData(new ArrayList<TtUserAmountRecords>());
        }
        List<Integer> userIds = ttUsers.stream().map(i -> i.getUserId()).collect(Collectors.toList());
        QueryWrapper<TtUserAmountRecords> ttUserAmountRecordsQueryWrapper = new QueryWrapper<>();
        // 时间段筛选
        if (ttUserAmountRecordsBody.getStartTime() != null) {
            ttUserAmountRecordsQueryWrapper.ge("create_time",ttUserAmountRecordsBody.getStartTime());
        }
        if (ttUserAmountRecordsBody.getEndTime() != null) {
            ttUserAmountRecordsQueryWrapper.lt("create_time",ttUserAmountRecordsBody.getEndTime());
        }
        if (ttUserAmountRecordsBody.getUserId() != null) {
            ttUserAmountRecordsQueryWrapper.eq("user_id",ttUserAmountRecordsBody.getUserId());
        }

        List<TtUserAmountRecords> list = userAmountRecordsService.list(ttUserAmountRecordsQueryWrapper.in("user_id",userIds).orderByDesc("create_time"));

        return getPageData(list);
    }

    @ApiOperation("获取出货记录")
    @GetMapping("/getDeliveryRecordList")
    public PageDataInfo<TtDeliveryRecordDataVO> getDeliveryRecordList(TtDeliveryRecordBody deliveryRecordBody) {
        TtUser ttUser = userService.getById(getUserId());
        deliveryRecordBody.setUserId(ttUser.getUserId());
        startPage();
        List<TtDeliveryRecordDataVO> list = deliveryRecordService.getDeliveryRecordByUserList(deliveryRecordBody);
        return getPageData(list);
    }

    /**
     * 每日出货排行榜
     * @param type 1今天 2昨天 3近一周
     */
    @ApiOperation("每日出货排行榜")
    @GetMapping("/propRankOfDay/{type}/{number}")
    public R propRankOfDay(@PathVariable("type") Integer type,
                           @PathVariable(value = "number",required = false) Integer number) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);

        Timestamp end = null;
        Timestamp begin = null;
        if (type.equals(1)) {
            begin = new Timestamp(c.getTimeInMillis());
            end = new Timestamp(System.currentTimeMillis());
        }else if (type.equals(2)) {
            end = new Timestamp(c.getTimeInMillis());
            c.add(Calendar.DAY_OF_MONTH,-1);
            begin = new Timestamp(c.getTimeInMillis());
        }else if (type.equals(3)) {
            end = new Timestamp(c.getTimeInMillis());
            c.add(Calendar.WEEK_OF_MONTH,-1);
            begin = new Timestamp(c.getTimeInMillis());
        }

        number = 10;
        List<TtUserPackSackDataVO> list = userService.propRankOfDay(begin,end,number);
        // return getPageData(list);
        return R.ok(list);
    }


    @ApiOperation("获取用户总充值和VIP等级信息")
    @GetMapping("vipLevelInfo")
    public R<VipLevelInfoVO> vipLevelInfo(){

        //获取用户总充值
        TtUser user = userService.getOne(Wrappers.lambdaQuery(TtUser.class)
                .select(TtUser::getTotalRecharge)
                .eq(TtUser::getUserId, SecurityUtils.getUserId()));
        BigDecimal totalRecharge = user.getTotalRecharge();

        List<TtVipLevel> list = ttVipLevelService.list();

        return R.ok(new VipLevelInfoVO(totalRecharge, list));

    }
}
