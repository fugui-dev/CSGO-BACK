package com.ruoyi.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.github.pagehelper.Page;
import com.ruoyi.admin.mapper.TtBoxRecordsMapper;
import com.ruoyi.admin.mapper.TtUserBlendErcashMapper;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtDeliveryRecordService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.domain.common.constant.*;
import com.ruoyi.domain.common.constant.sys.UserStatus;
import com.ruoyi.domain.dto.packSack.DecomposeLogCondition;
import com.ruoyi.domain.dto.packSack.DecomposeParam;
import com.ruoyi.domain.dto.packSack.DeliveryParam;
import com.ruoyi.domain.dto.packSack.PackSackCondition;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.delivery.TtDeliveryRecord;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.vo.TtBoxRecordsDataVO;
import com.ruoyi.domain.vo.UserPackSackDataVO;
import com.ruoyi.domain.vo.client.PackSackGlobalData;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.common.service.DeliverGoodsService;
import com.ruoyi.user.mapper.ApiUserPackSackMapper;
import com.ruoyi.user.model.dto.SmeltRequest;
import com.ruoyi.user.model.vo.SmeltVO;
import com.ruoyi.user.service.ApiUserPackSackService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ruoyi.admin.config.RedisConstants.USER_PLAY_COMMON;

@Service
public class ApiUserPackSackServiceImpl implements ApiUserPackSackService {

    private final ISysConfigService configService;
    private final TtUserService userService;
    private final ApiUserPackSackMapper userPackSackMapper;
    private final TtBoxRecordsService boxRecordsService;


    private final TtBoxRecordsMapper ttBoxRecordsMapper;
    private final TtDeliveryRecordService deliveryRecordService;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private DeliverGoodsService deliverGoodsService;

    @Autowired
    private ThreadPoolExecutor customThreadPoolExecutor;

    @Autowired
    private TtUserBlendErcashMapper TtUserBlendErcashMapper;

    @Autowired
    private TtOrnamentService ornamentService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisLock redisLock;

