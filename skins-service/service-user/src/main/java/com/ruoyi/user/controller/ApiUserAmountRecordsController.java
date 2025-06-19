package com.ruoyi.user.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.admin.mapper.TtPromotionUpdateMapper;
import com.ruoyi.admin.mapper.TtUserBlendErcashMapper;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.admin.service.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.dto.sys.TeamUsersParam;
import com.ruoyi.domain.dto.userRecord.AmountRecordsDetailCondition;
import com.ruoyi.domain.dto.userRecord.DeliveryRecordsConfition;
import com.ruoyi.domain.dto.userRecord.OrderCondition;
import com.ruoyi.domain.vo.*;
import com.ruoyi.domain.vo.TtUserAmountRecords.PWelfareVO;
import com.ruoyi.domain.vo.TtUserAmountRecords.UserAmountDetailVO;
import com.ruoyi.domain.vo.delivery.DeliveryRecordVO;
import com.ruoyi.domain.vo.order.TtOrderVO;
import com.ruoyi.domain.vo.sys.SimpleTtUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ruoyi.domain.common.constant.TtAccountRecordSource.*;

@Api(tags = "用户充值记录")
@RestController
@RequestMapping("/api/userAmountRecords")
@Slf4j
public class ApiUserAmountRecordsController extends BaseController {

    @Autowired
    private TtUserAmountRecordsService ttUserAmountRecordsService;

    @Autowired
    private TtUserBlendErcashMapper ttUserBlendErcashMapper;

    // @Autowired
    // private TtUserBlendErcashMapper ttUserBlendErcashMapper;

    @Autowired
    private TtUserAmountRecordsService userAmountRecordsService;

    @Autowired
    private TtUserService userService;

    @Autowired
    private TtUserMapper userMapper;

    @Autowired
    private TtOrderService orderService;

    // @Autowired
    // private TtUserBlendErcashMapper ttUserBlendErcashMapper;

    @Autowired
    private TtDeliveryRecordService ttDeliveryRecordService;

    @Autowired
    private TtOrderService ttOrderService;

    @Autowired
    private TtPromotionUpdateMapper ttPromotionUpdateMapper;

    @Autowired
    private TtPromotionRecordService promotionRecordService;


    /**
     * 统计用户充值网推广数据
     * @param userId
     * @return
     */
    @ApiOperation("统计用户充值网推广数据")
    @GetMapping("/statisticsPromotionData/{userId}")
    public R<PromotionDataVO> statisticsPromotionData(@PathVariable("userId") Integer userId){
        PromotionDataVO data = promotionRecordService.statisticsPromotionData(userId);
        return R.ok(data);
    }

    /**
     * 充值网流水排行榜
     *
     * @param type 1今天 2昨天 3近一周
     */
    @ApiOperation("充值网流水排行榜")
    @Anonymous
    @GetMapping("/amountRank/{type}/{page}/{size}")
    public Page<TtUserAccountRecordsRankVO> amountRank(@PathVariable("type") Integer type,
                                                       @PathVariable("page") Integer page,
                                                       @PathVariable("size") Integer size) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp end = null;
        Timestamp begin = null;

        if (type.equals(1)) {
            begin = new Timestamp(c.getTimeInMillis());
            end = new Timestamp(System.currentTimeMillis());
        } else if (type.equals(2)) {
            end = new Timestamp(c.getTimeInMillis());
            c.add(Calendar.DAY_OF_MONTH, -1);
            begin = new Timestamp(c.getTimeInMillis());
        } else if (type.equals(3)) {
            end = new Timestamp(c.getTimeInMillis());
            c.add(Calendar.WEEK_OF_MONTH, -1);
            begin = new Timestamp(c.getTimeInMillis());
        }

