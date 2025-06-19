package com.ruoyi.thirdparty.zbt.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.dto.zbt.OrnamentZBT;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.system.mapper.SysDictDataMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.zbt.param.SearchParams;
import com.ruoyi.thirdparty.zbt.param.SearchPriceBatchParams;
import com.ruoyi.thirdparty.zbt.result.ResultZbt;
import com.ruoyi.thirdparty.zbt.result.product.OrnPriceInfo;
import com.ruoyi.thirdparty.zbt.result.product.SearchData;
import com.ruoyi.thirdparty.zbt.service.ZBTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("OrnamentsPullTask")
public class OrnamentsPullTask {

    private Set<String> removeDuplicates = new HashSet<>();
    private final ZBTService zbtService;
    private final SysDictDataMapper dictDataMapper;
    private final ISysConfigService configService;
    private final TtOrnamentService ornamentsService;

    public OrnamentsPullTask(ZBTService zbtService,
                             SysDictDataMapper dictDataMapper,
                             ISysConfigService configService,
                             TtOrnamentService ornamentsService) {
        this.zbtService = zbtService;
        this.dictDataMapper = dictDataMapper;
        this.configService = configService;
        this.ornamentsService = ornamentsService;
    }

    public void getOrnamentsData() {
        log.info("拉取最新饰品数据任务执行开始...");
        long startTime = System.currentTimeMillis();
        List<String> itemIdList = ornamentsService.selectOrnamentsMarketHashNameList();
        if (!itemIdList.isEmpty()) removeDuplicates = new HashSet<>(itemIdList);
        log.info("数据库总条数：" + removeDuplicates.size());
        List<SysDictData> type = dictDataMapper.selectDictDataByType("ornaments_type");

        if (ObjectUtils.isEmpty(type)) return;
        Map<String, String> typeMap = objectToMap(type);

        Set<String> tempRemoveDuplicates = new HashSet<>();
        List<OrnamentZBT> ZBTOrnamentsDataList = new ArrayList<>();

        for (Map.Entry<String, String> entry : typeMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            SearchParams searchParams = new SearchParams();
            searchParams.setLimit("200");
            searchParams.setPage("1");
            searchParams.setCategory(key);
            ResultZbt<SearchData> searchResult = zbtService.search(searchParams);
            if (!searchResult.getSuccess()) return;
            List<OrnamentZBT> list = searchResult.getData().getList().stream()
                    .filter(searchList -> !tempRemoveDuplicates.contains(searchList.getMarketHashName()))
                    .peek(searchList -> tempRemoveDuplicates.add(searchList.getMarketHashName()))
                    .collect(Collectors.toList());
            ZBTOrnamentsDataList.addAll(list);
            List<OrnamentZBT> ZBTOrnamentsDataListByType = searchResult.getData().getList().stream()
                    .filter(searchList -> !removeDuplicates.contains(searchList.getMarketHashName()))
                    .peek(searchList -> removeDuplicates.add(searchList.getMarketHashName()))
                    .collect(Collectors.toList());
            Integer total = searchResult.getData().getTotal();
            int pageSize = 200;
            int pageNumCount = (total % pageSize == 0) ? total / pageSize : total / pageSize + 1;
            for (int i = 2; i <= pageNumCount; i++) {
                searchParams.setPage(String.valueOf(i));
                if (i == pageNumCount) searchParams.setLimit(String.valueOf(total % pageSize));
                searchResult = zbtService.search(searchParams);
                if (!searchResult.getSuccess()) return;
                list = searchResult.getData().getList().stream().filter(searchList -> !tempRemoveDuplicates.contains(searchList.getMarketHashName()))
                        .peek(searchList -> tempRemoveDuplicates.add(searchList.getMarketHashName())).collect(Collectors.toList());
                ZBTOrnamentsDataList.addAll(list);
                List<OrnamentZBT> typeList = searchResult.getData().getList().stream().filter(searchList -> !removeDuplicates.contains(searchList.getMarketHashName()))
                        .peek(searchList -> removeDuplicates.add(searchList.getMarketHashName())).collect(Collectors.toList());
                ZBTOrnamentsDataListByType.addAll(typeList);
            }
            batchInsertOrnaments(ZBTOrnamentsDataListByType, value);
        }
        LambdaQueryWrapper<TtOrnament> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(TtOrnament::getIsProprietaryProperty, "1");
        Map<String, TtOrnament> ornamentsMap =
                ornamentsService.list(wrapper).stream().collect(Collectors.toMap(TtOrnament::getMarketHashName, ornaments -> ornaments));
        log.info("扎比特数据条数：" + ZBTOrnamentsDataList.size());
        batchUpdateOrnamentsPrice(ZBTOrnamentsDataList, ornamentsMap);
        batchSoldOutOrnaments(ZBTOrnamentsDataList, ornamentsMap);
        // 获取方法结束时间
        long endTime = System.currentTimeMillis();
        // 计算方法执行时间
        long duration = endTime - startTime;
        log.info("方法执行时间（秒）：" + duration / 1000.0);
        log.info("拉取最新饰品数据任务执行结束...");

        syncZbtPrice();

    }

