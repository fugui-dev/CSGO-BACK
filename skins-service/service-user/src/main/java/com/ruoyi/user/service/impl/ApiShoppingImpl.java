package com.ruoyi.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.admin.mapper.TtUserBlendErcashMapper;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.mapper.TtBoxRecordsMapper;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.user.mapper.ApiShoppingMapper;
import com.ruoyi.user.service.ApiShoppingService;
import com.ruoyi.domain.other.ApiShoppingBody;
import com.ruoyi.domain.vo.ApiShoppingDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.ruoyi.common.utils.PageUtils.startPage;
import static com.ruoyi.domain.common.constant.TtboxRecordSource.MALL_EXCHANGE;

@Service
@Slf4j
public class ApiShoppingImpl implements ApiShoppingService {

    private final TtUserService userService;
    private final TtBoxRecordsMapper boxRecordsMapper;
    private final ApiShoppingMapper shoppingMapper;
    private final ISysConfigService configService;
    private final TtOrnamentMapper ornamentsMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TtUserBlendErcashMapper ttUserBlendErcashMapper;

    public ApiShoppingImpl(TtUserService userService,
                           TtBoxRecordsMapper boxRecordsMapper,
                           ApiShoppingMapper shoppingMapper,
                           ISysConfigService configService,
                           TtOrnamentMapper ornamentsMapper) {
        this.userService = userService;
        this.boxRecordsMapper = boxRecordsMapper;
        this.shoppingMapper = shoppingMapper;
        this.configService = configService;
        this.ornamentsMapper = ornamentsMapper;
    }

    @Autowired
    private TtOrnamentService ttOrnamentService;

    @Override
    public List<ApiShoppingDataVO> list(ApiShoppingBody param) {

        // 商城兑换比例
        String exchangePriceRatio = configService.selectConfigByKey("exchangePriceRatio");

        LambdaQueryWrapper<TtOrnament> wrapper = new LambdaQueryWrapper<>();

        // 名称
        wrapper.like(ObjectUtil.isNotEmpty(param.getName()), TtOrnament::getShortName, param.getName());
        // 类型
        wrapper.eq(ObjectUtil.isNotEmpty(param.getType()), TtOrnament::getType, param.getType());
        // 外观
        wrapper.eq(ObjectUtil.isNotEmpty(param.getExterior()), TtOrnament::getExterior, param.getExterior());
        // 品质
        wrapper.eq(ObjectUtil.isNotEmpty(param.getQuality()), TtOrnament::getQuality, param.getQuality());
        // 稀有程度
        wrapper.eq(ObjectUtil.isNotEmpty(param.getRarity()), TtOrnament::getRarity, param.getRarity());
        // 是否上架
        wrapper.eq(TtOrnament::getIsPutaway, "0");
        // 价格大于0
        wrapper.gt(TtOrnament::getUsePrice, 0);

        // 价格区间
        if (ObjectUtil.isNotNull(param.getMaxPrice())) {
            if (BigDecimal.ZERO.compareTo(param.getMaxPrice()) > 0) param.setMaxPrice(null);
            if (param.getMinPrice().compareTo(param.getMaxPrice()) > 0) param.setMaxPrice(null);
            if (BigDecimal.ZERO.compareTo(param.getMinPrice()) > 0) param.setMinPrice(BigDecimal.ZERO);
        }
        if (ObjectUtil.isNotNull(param.getMaxPrice())) {
            wrapper.between(TtOrnament::getUsePrice, param.getMinPrice(), param.getMaxPrice());
        }

        // 排序
        if (ObjectUtil.isEmpty(param.getSortBy())) wrapper.orderByDesc(TtOrnament::getUsePrice);
        if (ObjectUtil.isNotEmpty(param.getSortBy()) && param.getSortBy().equals(1))
            wrapper.orderByAsc(TtOrnament::getUsePrice);
        if (ObjectUtil.isNotEmpty(param.getSortBy()) && param.getSortBy().equals(2))
            wrapper.orderByDesc(TtOrnament::getUsePrice);
        if (ObjectUtil.isNotEmpty(param.getSortBy()) && param.getSortBy().equals(3))
            wrapper.orderByAsc(TtOrnament::getUpdateTime);
        if (ObjectUtil.isNotEmpty(param.getSortBy()) && param.getSortBy().equals(4))
            wrapper.orderByDesc(TtOrnament::getUpdateTime);

        startPage();
        List<TtOrnament> list = ttOrnamentService.list(wrapper);

        List<ApiShoppingDataVO> res = list.stream().map(ornament -> {
            ApiShoppingDataVO vo = new ApiShoppingDataVO();
            BeanUtil.copyProperties(ornament, vo);
            vo.setOrnamentName(ornament.getName());
            if (ObjectUtil.isNotEmpty(vo.getUsePrice())) {
                vo.setCreditsPrice(vo.getUsePrice().multiply(new BigDecimal(exchangePriceRatio)));
            }
            return vo;
        }).collect(Collectors.toList());

        return res;

        // return shoppingMapper.list(shoppingBody, Integer.parseInt(exchangePriceRatio));
    }

