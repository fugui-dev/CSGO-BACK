package com.ruoyi.thirdparty.common.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.admin.service.TtOrnamentYYService;
import com.ruoyi.admin.service.TtYyOrnamentsService;
import com.ruoyi.common.constant.ThirdParty.Platform;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.DeliveryOrderStatus;
import com.ruoyi.domain.common.constant.PartyType;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.dto.deliver.TradeBuyParam;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.delivery.TtDeliveryRecord;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtYYOrnaments;
import com.ruoyi.domain.vo.AvailableMarketOrnamentVO;
import com.ruoyi.domain.vo.OnSaleOrnamentVO;
import com.ruoyi.domain.vo.queryTemplateSaleByCategoryDataVO;
import com.ruoyi.domain.vo.queryTemplateSaleByCategoryVO;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.common.controller.DeliverGoodsController.GetAvailableMarketListParam;
import com.ruoyi.thirdparty.common.service.DeliverGoodsService;
import com.ruoyi.thirdparty.yyyouping.service.yyyoupingService;
import com.ruoyi.thirdparty.yyyouping.utils.YYClient;
import com.ruoyi.thirdparty.yyyouping.utils.common.YYResult;
import com.ruoyi.thirdparty.zbt.param.NormalBuyParamV2DTO;
import com.ruoyi.thirdparty.zbt.param.OrderBuyDetailParams;
import com.ruoyi.thirdparty.zbt.param.ProductListParams;
import com.ruoyi.thirdparty.zbt.param.QuickBuyParamV2DTO;
import com.ruoyi.thirdparty.zbt.result.ResultZbt;
import com.ruoyi.thirdparty.zbt.result.buy.OpenBuyResultDTO;
import com.ruoyi.thirdparty.zbt.result.order.OrderBuyDetailDTO;
import com.ruoyi.thirdparty.zbt.result.product.AvailableMarket;
import com.ruoyi.thirdparty.zbt.result.product.AvailableMarketList;
import com.ruoyi.thirdparty.zbt.service.ZBTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.ruoyi.domain.common.constant.DeliveryOrderStatus.*;

@Service
@Slf4j
public class DeliverGoodsServiceImpl implements DeliverGoodsService {

    private final ISysConfigService configService;
    private final ZBTService zbtService;
    private final TtUserMapper userMapper;
    private final TtOrnamentMapper ornamentsMapper;
    private final TtBoxRecordsMapper boxRecordsMapper;
    private final TtDeliveryRecordMapper deliveryRecordMapper;

    //@Autowired
    private YYClient yyClient;

    @Autowired
    private TtYyOrnamentsService ttYyOrnamentsService;

    @Autowired
    private TtOrnamentService ttOrnamentService;

    @Autowired
    private TtOrnamentYYService ttOrnamentYYService;

    @Autowired
    private TtOrnamentsYYMapper ttOrnamentsYYMapper;

    @Autowired
    private yyyoupingService yyyoupingService;

