package com.ruoyi.admin.controller;

import com.ruoyi.admin.mapper.TtUserBlendErcashMapper;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.dto.TeamDetailsListParam;
import com.ruoyi.domain.dto.sys.TeamUsersParam;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.domain.other.TtUserBody;
import com.ruoyi.domain.other.TtUserPackSackBody;
import com.ruoyi.domain.vo.TeamDetailSimpleVO;
import com.ruoyi.domain.vo.TtUserPackSackDataVO;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.domain.other.ApiUserOnline;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/user")
public class TtUserController extends BaseController {

    private final TtUserService userService;
    private final RedisCache redisCache;
    private final TtBoxRecordsService boxRecordsService;

    public TtUserController(TtUserService ttUserService,
                            RedisCache redisCache,
                            TtBoxRecordsService boxRecordsService) {
        this.userService = ttUserService;
        this.redisCache = redisCache;
        this.boxRecordsService = boxRecordsService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtUser> list(TtUserBody ttUserBody) {
        List<TtUser> list = userService.queryList(ttUserBody);
        list.forEach(ttUser -> ttUser.setPassword(""));
        return getPageData(list);
    }

    @PostMapping("/export")
    public void export(HttpServletResponse response, TtUserBody ttUserBody) {
        List<TtUser> list = userService.queryList(ttUserBody);
        ExcelUtil<TtUser> util = new ExcelUtil<>(TtUser.class);
        util.exportExcel(response, list, "用户信息列表");
    }

    @GetMapping(value = "/{userId}")
    public R<TtUser> getInfo(@PathVariable("userId") Long userId) {
        TtUser user = userService.getById(userId);
        user.setPassword(null);
        if (user.getRemark() != null && user.getRemark().length()>5)user.setRemark(user.getRemark().substring(5));
        return R.ok(user);
    }

    @PostMapping("/generateAccount/{num}")
    public void generateAccount(HttpServletResponse response, @PathVariable("num") Integer num) {
        List<TtUser> userList = userService.getAccountList(num);
        userService.generateAccount(response, userList);
    }

    @ApiOperation("修改用户信息")
    @PutMapping
    public AjaxResult edit(@RequestBody TtUser ttUser) {
        ttUser.setUpdateBy(getUsername());
        ttUser.setUpdateTime(DateUtils.getNowDate());
        return userService.updateUserById(ttUser);
    }

    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        return toAjax(userService.removeByIds(Arrays.asList(userIds)));
    }

    @GetMapping("/online/list")
    public PageDataInfo<ApiUserOnline> list(String ipaddr, String userName) {
        Collection<String> redisKeys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<ApiUserOnline> userOnlineList = redisKeys.stream().map(redisKey -> {
            LoginUser user = redisCache.getCacheObject(redisKey);
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
                return userService.selectOnlineByInfo(ipaddr, userName, user);
            } else if (StringUtils.isNotEmpty(ipaddr)) {
                return userService.selectOnlineByIpaddr(ipaddr, user);
            } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser())) {
                return userService.selectOnlineByUserName(userName, user);
            } else {
                return userService.loginUserToUserOnline(user);
            }
        }).collect(Collectors.toList());
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return getPageData(userOnlineList);
    }

    @GetMapping("/getPackSack")
    public PageDataInfo<TtUserPackSackDataVO> getPackSack(TtUserPackSackBody ttUserPackSackBody) {
        startPage();
        List<TtUserPackSackDataVO> list = userService.getPackSack(ttUserPackSackBody);
        return getPageData(list);
    }

    @PostMapping("/removeUserPackSackData/{ids}")
    public AjaxResult removeUserPackSackData(@PathVariable Long[] ids) {
        List<TtBoxRecords> boxRecordsList = boxRecordsService.getBaseMapper().selectBatchIds(Arrays.asList(ids));
        boxRecordsList = boxRecordsList.stream().peek(ttBoxRecords -> {
            ttBoxRecords.setStatus(TtboxRecordStatus.ADMIN_DELETE.getCode());
            ttBoxRecords.setUpdateTime(DateUtils.getNowDate());
        }).collect(Collectors.toList());
        return toAjax(boxRecordsService.updateBatchById(boxRecordsList, 1));
    }

    /**
     * 用户盈利数据
     * @param userId
     * @return
     */
    @GetMapping("/getUserProfitStatistics/{userId}")
    public R<Object> getUserProfitStatistics(@PathVariable("userId") Integer userId){
        Map<String, BigDecimal> map = userService.getUserProfitStatistics(userId);
        return R.ok(map);
    }

    /**
     * 查询佣金主播汇总数据
     * @param userId
     * @return
     */
    @GetMapping("/getPromotionDataInfo/{userId}")
    public R<Map> getPromotionDataInfo(@PathVariable("userId") Integer userId){

        return R.ok(userService.getPromotionDataInfo(userId));

    }

    /**
     * 查询佣金主播团队数据
     * @return
     */
    @PostMapping("/teamDetailsList")
    public R<Map<String, Object>> teamDetailsList(@RequestBody TeamDetailsListParam param){

        List<TeamDetailSimpleVO> voList = userService.teamDetailsList(param.getParentId(), param.getBeginTime(), param.getEndTime(), param.getPageSize(), param.getPageNum());

        BigDecimal recharge = new BigDecimal(0);
        BigDecimal consume = new BigDecimal(0);
        for (TeamDetailSimpleVO vo : voList) {
            recharge = recharge.add(vo.getRecharge());
            consume = consume.add(vo.getBeConsume());
        }

        HashMap<String, Object> map = new HashMap<>(3);
        map.put("details", voList);
        map.put("selectTotalRecharge", recharge);
        map.put("selectTotalConsume", consume);

        return R.ok(map);

    }




}