    public ApiUserPackSackServiceImpl(ISysConfigService configService, TtUserService userService,
                                      ApiUserPackSackMapper userPackSackMapper,
                                      TtBoxRecordsService boxRecordsService,
                                      TtBoxRecordsMapper ttBoxRecordsMapper,
                                      TtDeliveryRecordService deliveryRecordService,
                                      RabbitTemplate rabbitTemplate) {
        this.configService = configService;
        this.userService = userService;
        this.userPackSackMapper = userPackSackMapper;
        this.boxRecordsService = boxRecordsService;
        this.ttBoxRecordsMapper = ttBoxRecordsMapper;
        this.deliveryRecordService = deliveryRecordService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public R delivery(DeliveryParam param, TtUser ttUser) {

        String lockKey = USER_PLAY_COMMON + "user_id:" + ttUser.getUserId();
        Boolean lock = redisLock.tryLock(lockKey, 2L, 3L, TimeUnit.SECONDS);
        if (!lock) {
            return R.fail("操作频繁，请重试！");
        }

        if (!param.getIsAll()) {

            String transactionLink = ttUser.getTransactionLink();
            Long steamId = ttUser.getSteamId();

            if (ObjectUtils.isEmpty(param.getPackSackIds())) {
                return R.fail(false, "请选择需要提取的饰品！");
            }

            // 查询并修改boxRecord的状态（改为申请提货状态，不改数据库）
            List<TtBoxRecords> boxRecordsList = packSackHandle(param.getPackSackIds(), ttUser, TtboxRecordStatus.APPLY_DELIVERY.getCode());

            if (boxRecordsList.isEmpty()) {
                return R.fail(false, "请选择需要提取的饰品！");
            } else {
                // 检查是否包含道具
                Integer deliveryAble = ttBoxRecordsMapper.checkDeliveryAble(param.getPackSackIds());
                if (deliveryAble > 0) {
                    return R.fail("道具不可用于提货");
                }
            }

            // 普通用户需要绑定交易链接
            // if (ttUser.getUserType().equals(UserType.COMMON_USER.getCode())) {
            //     if (StringUtils.isEmpty(transactionLink)) {
            //         return R.fail(false, "您未绑定交易链接，请绑定Steam交易链接后重试！");
            //     }
            // }
            if (StringUtils.isEmpty(transactionLink)) {
                return R.fail(false, "您未绑定交易链接，请绑定Steam交易链接后重试！");
            }

            // 更新开箱记录状态
            if (!boxRecordsService.updateBatchById(boxRecordsList, 1)) {
                R.fail("饰品提取异常，请联系管理员！");
            }

            // 构建提货记录数据
            List<TtDeliveryRecord> deliveryRecordList = new ArrayList<>();
            for (TtBoxRecords ttBoxRecords : boxRecordsList) {
                TtDeliveryRecord ttDeliveryRecord = TtDeliveryRecord.builder()
                        .userId(ttUser.getUserId())
                        .boxRecordsId(ttBoxRecords.getId())
                        .ornamentId(ttBoxRecords.getOrnamentId())
                        .marketHashName(ttBoxRecords.getMarketHashName())
                        .ornamentsPrice(ttBoxRecords.getOrnamentsPrice())
                        .outTradeNo(IdUtils.fastSimpleUUID().toUpperCase())
                        .build();
                 if (ttUser.getUserType().equals(UserType.ANCHOR.getCode())) {
                     // 主播直接发货完成
                      ttDeliveryRecord.setDelivery(DeliveryPattern.ANCHOR.getCode());
                     ttDeliveryRecord.setStatus(DeliveryOrderStatus.ORDER_COMPLETE.getCode());
                     ttDeliveryRecord.setMessage(DeliveryOrderStatus.ORDER_COMPLETE.getMsg());
                     ttDeliveryRecord.setCreateBy(ttUser.getNickName());
                     ttDeliveryRecord.setCreateTime(DateUtils.getNowDate());
                 } else if (ttUser.getUserType().equals(UserType.COMMON_USER.getCode())) {
                     // 自动发货最小价格
                     String autoDeliveryMinPriceStr = configService.selectConfigByKey("autoDeliveryMinPrice");
                     if (new BigDecimal(autoDeliveryMinPriceStr).compareTo(ttBoxRecords.getOrnamentsPrice()) > 0) {
                         ttDeliveryRecord.setDelivery(DeliveryPattern.AUTO.getCode());
                     } else {
                         ttDeliveryRecord.setDelivery(DeliveryPattern.MANUAL.getCode());
                     }
                     ttDeliveryRecord.setStatus(DeliveryOrderStatus.DELIVERY_BEFORE.getCode());
                     ttDeliveryRecord.setMessage(DeliveryOrderStatus.DELIVERY_BEFORE.getMsg());
                     ttDeliveryRecord.setCreateBy(ttUser.getNickName());
                     ttDeliveryRecord.setCreateTime(DateUtils.getNowDate());
                 }

                // 自动发货最小价格
//                String autoDeliveryMinPriceStr = configService.selectConfigByKey("autoDeliveryMinPrice");
//                if (new BigDecimal(autoDeliveryMinPriceStr).compareTo(ttBoxRecords.getOrnamentsPrice()) > 0) {
//                    ttDeliveryRecord.setDelivery(DeliveryPattern.AUTO.getCode());
//                } else {
//                    ttDeliveryRecord.setDelivery(DeliveryPattern.MANUAL.getCode());
//                }
//                ttDeliveryRecord.setStatus(DeliveryOrderStatus.DELIVERY_BEFORE.getCode());
//                ttDeliveryRecord.setMessage(DeliveryOrderStatus.DELIVERY_BEFORE.getMsg());
//                ttDeliveryRecord.setCreateBy(ttUser.getNickName());
//                ttDeliveryRecord.setCreateTime(DateUtils.getNowDate());

                deliveryRecordList.add(ttDeliveryRecord);
            }

            if (!deliveryRecordService.saveBatch(deliveryRecordList, 1)) {
                R.fail("提货记录更新失败。");
            }

            // 异步处理自动发货
            CompletableFuture.runAsync(() -> {
                deliverGoodsService.autoDelivery(ttUser.getUserId());
            }, customThreadPoolExecutor);

            // String message = String.valueOf(ttUser.getUserId());
            // rabbitTemplate.convertAndSend(DelayedQueueConfig.DELIVERY_QUEUE, message);
            return R.ok(true);
        } else {

            String transactionLink = ttUser.getTransactionLink();
            Long steamId = ttUser.getSteamId();

            List<TtBoxRecords> boxRecordsList = new LambdaQueryChainWrapper<>(boxRecordsService.getBaseMapper())
                    .eq(TtBoxRecords::getHolderUserId, ttUser.getUserId())
                    .eq(TtBoxRecords::getStatus, TtboxRecordStatus.IN_PACKSACK_ON.getCode())
                    .list();

            if (boxRecordsList.isEmpty()) {
                return R.ok("操作完成，背包没有物品。");
            }

            // 检查是否包含道具
            //Integer deliveryAble = ttBoxRecordsMapper.checkDeliveryAble(param.getPackSackIds());
            Integer deliveryAble = ttBoxRecordsMapper.checkAllDeliveryAble(ttUser.getUserId());
            if (deliveryAble > 0) {
                return R.fail("道具不可用于提货");
            }

            boxRecordsList = boxRecordsList.stream().peek(ttBoxRecords -> {
                ttBoxRecords.setStatus(TtboxRecordStatus.APPLY_DELIVERY.getCode());
                ttBoxRecords.setUpdateTime(DateUtils.getNowDate());
            }).collect(Collectors.toList());

            if (boxRecordsList.isEmpty()) return R.fail(false, "请选择需要提取的饰品！");
            // if (ttUser.getUserType().equals(UserType.COMMON_USER.getCode())) {    // 普通用户
            //     if (StringUtils.isEmpty(transactionLink))
            //         return R.fail(false, "您未绑定交易链接，请绑定Steam交易链接后重试！");
            // }
            if (StringUtils.isEmpty(transactionLink))
                return R.fail(false, "您未绑定交易链接，请绑定Steam交易链接后重试！");

            // 更新开箱记录状态
            if (!boxRecordsService.updateBatchById(boxRecordsList, 1)) {
                R.fail("饰品提取异常，请联系管理员！");
            }

            // 构建提货记录数据
            List<TtDeliveryRecord> deliveryRecordList = new ArrayList<>();
            for (TtBoxRecords ttBoxRecords : boxRecordsList) {
                TtDeliveryRecord ttDeliveryRecord = TtDeliveryRecord.builder()
                        .userId(ttUser.getUserId())
                        .boxRecordsId(ttBoxRecords.getId())
                        .ornamentId(ttBoxRecords.getOrnamentId())
                        .marketHashName(ttBoxRecords.getMarketHashName())
                        .ornamentsPrice(ttBoxRecords.getOrnamentsPrice())
                        .outTradeNo(IdUtils.fastSimpleUUID().toUpperCase())
                        .build();
                 if (ttUser.getUserType().equals(UserType.ANCHOR.getCode())) {
                     // 主播直接发货完成
                     ttDeliveryRecord.setDelivery(DeliveryPattern.ANCHOR.getCode());
                     ttDeliveryRecord.setStatus(DeliveryOrderStatus.ORDER_COMPLETE.getCode());
                     ttDeliveryRecord.setMessage(DeliveryOrderStatus.ORDER_COMPLETE.getMsg());
                     ttDeliveryRecord.setCreateBy(ttUser.getNickName());
                     ttDeliveryRecord.setCreateTime(DateUtils.getNowDate());
                 } else if (ttUser.getUserType().equals(UserType.COMMON_USER.getCode())) {
                     // 自动发货最小价格
                     String autoDeliveryMinPriceStr = configService.selectConfigByKey("autoDeliveryMinPrice");
                     if (new BigDecimal(autoDeliveryMinPriceStr).compareTo(ttBoxRecords.getOrnamentsPrice()) > 0) {
                         ttDeliveryRecord.setDelivery(DeliveryPattern.AUTO.getCode());
                     }
                     ttDeliveryRecord.setDelivery(DeliveryPattern.MANUAL.getCode());
                     ttDeliveryRecord.setCreateBy(ttUser.getNickName());
                     ttDeliveryRecord.setCreateTime(DateUtils.getNowDate());
                 }

//                // 自动发货最小价格
//                if (!Objects.equals(ttDeliveryRecord.getDelivery(), DeliveryPattern.ANCHOR.getCode())){
//                    String autoDeliveryMinPriceStr = configService.selectConfigByKey("autoDeliveryMinPrice");
//                    if (new BigDecimal(autoDeliveryMinPriceStr).compareTo(ttBoxRecords.getOrnamentsPrice()) > 0) {
//                        ttDeliveryRecord.setDelivery(DeliveryPattern.AUTO.getCode());
//                    }
//                }

//                ttDeliveryRecord.setDelivery(DeliveryPattern.MANUAL.getCode());
//                ttDeliveryRecord.setCreateBy(ttUser.getNickName());
//                ttDeliveryRecord.setCreateTime(DateUtils.getNowDate());

                deliveryRecordList.add(ttDeliveryRecord);
            }

            if (!deliveryRecordService.saveBatch(deliveryRecordList, 1)) {
                R.fail("提货记录更新失败。");
            }

            // todo 异步处理自动发货（整个方法可以优化）
            CompletableFuture.runAsync(() -> {
                deliverGoodsService.autoDelivery(ttUser.getUserId());
            }, customThreadPoolExecutor);

            // String message = String.valueOf(ttUser.getUserId());
//             rabbitTemplate.convertAndSend(DelayedQueueConfig.DELIVERY_QUEUE, message);

            return R.ok(true);

        }
    }

    @Override
    @Transactional
    public int decompose(DecomposeParam param, TtUser ttUser) {

        if (!param.getIsAll()) {
            List<TtBoxRecords> boxRecordsList = new LambdaQueryChainWrapper<>(boxRecordsService.getBaseMapper())
                    .eq(TtBoxRecords::getHolderUserId, ttUser.getUserId())
                    .eq(TtBoxRecords::getStatus, TtboxRecordStatus.IN_PACKSACK_ON.getCode())
                    .in(TtBoxRecords::getId, param.getPackSackIds())
                    .list();
            if (boxRecordsList.isEmpty()) {
                return 0;
            }
            boxRecordsList = boxRecordsList.stream().peek(ttBoxRecords -> {
                ttBoxRecords.setStatus(TtboxRecordStatus.RESOLVE.getCode());
                ttBoxRecords.setUpdateTime(DateUtils.getNowDate());
            }).collect(Collectors.toList());

            //List<TtBoxRecords> boxRecordsList = packSackHandle(param.getPackSackIds(), ttUser, TtboxRecordStatus.RESOLVE.getCode());

            boxRecordsService.updateBatchById(boxRecordsList, 1);

            // 累加
            BigDecimal decomposeTotal = boxRecordsList.stream()
                    .map(TtBoxRecords::getOrnamentsPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 获得弹药
            TtUser byId = userService.getById(ttUser.getUserId());
            byId.setAccountCredits(ttUser.getAccountCredits().add(decomposeTotal));
            byId.setAccountAmount(ttUser.getAccountAmount().add(decomposeTotal));
            userService.updateById(byId);

            // 综合消费日志
            TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                    .userId(byId.getUserId())

                     .amount(ObjectUtil.isNotEmpty(decomposeTotal) ? decomposeTotal : null)
                     .finalAmount(ObjectUtil.isNotEmpty(decomposeTotal) ? byId.getAccountAmount() : null)

                    .credits(ObjectUtil.isNotEmpty(decomposeTotal) ? decomposeTotal : null)
                    .finalCredits(ObjectUtil.isNotEmpty(decomposeTotal) ? byId.getAccountCredits() : null)

                    .total(decomposeTotal)  // 收支合计

                    .type(TtAccountRecordType.INPUT.getCode())
                    .source(TtAccountRecordSource.DECOMPOSE_ORNAMENT.getCode())
                    .remark(TtAccountRecordSource.DECOMPOSE_ORNAMENT.getMsg())

                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()))
                    .build();

            TtUserBlendErcashMapper.insert(blendErcash);

            return boxRecordsList.size();
        }else {

            // 全部分解
            LambdaQueryWrapper<TtBoxRecords> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1
                    .eq(TtBoxRecords::getHolderUserId,ttUser.getUserId())
                    .eq(TtBoxRecords::getStatus,TtboxRecordStatus.IN_PACKSACK_ON.getCode());
            List<TtBoxRecords> list = boxRecordsService.list(wrapper1);

            // 更新物品状态
            Timestamp now = new Timestamp(System.currentTimeMillis());
            List<TtBoxRecords> collect = list.stream().peek(item -> {
                item.setStatus(TtboxRecordStatus.RESOLVE.getCode());
                item.setUpdateTime(now);
            }).collect(Collectors.toList());
            boxRecordsService.updateBatchById(collect,1);

            // 累加弹药
            BigDecimal total = list.stream()
                    .map(TtBoxRecords::getOrnamentsPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            LambdaUpdateWrapper<TtUser> wrapper2 = new LambdaUpdateWrapper<>();
            wrapper2
                    .eq(TtUser::getUserId,ttUser.getUserId())
                    .eq(TtUser::getStatus, UserStatus.NORMAL.getCode())
                    .eq(TtUser::getDelFlag,0)
                    .setSql("account_credits = account_credits + "+total.toString() +
                            ", account_amount = account_amount + " + total.toString());
            userService.update(wrapper2);

            TtUser byId = userService.getById(ttUser.getUserId());
            // 综合消费日志
            TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                    .userId(byId.getUserId())

                     .amount(ObjectUtil.isNotEmpty(total) ? total : null)
                     .finalAmount(ObjectUtil.isNotEmpty(total) ? byId.getAccountAmount() : null)

                    .credits(ObjectUtil.isNotEmpty(total) ? total : null)
                    .finalCredits(ObjectUtil.isNotEmpty(total) ? byId.getAccountCredits() : null)

                    .total(total)  // 收支合计

                    .type(TtAccountRecordType.INPUT.getCode())
                    .source(TtAccountRecordSource.DECOMPOSE_ORNAMENT.getCode())
                    .remark(TtAccountRecordSource.DECOMPOSE_ORNAMENT.getMsg())

                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()))
                    .build();

            TtUserBlendErcashMapper.insert(blendErcash);

            return list.size();
        }

    }

    @Override
    public List<UserPackSackDataVO> getPackSack(Integer userId) {
        return userPackSackMapper.getPackSack(userId);
    }

    // 查询并修改boxRecord的状态（不改数据库）
    @Override
    public List<TtBoxRecords> packSackHandle(List<Long> packSackIds, TtUser ttUser, Integer status) {

        List<TtBoxRecords> boxRecordsList = new LambdaQueryChainWrapper<>(boxRecordsService.getBaseMapper())
                .eq(TtBoxRecords::getHolderUserId, ttUser.getUserId())
                .eq(TtBoxRecords::getStatus, TtboxRecordStatus.IN_PACKSACK_ON.getCode())
                .in(TtBoxRecords::getId, packSackIds)
                .list();
        if (!boxRecordsList.isEmpty()) {
            boxRecordsList = boxRecordsList.stream().peek(ttBoxRecords -> {
                ttBoxRecords.setStatus(status);
                ttBoxRecords.setUpdateTime(DateUtils.getNowDate());
            }).collect(Collectors.toList());
        }
        return boxRecordsList;
    }

    @Override
    public List<TtBoxRecordsDataVO> decomposeLog(DecomposeLogCondition param) {
        param.setLimit((param.getPage() - 1) * param.getSize());
        param.setBoxRecordStatus(TtboxRecordStatus.RESOLVE.getCode());
        return ttBoxRecordsMapper.decomposeLog(param);
    }

    @Override
    public R clientPackSack(PackSackCondition condition) {

        if (StringUtils.isBlank(condition.getBeginTime()) || StringUtils.isBlank(condition.getBeginTime())) {
            condition.setBeginTime(null);
            condition.setEndTime(null);
        }
        if (StringUtils.isBlank(condition.getName())) condition.setName(null);

        if (condition.getPage()!=null && condition.getSize()!=null){
            PageUtils.startPage(condition.getPage(), condition.getSize());
        }

        List<UserPackSackDataVO> vos = userPackSackMapper.clientPackSack(
                condition.getUidList(),
                condition.getStatusList(),
                condition.getName(),
                condition.getBeginTime(),
                condition.getEndTime(),
                condition.getOrderByFie(),
                condition.getOrderByType()
        );

        Page<Object> page = new Page<>();
        BeanUtils.copyProperties(vos, page);

        Integer total = (int) page.getTotal();

        return R.okAndTotal(vos, total);
    }

    @Override
    public R<PackSackGlobalData> packSackGlobalData(Integer userId) {

        PackSackGlobalData data = userPackSackMapper.packSackGlobalData(userId);
        return R.ok(data);
    }

    @Override
    public R<SmeltVO> smelt(SmeltRequest smeltRequest) {
        return R.ok();
    }
}