    public DeliverGoodsServiceImpl(ISysConfigService configService,
                                   ZBTService zbtService,
                                   TtUserMapper userMapper,
                                   TtOrnamentMapper ornamentsMapper,
                                   TtBoxRecordsMapper boxRecordsMapper,
                                   TtDeliveryRecordMapper deliveryRecordMapper) {
        this.configService = configService;
        this.zbtService = zbtService;
        this.userMapper = userMapper;
        this.ornamentsMapper = ornamentsMapper;
        this.boxRecordsMapper = boxRecordsMapper;
        this.deliveryRecordMapper = deliveryRecordMapper;
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public R getAvailableMarketList(Long ornamentsId, Integer partyType) {

        // 从平台获取的数据集
        List<AvailableMarket> resList = null;
        if (partyType.equals(PartyType.ZBT.getCode())){

            // 获取饰品信息
            TtOrnament ornament = new LambdaQueryChainWrapper<>(ornamentsMapper)
                    .eq(TtOrnament::getId, ornamentsId)
                    .one();
            // 构建查询参数
            ProductListParams apiParm = ProductListParams.builder()
                    .itemId(String.valueOf(ornament.getId()))
                    .build();

            // 查询接口
            ResultZbt<AvailableMarketList> productList = zbtService.productList(apiParm);
            if (!productList.getSuccess()) {
                log.error("调用ZBT获取某一个饰品的所有在售接口异常，请检查代码是否正确！");
                return null;
            }

            // 过滤可用数据
            AvailableMarketList availableMarketList = productList.getData();

            resList = availableMarketList.getList();

        }else if (partyType.equals(PartyType.YY_YOU_PING.getCode())){

            // 获取饰品信息
            TtOrnament ornament = new LambdaQueryChainWrapper<>(ornamentsMapper)
                    .eq(TtOrnament::getId, ornamentsId)
                    .one();
            // 构建查询参数
            ProductListParams apiParm = ProductListParams.builder()
                    .itemId(String.valueOf(ornament.getId()))
                    .build();

            // 查询接口
            ResultZbt<AvailableMarketList> productList = zbtService.productList(apiParm);
            if (!productList.getSuccess()) {
                log.error("调用ZBT获取某一个饰品的所有在售接口异常，请检查代码是否正确！");
                return null;
            }

            // 过滤可用数据
            AvailableMarketList availableMarketList = productList.getData();

            resList = availableMarketList.getList();
        }

        if (ObjectUtil.isEmpty(resList)) return R.fail("平台没有该物品在售信息。");

        // 构建返回结果
        List<OnSaleOrnamentVO> resultList = new ArrayList<>();

        // 提取可用数据
        for (AvailableMarket availableMarket : resList) {
            OnSaleOrnamentVO data = OnSaleOrnamentVO.builder().build();
            data.setId(availableMarket.getId());
            data.setDelivery(availableMarket.getDelivery());
            data.setItemName(availableMarket.getItemName());
            data.setImageUrl(availableMarket.getImageUrl());
            data.setCnyPrice(availableMarket.getCnyPrice());
            data.setPrice(availableMarket.getPrice());
            resultList.add(data);
        }
        return R.ok(resultList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R tradeBuy(TradeBuyParam param) {
        // 获取提货订单数据
        TtDeliveryRecord ttDeliveryRecord = deliveryRecordMapper.selectById(param.getDeliveryRecordId());
        // 获取用户数据
        TtUser ttUser = userMapper.selectById(ttDeliveryRecord.getUserId());
        // 初始化数据
        String transactionLink = ttUser.getTransactionLink();
        if (StringUtils.isEmpty(transactionLink)) {
            return R.fail("该用户未绑定Steam交易链接！");
        }
        String outTradeNo = ttDeliveryRecord.getOutTradeNo();

        if (param.getPartyType().equals(PartyType.ZBT.getCode())){

            // 构建购买参数
            NormalBuyParamV2DTO normalBuyParamV2DTO = new NormalBuyParamV2DTO();
            normalBuyParamV2DTO.setOutTradeNo(outTradeNo);
            normalBuyParamV2DTO.setProductId(param.getProductId().toString());
            normalBuyParamV2DTO.setTradeUrl(transactionLink);

            // 购买
            ResultZbt<OpenBuyResultDTO> tradeBuy = zbtService.tradeBuy(normalBuyParamV2DTO);
            if (!tradeBuy.getSuccess()) {
                return R.fail(tradeBuy.getErrorMsg());
            }

            // 获取结果数据
            OpenBuyResultDTO data = tradeBuy.getData();
            System.err.println(data);
            // 更新提货订单数据
            ttDeliveryRecord.setBuyPrice(data.getBuyPrice());
            ttDeliveryRecord.setThirdpartyDelivery(data.getDelivery());
            ttDeliveryRecord.setOrderId(data.getOrderId());
            ttDeliveryRecord.setStatus(DELIVERY_AFTER.getCode()); // 发货订单状态 1待发货
            ttDeliveryRecord.setMessage(DELIVERY_AFTER.getMsg());
            ttDeliveryRecord.setUpdateBy(SecurityUtils.getUsername());
            ttDeliveryRecord.setUpdateTime(DateUtils.getNowDate());
            if (deliveryRecordMapper.updateById(ttDeliveryRecord) > 0) {
                return R.ok();
            }
            return R.fail("更新提货订单数据异常！");

        }else if (param.getPartyType().equals(PartyType.YY_YOU_PING.getCode())){
            return R.ok("暂未开放。");
        }else {
            return R.fail("非法的第三方平台代码");
        }

    }

    @Override
    @Transactional
    public String synchronousStatus(String outTradeNo) {
        // 获取提货数据
        TtDeliveryRecord ttDeliveryRecord = new LambdaQueryChainWrapper<>(deliveryRecordMapper)
                .eq(TtDeliveryRecord::getOutTradeNo, outTradeNo).one();
        if (10 == ttDeliveryRecord.getStatus() || 11 == ttDeliveryRecord.getStatus()) {
            return "";
        }
        // 构建查询参数
        OrderBuyDetailParams orderBuyDetailParams = new OrderBuyDetailParams();
        orderBuyDetailParams.setOutTradeNo(ttDeliveryRecord.getOutTradeNo());
        ResultZbt<OrderBuyDetailDTO> orderBuyDetail = zbtService.orderBuyDetail(orderBuyDetailParams);
        if (!orderBuyDetail.getSuccess()) {
            return "查询购买订单详情V2失败";
        }
        // 获取结果数据
        OrderBuyDetailDTO data = orderBuyDetail.getData();
        DeliveryOrderStatus status = data.getStatus();

        log.info("查询订单结果【{}】", data);
        if (status.equals(ORDER_CANCEL)){
            ttDeliveryRecord.setStatus(status.getCode());
            ttDeliveryRecord.setMessage(data.getFailedDesc());
            ttDeliveryRecord.setUpdateBy(SecurityUtils.getUsername());
            ttDeliveryRecord.setUpdateTime(DateUtils.getNowDate());
            // 将用户提货饰品返回用户背包
            TtBoxRecords ttBoxRecords = new LambdaQueryChainWrapper<>(boxRecordsMapper)
                    .eq(TtBoxRecords::getId, ttDeliveryRecord.getBoxRecordsId())
                    .eq(TtBoxRecords::getHolderUserId, ttDeliveryRecord.getUserId())
                    .eq(TtBoxRecords::getOrnamentId, ttDeliveryRecord.getOrnamentId())
                    .eq(TtBoxRecords::getStatus, "2")   // 状态 2已提取
                    .one();
            if (StringUtils.isNull(ttBoxRecords)) {
                return "未查询到该用户背包的饰品信息！";
            }
            ttBoxRecords.setStatus(TtboxRecordStatus.IN_PACKSACK_ON.getCode());   // 状态 0背包
            ttBoxRecords.setUpdateTime(DateUtils.getNowDate());
            boxRecordsMapper.updateById(ttBoxRecords);
            if (deliveryRecordMapper.updateById(ttDeliveryRecord) > 0) {
                return "";
            }
        } else if (status.equals(ORDER_COMPLETE)){
            ttDeliveryRecord.setMessage(status.getMsg());
            // 发送发货成功通知
            Map<String, String> noticeMap = new HashMap<>();
            noticeMap.put("userId", ttDeliveryRecord.getUserId().toString());
            noticeMap.put("title", "发货成功通知");
            noticeMap.put("content", "发货成功，请注意查收。");
            noticeMap.put("createTime", DateUtils.getTime());
            rabbitTemplate.convertAndSend("notice_queue", noticeMap);
        }else {
            return "不匹配的ZBT响应内容！";
        }

        ttDeliveryRecord.setStatus(status.getCode());
        ttDeliveryRecord.setUpdateBy(SecurityUtils.getUsername());
        ttDeliveryRecord.setUpdateTime(DateUtils.getNowDate());
        if (deliveryRecordMapper.updateById(ttDeliveryRecord) > 0) return "";
        return "同步提货订单数据异常！";
    }

    @Override
    public void autoDelivery(Integer userId) {
        // 获取需要自动发货的数据
        List<TtDeliveryRecord> deliveryRecordList = new LambdaQueryChainWrapper<>(deliveryRecordMapper)
                .eq(TtDeliveryRecord::getUserId, userId)
                .eq(TtDeliveryRecord::getDelivery, "2") // 网站发货模式（1人工发货 2自动发货 3主播号提取）
                .eq(TtDeliveryRecord::getStatus, "0")   // 发货订单状态（0发起提货 1待发货 3待收货 10订单完成 11订单取消）
                .list();
        if (StringUtils.isNull(deliveryRecordList) || deliveryRecordList.isEmpty()) {
            log.info("{}号用户发起提货，无自动发货饰品，请在后台手动发货！", userId);
            return;
        }

        // 饰品购买价格溢价率
        String buyPricePremiumRateStr = configService.selectConfigByKey("buyPricePremiumRate");
        BigDecimal buyPricePremiumRate = new BigDecimal(buyPricePremiumRateStr);
        // ZBT平台币种汇率
        String ZBTParitiesStr = configService.selectConfigByKey("ZBTParities");
        BigDecimal ZBTParities = new BigDecimal(ZBTParitiesStr);

        // 快速购买
        for (TtDeliveryRecord ttDeliveryRecord : deliveryRecordList) {
            // 获取用户信息
            TtUser ttUser = userMapper.selectById(ttDeliveryRecord.getUserId());
            // 获取饰品信息
            TtOrnament ttOrnament = ornamentsMapper.selectById(ttDeliveryRecord.getOrnamentId());
            // 计算可接受最高价
            BigDecimal ornamentsPrice = ttDeliveryRecord.getOrnamentsPrice();   // 饰品价格
            BigDecimal price = ornamentsPrice.divide(ZBTParities, 2, RoundingMode.HALF_UP).multiply(buyPricePremiumRate);
            // 构建购买参数
            QuickBuyParamV2DTO quickBuyParamV2DTO = new QuickBuyParamV2DTO();
            // TODO: 2024/3/30  ttOrnament.getId()
            quickBuyParamV2DTO.setItemId(String.valueOf(ttOrnament.getId()));
            quickBuyParamV2DTO.setMaxPrice(price.toString()); // 购买a可以接受的最高价格(T币)
            quickBuyParamV2DTO.setOutTradeNo(ttDeliveryRecord.getOutTradeNo());
            quickBuyParamV2DTO.setTradeUrl(ttUser.getTransactionLink());
            ResultZbt<OpenBuyResultDTO> tradeQuickBuy = zbtService.tradeQuickBuy(quickBuyParamV2DTO);
            if (!tradeQuickBuy.getSuccess()) {
                log.error("快速购买接口v2异常，订单号：{}", ttDeliveryRecord.getOutTradeNo());
                continue;
            }
            // 获取结果数据
            OpenBuyResultDTO data = tradeQuickBuy.getData();
            // 更新提货订单数据
            ttDeliveryRecord.setBuyPrice(data.getBuyPrice());
            ttDeliveryRecord.setThirdpartyDelivery(data.getDelivery());
            ttDeliveryRecord.setOrderId(data.getOrderId());
            ttDeliveryRecord.setStatus(DELIVERY_BEFORE.getCode()); // 发货订单状态 1待发货
            ttDeliveryRecord.setMessage("等待发货");
            ttDeliveryRecord.setUpdateBy("自动发货");
            ttDeliveryRecord.setUpdateTime(DateUtils.getNowDate());
            // 更新状态
            if (deliveryRecordMapper.updateById(ttDeliveryRecord) > 0) {
                log.info("订单'{}'自动发货成功，等待卖家发货中......", ttDeliveryRecord.getOutTradeNo());
            }
        }

    }

    public List<AvailableMarketOrnamentVO> ZBTGetAvailable(String marketHashName, ProductListParams productListParams){

        LambdaQueryWrapper<TtOrnament> ttOrnamentsQuery = new LambdaQueryWrapper<>();
        ttOrnamentsQuery.eq(TtOrnament::getMarketHashName, marketHashName);
        TtOrnament ornament = ttOrnamentService.getOne(ttOrnamentsQuery);

        // zbt所有数据
        productListParams.setItemId(String.valueOf(ornament.getId()));
        ResultZbt<AvailableMarketList> availableZbt = zbtService.productList(productListParams);

        if (!availableZbt.getSuccess()){
            log.warn(availableZbt.getErrorMsg());
            return null;
        }

        AvailableMarketList data = availableZbt.getData();
        return data.getList().stream().map(item->{
            return AvailableMarketOrnamentVO.builder()
                    .platform(Platform.ZBT.getCode())
                    .itemId(item.getItemId())
                    .delivery(item.getDelivery())
                    .itemName(item.getItemName())
                    .imageUrl(item.getImageUrl())
                    .cnyPrice(item.getCnyPrice())
                    .price(item.getPrice())
                    .build();
        }).collect(Collectors.toList());

    }

    public List<AvailableMarketOrnamentVO> YYGetAvailable(String marketHashName, ProductListParams productListParams){

        LambdaQueryWrapper<TtYYOrnaments> ttYYOrnamentsQuery = new LambdaQueryWrapper<>();
        ttYYOrnamentsQuery.eq(TtYYOrnaments::getHashName,marketHashName);
        TtYYOrnaments yyOrnament = ttYyOrnamentsService.getOne(ttYYOrnamentsQuery);

        HashMap<String, Object> map = new HashMap<>();
        JSONObject o = new JSONObject();
        o.put("templateId",yyOrnament.getId());
        log.info("obj="+o.toString());
        map.put("requestList", ListUtil.of(o));
        YYResult yyResult = yyyoupingService.yyApiBatchGetOnSaleCommodityInfo(map);

        Object dataJson = yyResult.getData();
        ObjectMapper objectMapper = new ObjectMapper();
        queryTemplateSaleByCategoryDataVO dataVO;
        try {
            dataVO = objectMapper.readValue(JSON.toJSONString(dataJson), queryTemplateSaleByCategoryDataVO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<queryTemplateSaleByCategoryVO> voList = dataVO.getSaleTemplateByCategoryResponseList();

        List<AvailableMarketOrnamentVO> collect = voList.stream().map(item -> {
            return AvailableMarketOrnamentVO.builder()
                    .platform(Platform.YY.getCode())
                    .templateId(item.getTemplateId())
                    .imageUrl(item.getIconUrl())
                    .build();
        }).collect(Collectors.toList());

        return null;
    }

    @Override
    public List<AvailableMarketOrnamentVO> getAvailableMarketListByHashName(String marketHashName, ProductListParams productListParams) {

        // zbt所有数据
        List<AvailableMarketOrnamentVO> ZBTGetAvailable = ZBTGetAvailable(marketHashName, productListParams);

        // yy所有数据
        List<AvailableMarketOrnamentVO> YYGetAvailable = YYGetAvailable(marketHashName, productListParams);

        // 合并所有数据

        return null;

        // // 获取饰品itemId
        // TtOrnaments ornaments = new LambdaQueryChainWrapper<>(ornamentsMapper).eq(TtOrnaments::getId, ornamentsId).one();
        // // 构建查询参数
        // productListParams.setItemId(ornaments.getItemId());
        // ResultZbt<AvailableMarketList> productList = zbtService.productList(productListParams);  // 查询接口
        // if (!productList.getSuccess()) {
        //     log.error("调用ZBT获取某一个饰品的所有在售接口异常，请检查代码是否正确！");
        //     return null;
        // }
        // AvailableMarketList availableMarketList = productList.getData();
        // // 查询到的数据
        // List<AvailableMarket> list = availableMarketList.getList();
        // // 构建返回结果
        // List<AvailableMarketOrnamentsListDataVO> resultList = new ArrayList<>();
        // // 提取可用数据
        // for (AvailableMarket availableMarket : list) {
        //     AvailableMarketOrnamentsListDataVO data = AvailableMarketOrnamentsListDataVO.builder().build();
        //     data.setId(availableMarket.getId());
        //     data.setDelivery(availableMarket.getDelivery());
        //     data.setItemName(availableMarket.getItemName());
        //     data.setImageUrl(availableMarket.getImageUrl());
        //     data.setCnyPrice(availableMarket.getCnyPrice());
        //     data.setPrice(availableMarket.getPrice());
        //     resultList.add(data);
        // }
        // return resultList;
    }

    @Override
    public R getAvailableMarketList(GetAvailableMarketListParam param) {

        // 返回结果
        List<OnSaleOrnamentVO> resultList = new ArrayList<>();

        // 获取饰品信息（这里暂时不支持批量查询）
        List<TtOrnament> list = new LambdaQueryChainWrapper<>(ornamentsMapper)
                .in(TtOrnament::getId, param.getOrnamentsId().get(0))
                .list();

        if (param.getPartyType().equals(PartyType.ZBT.getCode())){

            for (TtOrnament item: list){

                if (ObjectUtil.isEmpty(item.getZbtId())) continue;

                // 构建查询参数
                ProductListParams apiParm = ProductListParams.builder()
                        .itemId(String.valueOf(item.getZbtId()))
                        .build();

                // 查询接口
                ResultZbt<AvailableMarketList> productList = zbtService.productList(apiParm);
                if (!productList.getSuccess()) {
                    log.error("调用ZBT获取某一个饰品的所有在售接口异常，请检查代码是否正确！");
                    return R.fail("调用ZBT获取某一个饰品的所有在售接口异常");
                }

                // 过滤可用数据
                AvailableMarketList availableMarketList = productList.getData();

                List<AvailableMarket> list1 = availableMarketList.getList();

                if (ObjectUtil.isEmpty(list1)) {
                    return R.fail("平台没有该物品在售信息。");
                }

                // 提取可用数据
                for (AvailableMarket availableMarket : list1) {
                    OnSaleOrnamentVO data = OnSaleOrnamentVO.builder().build();
                    data.setPartyType(PartyType.ZBT.getCode());
                    data.setId(availableMarket.getId());
                    data.setDelivery(availableMarket.getDelivery());
                    data.setItemName(availableMarket.getItemName());
                    data.setImageUrl(availableMarket.getImageUrl());
                    data.setCnyPrice(availableMarket.getCnyPrice());
                    data.setPrice(availableMarket.getPrice());
                    resultList.add(data);
                }
            }

        } else if (param.getPartyType().equals(PartyType.YY_YOU_PING.getCode())){

            // 构建查询参数
            HashMap<String, Object> apiParam = new HashMap<>();

            ArrayList<Map<String,String>> ids = new ArrayList<>();
            for (TtOrnament item : list){
                Map<String, String> i = MapUtil.builder("templateId", String.valueOf(item.getYyyoupingId())).build();
                ids.add(i);
            }

            apiParam.put("requestList",ids);

            // 查询接口
            YYResult yyResult = yyyoupingService.yyApiBatchGetOnSaleCommodityInfo(apiParam);
            if (!yyResult.isSuccess()) {
                log.error("调用ZBT获取某一个饰品的所有在售接口异常，请检查代码是否正确！");
                return R.fail("调用YY获取某一个饰品的所有在售接口异常");
            }

            // 过滤可用数据
            List<String> data = JSONUtil.toList(JSON.toJSONString(yyResult.getData()), String.class);
            resultList = data.stream().map(item->{
                cn.hutool.json.JSONObject obj = JSONUtil.parseObj(item);
                cn.hutool.json.JSONObject saleTemplateResponse = obj.get("saleTemplateResponse", cn.hutool.json.JSONObject.class);
                cn.hutool.json.JSONObject saleCommodityResponse = obj.get("saleCommodityResponse", cn.hutool.json.JSONObject.class);

                return OnSaleOrnamentVO.builder()
                        .partyType(PartyType.YY_YOU_PING.getCode())
                        .id(saleTemplateResponse.get("templateId",String.class))
                        .itemName(saleCommodityResponse.get("name",String.class))
                        .imageUrl(list.get(0).getImageUrl())
                        .price(saleCommodityResponse.get("minSellPrice",BigDecimal.class))
                        .build();
            }).collect(Collectors.toList());

        }

        return R.ok(resultList);
    }
}