    @Override
    public R exchange(TtUser ttUser, Long ornamentsId) {

        TtOrnament ttOrnament = new LambdaQueryChainWrapper<>(ornamentsMapper)
                .eq(TtOrnament::getId, ornamentsId)
                .eq(TtOrnament::getIsPutaway, "0")
                .one();
        if (StringUtils.isNull(ttOrnament)) return R.fail("未查询到该饰品信息，请联系管理员！");

        String exchangePriceRatio = configService.selectConfigByKey("exchangePriceRatio");  // 弹药价格比例
        BigDecimal exchange = ttOrnament.getUsePrice().multiply(new BigDecimal(exchangePriceRatio));

        if (exchange.compareTo(ttUser.getAccountCredits()) > 0) return R.fail("您的弹药不足！");

        // 扣弹药
        LambdaUpdateWrapper<TtUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper
                .eq(TtUser::getUserId, ttUser.getUserId())
                .setSql("account_credits = account_credits - " + exchange.toString());
        userService.update(wrapper);
        ttUser = userService.getById(ttUser.getUserId());

        TtUser userById = userService.getById(ttUser.getUserId());

        // 综合消费日志
        TtUserBlendErcash blendErcash = TtUserBlendErcash.builder()
                .userId(userById.getUserId())

                .amount(BigDecimal.ZERO)
                .finalAmount(null)

                .credits(ObjectUtil.isNotEmpty(exchange) ? exchange.negate() : null)
                .finalCredits(ObjectUtil.isNotEmpty(exchange) ? userById.getAccountCredits().subtract(exchange) : null)

                .total(exchange.negate())  // 收支合计

                .type(TtAccountRecordType.OUTPUT.getCode())
                .source(TtAccountRecordSource.EXCHANGE.getCode())
                .remark(TtAccountRecordSource.EXCHANGE.getMsg())

                .createTime(new Timestamp(System.currentTimeMillis()))
                .updateTime(new Timestamp(System.currentTimeMillis()))
                .build();

        ttUserBlendErcashMapper.insert(blendErcash);

        TtBoxRecords boxRecords = TtBoxRecords.builder().build();
        boxRecords.setUserId(ttUser.getUserId());
        boxRecords.setOrnamentId(ornamentsId);
        boxRecords.setOrnamentsPrice(ttOrnament.getUsePrice());
        boxRecords.setStatus(TtboxRecordStatus.IN_PACKSACK_ON.getCode());
        boxRecords.setCreateTime(new Date());
        boxRecords.setSource(MALL_EXCHANGE.getCode());
        boxRecords.setHolderUserId(ttUser.getUserId());
        boxRecordsMapper.insert(boxRecords);

        return R.ok();
    }

    // 废弃方法
    public void parentAward(TtUser ttUser, BigDecimal exchange) {
        BigDecimal t = exchange.multiply(new BigDecimal("0.03"));
        TtUser parent = userService.getById(ttUser.getParentId());
        parent.setAccountCredits(parent.getAccountCredits().add(t));
        userService.updateUserById(parent);
        userService.insertUserCreditsRecords(parent.getUserId(), TtAccountRecordType.INPUT, TtAccountRecordSource.P_WELFARE, t, parent.getAccountCredits());
    }

    @Override
    public String integratingConversion(TtUser ttUser, BigDecimal credits) {
        if (credits.compareTo(ttUser.getAccountCredits()) > 0) return "您的弹药不足！";
        ttUser.setAccountAmount(ttUser.getAccountAmount().add(credits));
        ttUser.setAccountCredits(ttUser.getAccountCredits().subtract(credits));
        if (userService.updateById(ttUser)) {
            userService.insertUserAmountRecords(ttUser.getUserId(), TtAccountRecordType.INPUT, TtAccountRecordSource.INTEGRATING_CONVERSION, credits, ttUser.getAccountAmount());
            userService.insertUserCreditsRecords(ttUser.getUserId(), TtAccountRecordType.OUTPUT, TtAccountRecordSource.INTEGRATING_CONVERSION, credits.negate(), ttUser.getAccountCredits());
            return "";
        }
        return "弹药转换异常！";
    }
}