    public void syncZbtPrice(){

        List<TtOrnament> ornaments = ornamentsService.list(Wrappers.lambdaQuery(TtOrnament.class)
                .select(TtOrnament::getMarketHashName));


        log.info("开始同步ZTB价格...");
        String usePricePremiumRateStr = configService.selectConfigByKey("usePricePremiumRate");
        BigDecimal usePricePremiumRate = new BigDecimal(usePricePremiumRateStr);
        String ZBTParitiesStr = configService.selectConfigByKey("ZBTParities");
        BigDecimal ZBTParities = new BigDecimal(ZBTParitiesStr);

        int total = ornaments.size() - 1; //总条数
        ArrayList<String> hashNameList = new ArrayList<>(200); //存放hashName的集合
        int calcNum = 0; //计数
        for (int i = 0; i <= total; i++) {

            hashNameList.add(ornaments.get(i).getMarketHashName());
            calcNum++;

            //满200后就同步一次
            if (calcNum == 200 || i == total){
                ResultZbt<List<OrnPriceInfo>> resultZbt = zbtService.queryPriceBatch(new SearchPriceBatchParams(730, hashNameList));
                if (resultZbt.getSuccess()){
                    try {
                        List<OrnPriceInfo> infoList = resultZbt.getData();

                        Set<TtOrnament> updatePriceSet = infoList.stream()
                                .filter(info -> info.getPrice()!=null).map(info -> {
                            TtOrnament ornament = new TtOrnament();
                            ornament.setZbtId(info.getId());
                            ornament.setMarketHashName(info.getMarketHashName());
                            ornament.setPrice(info.getPrice());
                            ornament.setUsePrice(info.getPrice().multiply(usePricePremiumRate).multiply(ZBTParities));
                            //如果没有价格，下架饰品
                            if (info.getPrice() != null && info.getPrice().compareTo(BigDecimal.ZERO) <= 0){
                                ornament.setIsPutaway("1");
                            }
                            return ornament;
                        }).collect(Collectors.toSet());

                        boolean updatePriceFlag = ornamentsService.updateBatchById(updatePriceSet);
                        log.info("同步饰品价格200个成功？【{}】", updatePriceFlag);

                    }catch (Exception e){
                        log.error("同步价格出现异常！", e);
                    }

                }
                //清空技术和list
                calcNum = 0;
                hashNameList = new ArrayList<String>(200);

            }



        }
        log.info("ZTB价格同步完成...");

    }