        return ttUserAmountRecordsService.rank(begin, end, page, size);
    }

    /**
     * 充值网推广奖励明细
     */
    @ApiOperation("充值网推广奖励明细")
    @GetMapping("/pWelfareRecords/{page}/{size}")
    public R pWelfareRecords(@PathVariable("page") Integer page,
                             @PathVariable("size") Integer size) {
        if (page < 1) {
            return R.fail("page > 0");
        }
        if (size > 21) {
            return R.fail("size <= 20 !!!");
        }
        Long userId = getUserId();
        return ttUserAmountRecordsService.pWelfareRecords(userId.intValue(), page, size);
    }

    /**
     * 流水网推广奖励明细-暂时交换了前端url
     */
    @ApiOperation("流水网推广奖励明细")
    @GetMapping("/pCommissionRecords/{page}/{size}")
    public R<PWelfareVO> pCommissionRecords(@PathVariable("page") Integer page,
                                            @PathVariable("size") Integer size) {
        if (page < 1) {
            return R.fail("page > 0");
        }
        if (size > 21) {
            return R.fail("size <= 20 !!!");
        }
        Long userId = getUserId();
        return ttUserAmountRecordsService.pCommissionRecords(userId.intValue(), page, size);
    }

    /**
     * 综合排行榜
     */
    @ApiOperation("综合排行榜")
    // @Anonymous
    @PostMapping("/blendErcashRank")
    public R blendErcashRank(@RequestBody RankParam param) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp end = null;
        Timestamp begin = null;

        if (param.getType().equals(1)) {
            begin = new Timestamp(c.getTimeInMillis());
            end = new Timestamp(System.currentTimeMillis());
        } else if (param.getType().equals(2)) {
            end = new Timestamp(c.getTimeInMillis());
            c.add(Calendar.DAY_OF_MONTH, -1);
            begin = new Timestamp(c.getTimeInMillis());
        } else if (param.getType().equals(3)) {
            end = new Timestamp(c.getTimeInMillis());
            c.add(Calendar.WEEK_OF_MONTH, -1);
            begin = new Timestamp(c.getTimeInMillis());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginT = dateFormat.format(begin);
        String endT = dateFormat.format(end);
        if (StringUtils.isBlank(beginT) || StringUtils.isBlank(beginT)) {
            beginT = null;
            endT = null;
        }

        List<Integer> source = Arrays.asList(
                GAME_TYPE_01.getCode(),
                GAME_TYPE_02.getCode(),
                // GAME_TYPE_03.getCode(),
                GAME_TYPE_04.getCode()
        );
        Integer limit = (param.getPage() - 1) * param.getSize();
        List<UserBERankVO> rank = ttUserBlendErcashMapper.rank(
                source,
                beginT,
                endT,
                limit,
                param.getSize());
        return R.ok(rank);

    }

    @ApiOperation("获取个人综合收支明细")
    // @NewUserInfo
    // @Anonymous
    @PostMapping("/userAccountDetail")
    public R<List<UserAmountDetailVO>> userAccountDetail(@RequestBody @Validated AmountRecordsDetailCondition param) {
        // R r = checkLogin();
        // if (!r.getCode().equals(200)) return R.fail(401,"登录过期");
        // Integer userId = ((Long)r.getData()).intValue();
        param.setUserId(null);

        param.setUserId(getUserId().intValue());
        // List<TtUserAmountRecords> list1 = userAmountRecordsService.queryList(param);
        List<UserAmountDetailVO> list = userAmountRecordsService.userAccountDetail(param);
        return R.ok(list);
    }

    @ApiOperation("获取个人提货记录")
    // @Anonymous
    @PostMapping("/deliveryRecords")
    public R deliveryRecords(@RequestBody @Validated DeliveryRecordsConfition param) {
        param.setUIdList(null);
        int userId = getUserId().intValue();
        if (ObjectUtil.isNull(userId)) return R.fail(401, "登录过期请重新登录。");
        param.setUIdList(Arrays.asList(userId));

        List<DeliveryRecordVO> list = ttDeliveryRecordService.byCondition(param);

        return R.ok(list);
    }

    @ApiOperation("获取团队用户列表")
    // @Anonymous
    @PostMapping("/teamDetailsList")
    public R<List<TeamDetailSimpleVO>> teamDetailsList(@RequestBody @Validated TeamUsersParam param) {
        Long parentId = getUserId();
        return R.ok(ttUserBlendErcashMapper.teamDetailsList(parentId, param.getBeginTime(), param.getEndTime(), 1000, param.getPage()));

    }


    @ApiOperation("获取团队用户列表")
    // @Anonymous
    @PostMapping("/teamUsers")
    public R<List<SimpleTtUserVO>> teamUsers(@RequestBody @Validated TeamUsersParam param) {
        param.setBossIds(null);
        param.setEmployeeIds(null);
        param.setBossIds(Arrays.asList(getUserId().intValue()));
        if (ObjectUtil.isNull(param.getOrderByFie())) param.setOrderByFie(2);

        // 查询所有下级id
        List<Integer> allEmployeesId = userMapper.allEmployeesByParents(param.getBossIds());
        if (allEmployeesId.isEmpty()) {
            return R.ok(new ArrayList<>());
        }

        // 解析时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date beginTime = null;
        try {
            beginTime = dateFormat.parse(param.getBeginTime());
        } catch (ParseException e) {
            return R.fail("日期解析异常，检查日期格式是否正确。");
        }

        // main
        if (param.getOrderByFie().equals(1)) {

            // 查询下级的最近一个绑定时间
            List<TeamDetailVO> mit = ttPromotionUpdateMapper.latelyUpdate(allEmployeesId);
            if (mit.size() < allEmployeesId.size()) log.warn("下级已绑定上级，但未写入更新日志，请及时检查！！！");
            // 如果最近绑定时间大于本次查询的起始时间，以最近绑定时间为准
            List<Integer> empIds1 = new ArrayList<>();
            List<TeamDetailVO> empIds2 = new ArrayList<>();
            for (TeamDetailVO item : mit) {
                Timestamp latelyTime = item.getBeginTime();
                if (latelyTime.compareTo(new Timestamp(beginTime.getTime())) > 0) {
                    empIds2.add(item);
                } else {
                    empIds1.add(item.getEmployeeId());
                }
            }

            List<SimpleTtUserVO> batchRechargeTotal1 = new ArrayList<>();
            if (!empIds1.isEmpty()){
                // 主表充值统计
                batchRechargeTotal1 = orderService.batchRechargeTotal(
                        empIds1,
                        param.getBeginTime(),
                        param.getEndTime(),
                        param.getOrderType(),
                        param.getPage(),
                        param.getSize());
            }

            List<SimpleTtUserVO> batchRechargeTotal2 = new ArrayList<>();
            if (!empIds2.isEmpty()){

                for (TeamDetailVO vo : empIds2){

                    String beginT = dateFormat.format(vo.getBeginTime());

                    List<SimpleTtUserVO> data = orderService.batchRechargeTotal(
                            Arrays.asList(vo.getEmployeeId()),
                            beginT,
                            param.getEndTime(),
                            param.getOrderType(),
                            param.getPage(),
                            param.getSize());

                    batchRechargeTotal2.addAll(data);
                }
            }

            batchRechargeTotal1.addAll(batchRechargeTotal2);
            return R.ok(batchRechargeTotal1);

        } else if (param.getOrderByFie().equals(2) || param.getOrderByFie().equals(3) || param.getOrderByFie().equals(4)) {
            if (StringUtils.isBlank(param.getBeginTime())) {
                param.setBeginTime(null);
            }
            if (StringUtils.isBlank(param.getEndTime())) {
                param.setEndTime(null);
            }
            if (ObjectUtil.isNull(param.getOrderType())) {
                param.setOrderType(1);
            }

            System.err.println("所有下级ID：" + allEmployeesId);
            // 查询下级的最近一个绑定时间
            List<TeamDetailVO> mit = ttPromotionUpdateMapper.latelyUpdate(allEmployeesId);
            System.err.println("所有下级最近绑定时间：" + mit);
            if (mit.size() < allEmployeesId.size()) {
                log.warn("下级已绑定上级，但未写入更新日志，请及时检查！！！");
            }
            // 如果最近绑定时间大于本次查询的起始时间，以最近绑定时间为准
            List<Integer> empIds1 = new ArrayList<>();
            List<TeamDetailVO> empIds2 = new ArrayList<>();
            for (TeamDetailVO item : mit) {
                Timestamp latelyTime = item.getBeginTime();
                if (latelyTime.compareTo(new Timestamp(beginTime.getTime())) > 0) {
                    empIds2.add(item);
                } else {
                    empIds1.add(item.getEmployeeId());
                }
            }
            System.err.println(empIds1);
            System.err.println(empIds2);

            List<SimpleTtUserVO> batchConsumeTotal1 = new ArrayList<>();
            if (!empIds1.isEmpty()) {
                // 主表消费统计
                batchConsumeTotal1 = ttUserBlendErcashMapper.batchConsumeTotal(
                        empIds1,
                        param.getBeginTime(),
                        param.getEndTime(),
                        param.getOrderByFie(),
                        param.getOrderType(),
                        (param.getPage() - 1) * param.getSize(),
                        param.getSize());
            }

            List<SimpleTtUserVO> batchConsumeTotal2 = new ArrayList<>();
            if (!empIds2.isEmpty()){
                for (TeamDetailVO vo : empIds2){

                    String beginT = dateFormat.format(vo.getBeginTime());

                    List<SimpleTtUserVO> data = ttUserBlendErcashMapper.batchConsumeTotal(
                            Arrays.asList(vo.getEmployeeId()),
                            beginT,
                            param.getEndTime(),
                            param.getOrderByFie(),
                            param.getOrderType(),
                            (param.getPage() - 1) * param.getSize(),
                            param.getSize()
                    );
                    System.err.println(data);

                    batchConsumeTotal2.addAll(data);
                }
            }

            batchConsumeTotal1.addAll(batchConsumeTotal2);

            return R.ok(batchConsumeTotal1);

        } else {
            return R.fail("非法的排序字段");
        }

        // return userService.teamUsers(param);
    }

    @ApiOperation("获取个人充值明细")
    @PostMapping("/rechargeRecords")
    public R rechargeRecords(@RequestBody @Validated OrderCondition param) {
        Long userId = getUserId();
        if (ObjectUtil.isNull(userId)) return R.fail(401, "登录过期，请重新登录。");

        param.setUserIdList(Arrays.asList(userId.intValue()));

        List<TtOrderVO> list = ttOrderService.byCondition(param);
        return R.ok(list);
    }

    public R checkLogin() {
        Long userId;
        try {
            userId = getUserId();
            if (ObjectUtil.isEmpty(userId)) AjaxResult.error(401, "登录过期，请重新登录。");
            return R.ok(userId);
        } catch (Exception e) {
            return R.fail(401, "登录过期，请重新登录。");
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class RankParam {

        @ApiModelProperty("类型（1今天 2昨天 3近七天）")
        @NotEmpty(message = "时间区间不能为空")
        private Integer type;
        @Min(value = 1, message = "最小1")
        private Integer page;
        @Min(value = 1, message = "最小1")
        private Integer size;
    }
}