    private void batchSoldOutOrnaments(List<OrnamentZBT> ZBTOrnamentsDataList, Map<String, TtOrnament> ornamentsMap) {
        List<TtOrnament> ornamentsList = ornamentsMap.entrySet().stream()
                .filter(entry -> ZBTOrnamentsDataList.stream().noneMatch(searchList -> searchList.getMarketHashName().equals(entry.getKey())))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        ornamentsList = ornamentsList.stream().peek(ttOrnaments -> {
            ttOrnaments.setUsePrice(BigDecimal.ZERO);
            ttOrnaments.setPrice(BigDecimal.ZERO);
            ttOrnaments.setUpdateTime(DateUtils.getNowDate());
            ttOrnaments.setRemark("ZBT市场已下架");
            ttOrnaments.setIsPutaway("1");
        }).collect(Collectors.toList());
        log.info("下架条数：" + ornamentsList.size());
//        boolean isSuccess = ornamentsService.updateBatchById(ornamentsList, 1);
        for (TtOrnament ttOrnament : ornamentsList) {
            try {

                boolean update = ornamentsService.updateById(ttOrnament);
                log.info("下架饰品【{}】成功！", ttOrnament.getMarketHashName());

            }catch (Exception e){
                log.error("下架饰品【{}】失败！", ttOrnament, e);
            }

        }
//        if (isSuccess) {
//            log.info("批量下架饰品成功！");
//        } else {
//            log.info("批量下架饰品失败！");
//        }
    }

    private void batchUpdateOrnamentsPrice(List<OrnamentZBT> list, Map<String, TtOrnament> ornamentsMap) {
        String usePricePremiumRateStr = configService.selectConfigByKey("usePricePremiumRate");
        BigDecimal usePricePremiumRate = new BigDecimal(usePricePremiumRateStr);
        String ZBTParitiesStr = configService.selectConfigByKey("ZBTParities");
        BigDecimal ZBTParities = new BigDecimal(ZBTParitiesStr);
        List<OrnamentZBT> updateList = list.stream().distinct().filter(ornamentZBT -> {
            OrnamentZBT.PriceInfo priceInfo = ornamentZBT.getPriceInfo();
            TtOrnament ornament = ornamentsMap.get(ornamentZBT.getMarketHashName());
            BigDecimal priceData;
            if (!Objects.isNull(ornament)) {
                priceData = ornament.getPrice();
            } else {
                priceData = BigDecimal.ZERO;
            }
            BigDecimal price;
            if (!Objects.isNull(priceInfo)) {
                price = priceInfo.getPrice();
            } else {
                price = BigDecimal.ZERO;
            }
            Integer quantityData;
            if (!Objects.isNull(ornament)) {
                quantityData = ornament.getQuantity();
            } else {
                quantityData = 0;
            }
            Integer quantity;
            if (!Objects.isNull(priceInfo)) {
                quantity = priceInfo.getQuantity();
            } else {
                quantity = 0;
            }
            return (priceData == null || quantityData == null) ||
                    (priceInfo == null) ||
                    (ornament == null) ||
                    (priceData.compareTo(price) != 0 || !Objects.equals(quantityData, quantity));
        }).collect(Collectors.toList());
        List<TtOrnament> ttOrnamentList = new ArrayList<>();
        updateList.forEach(searchList -> {
            String marketHashName = searchList.getMarketHashName();
            OrnamentZBT.PriceInfo priceInfo = searchList.getPriceInfo();
            BigDecimal price = StringUtils.isNull(priceInfo) ? BigDecimal.ZERO : priceInfo.getPrice();
            Integer quantity = StringUtils.isNull(priceInfo) ? 0 : priceInfo.getQuantity();
            TtOrnament ttOrnament = ornamentsMap.get(marketHashName);
            if (!Objects.isNull(ttOrnament)) {
                ttOrnament.setUsePrice(price.multiply(ZBTParities).multiply(usePricePremiumRate));
                ttOrnament.setPrice(price);
                ttOrnament.setQuantity(quantity);
                ttOrnament.setUpdateTime(DateUtils.getNowDate());
                ttOrnamentList.add(ttOrnament);
            }
        });
        log.info("更新条数：" + ttOrnamentList.size());
        for (TtOrnament ornament : ttOrnamentList) {

            try {
                boolean update = ornamentsService.updateById(ornament);
                log.info("更新饰品价格成功【{}】", ornament.getMarketHashName());
            }catch (Exception e){
                log.info("更新饰品价格失败【{}】", ornament, e);

            }


//        if (isSuccess) {
//            log.info("批量更新饰品价格成功！");
//        } else {
//            log.info("批量更新饰品价格失败！");
//        }

        }
//        boolean isSuccess = ornamentsService.saveOrUpdateBatch(ttOrnamentList, 1);
//        if (isSuccess) {
//            log.info("批量更新饰品价格成功！");
//        } else {
//            log.info("批量更新饰品价格失败！");
//        }
    }

    // 批量插入饰品
    private void batchInsertOrnaments(List<OrnamentZBT> ZBTOrnamentsDataListByType, String ornamentsTypeValue) {
        List<SysDictData> rarity = dictDataMapper.selectDictDataByType("ornaments_rarity");
        if (ObjectUtils.isEmpty(rarity)) return;
        List<SysDictData> exterior = dictDataMapper.selectDictDataByType("ornaments_exterior");
        if (ObjectUtils.isEmpty(exterior)) return;
        List<SysDictData> quality = dictDataMapper.selectDictDataByType("ornaments_quality");
        if (ObjectUtils.isEmpty(quality)) return;
        String usePricePremiumRateStr = configService.selectConfigByKey("usePricePremiumRate");
        BigDecimal usePricePremiumRate = new BigDecimal(usePricePremiumRateStr);
        String ZBTParitiesStr = configService.selectConfigByKey("ZBTParities");
        BigDecimal ZBTParities = new BigDecimal(ZBTParitiesStr);
        Map<String, String> rarityMap = objectToMap(rarity);
        Map<String, String> exteriorMap = objectToMap(exterior);
        Map<String, String> qualityMap = objectToMap(quality);
        List<TtOrnament> ornamentsList = new ArrayList<>();
        ZBTOrnamentsDataListByType.forEach(searchList -> {
            OrnamentZBT.PriceInfo priceInfo = searchList.getPriceInfo();
            BigDecimal price = StringUtils.isNull(priceInfo) ? BigDecimal.ZERO : priceInfo.getPrice();
            Integer quantity = StringUtils.isNull(priceInfo) ? 0 : priceInfo.getQuantity();
            String rarityValue = rarityMap.get(searchList.getRarity());
            String exteriorValue = exteriorMap.get(searchList.getExterior());
            String qualityValue = qualityMap.get(searchList.getQuality());
            TtOrnament ornaments = TtOrnament.builder().build();
            ornaments.setName(searchList.getItemName());
            ornaments.setUsePrice(price.multiply(ZBTParities).multiply(usePricePremiumRate));
            ornaments.setImageUrl(searchList.getImageUrl());
            ornaments.setMarketHashName(searchList.getMarketHashName());
            ornaments.setId(searchList.getItemId());
            ornaments.setZbtId(searchList.getItemId());
            ornaments.setPrice(price);
            ornaments.setQuantity(quantity);
            ornaments.setShortName(searchList.getShortName());
            ornaments.setType(ornamentsTypeValue);
            ornaments.setTypeName(searchList.getTypeName());
            ornaments.setQuality(qualityValue);
            ornaments.setQualityName(searchList.getQualityName());
            ornaments.setQualityColor(searchList.getQualityColor());
            ornaments.setRarity(rarityValue);
            ornaments.setRarityName(searchList.getRarityName());
            ornaments.setRarityColor(searchList.getRarityColor());
            ornaments.setExterior(exteriorValue);
            ornaments.setExteriorName(searchList.getExteriorName());
            ornaments.setCreateTime(DateUtils.getNowDate());
            ornamentsList.add(ornaments);
        });
        log.info("新增条数：" + ornamentsList.size());
        boolean isSuccess = ornamentsService.saveOrUpdateBatch(ornamentsList, 1);
        if (isSuccess) {
            log.info("批量插入饰品成功！");
        } else {
            log.info("批量插入饰品失败！");
        }
    }

    private Map<String, String> objectToMap(List<SysDictData> list) {
        Map<String, String> map = new HashMap<>();
        for (SysDictData sysDictData : list) {
            map.put(sysDictData.getDictLabel(), sysDictData.getDictValue());
        }
        return map;
    }
}
