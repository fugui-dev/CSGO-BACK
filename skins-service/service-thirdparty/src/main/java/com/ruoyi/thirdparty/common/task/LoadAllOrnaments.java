package com.ruoyi.thirdparty.common.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.admin.service.ConfigService;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.admin.service.TtOrnamentYYService;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.domain.common.constant.PartyType;
import com.ruoyi.domain.dto.yyyouping.OrnamentYY;
import com.ruoyi.domain.dto.zbt.OrnamentZBT;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.system.mapper.SysDictDataMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.common.NetworkDataLoader.NetworkDataLoader;
import com.ruoyi.thirdparty.yyyouping.service.yyyoupingService;
import com.ruoyi.thirdparty.yyyouping.utils.common.YYResult;
import com.ruoyi.thirdparty.zbt.param.SearchParams;
import com.ruoyi.thirdparty.zbt.result.ResultZbt;
import com.ruoyi.thirdparty.zbt.result.product.SearchData;
import com.ruoyi.thirdparty.zbt.service.ZBTService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.ruoyi.domain.common.constant.PartyType.YY_YOU_PING;
import static com.ruoyi.domain.common.constant.PartyType.ZBT;

@Slf4j
// 项目启动加载所有饰品信息
//@Component
public class LoadAllOrnaments implements ApplicationRunner {

    // @Value("${yy-youping.ornamentsFilePath}")
    // private String YYOrnamentsFilePath;
    //
    // @Autowired
    // private yyyoupingService yyyoupingService;

    @Autowired
    private TtOrnamentService ttOrnamentService;

    @Value("${mkcsgo.startLoadOrnaments}")
    private Boolean startLoadOrnaments;

    // 资源加载器集合
    @Autowired
    private List<NetworkDataLoader> networkDataLoaderList;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        startLoadOrnaments = false;

        log.info("是否加载网络ornaments资源{}", startLoadOrnaments);
        if (!startLoadOrnaments) return;

        // 1 加载各个平台数据
        Set<TtOrnament> ttOrnaments = new HashSet<>();
        for (NetworkDataLoader loader : networkDataLoaderList) {
            Set<TtOrnament> load = loader.load();
            ttOrnaments.addAll(load);
        }
        if (ObjectUtil.isEmpty(ttOrnaments) || ttOrnaments.isEmpty()) {
            log.warn("加载饰品网络资源失败。");
            return;
        }

        // 2 写db
        log.info("db allData ing~~~");
        for (TtOrnament item : ttOrnaments) {
            try {
                ttOrnamentService.saveOrUpdate(item);
            } catch (DuplicateKeyException e) {
                // log.info("过滤重复数据 【id】{}【hash】{}save。",item.getId(),item.getMarketHashName());
            } catch (Exception e) {
                e.printStackTrace();
                log.info("保存数据异常。");
                break;
            }
        }

        log.info("加载饰品网络资源成功。");

    }

    // 从平台获取数据
    // private Set<TtOrnament> loadOrnamentFromNet(PartyType[] partyTypes) {
    //
    //     // 加载必要的枚举数据
    //     List<String> dictTypes = Arrays.asList(
    //             "ornaments_type", "ornaments_type_name",
    //             "ornaments_exterior", "ornaments_exterior_name",
    //             "ornaments_quality", "ornaments_quality_name",
    //             "ornaments_rarity", "ornaments_rarity_name");
    //     List<SysDictData> enumList = dictDataMapper.selectDictDataByTypes(dictTypes);
    //
    //     Set<TtOrnament> res = null;
    //     for (NetworkDataLoader loader:networkDataLoaderList){
    //         Set<TtOrnament> load = loader.load();
    //         res.addAll(load);
    //     }
    //
    //     return res;
    //
    //     //1 加载ZBT平台数据
    //
    //     //... 加载...平台数据
    //
    //     //2 合并所有数据
    //
    //     // //1 从一个平台获取第一组基础数据
    //     // Set<TtOrnament> baseData = loadBaseData(partyTypes[1],enumList);
    //     // if (ObjectUtil.isEmpty(baseData) || baseData.isEmpty()) return baseData;
    //     //
    //     // //2 整合其他平台
    //     // return mergeOtherParty(baseData, partyTypes, enumList);
    //
    // }
    //
    // public Set<TtOrnament> mergeOtherParty(Set<TtOrnament> baseData,PartyType[] partyTypes ,List<SysDictData> enumList){
    //
    //     // Integer f = 0;
    //     for (int i = 1; i < partyTypes.length; i++) {
    //         if (partyTypes[i].equals(YY_YOU_PING)) {
    //
    //             YYResult yyResult = yyyoupingService.yyApiTemplateQuery(new HashMap<String, Object>());
    //             String fileUrl = (String) yyResult.getData();
    //             log.info(fileUrl);
    //
    //             // 下载文件
    //             SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    //             String format = dateFormat.format(System.currentTimeMillis());
    //             String fileName = YYOrnamentsFilePath + "yy_orn_" + format + ".txt";
    //
    //             long size = HttpUtil.downloadFile(fileUrl, FileUtil.file(fileName));
    //             log.info("yy饰品信息下载成功，文件大小: " + size);
    //
    //             // 解析json
    //             File file = FileUtil.file(fileName);
    //             JSONArray jsonArray = JSONUtil.readJSONArray(file, StandardCharsets.UTF_8);
    //             List<OrnamentYY> yyList = jsonArray.toList(OrnamentYY.class);
    //
    //             // 转换为TtOrnament
    //             Set<TtOrnament> collect = yyList.stream().map(item -> {
    //
    //                 return TtOrnament.builder()
    //                         .id(Long.valueOf(item.getHashName().hashCode()))
    //                         .yyyoupingId(item.getId())
    //                         .marketHashName(item.getHashName())
    //                         .name(item.getName())
    //                         .price(BigDecimal.TEN) //// TODO: 2024/4/1 补充价格信息
    //                         .usePrice(BigDecimal.TEN)
    //                         .updateTime(item.getUpdateTime())
    //                         .build();
    //
    //             }).collect(Collectors.toSet());
    //
    //             baseData.addAll(collect);
    //
    //         } else if (partyTypes[i].equals(ZBT)) {
    //
    //             // 饰品类型枚举
    //             //List<SysDictData> typeList = dictDataMapper.selectDictDataByType("ornaments_type");
    //
    //             for (SysDictData type : enumList) {
    //
    //                 if (!type.getDictType().equals("ornaments_type")) continue;
    //
    //                 Set<TtOrnament> ttOrnaments = ZBTLoadOrnamentByType(type.getDictLabel(),type,enumList);
    //
    //                 baseData.addAll(ttOrnaments);
    //
    //                 log.info("合并其他平台{}条数据。",ttOrnaments.size());
    //
    //             }
    //
    //         } else {
    //             log.warn("非法的平台类型代码，获取饰品数据失败。");
    //         }
    //     }
    //
    //     // 过滤zbt没有的数据
    //     log.info("过滤数据");
    //     Set<TtOrnament> resData = baseData.stream().filter(item -> {
    //         return ObjectUtil.isNotEmpty(item.getZbtId());
    //     }).collect(Collectors.toSet());
    //
    //     return resData;
    //
    // }
    //
    // // zbt 根据饰品类型获取数据
    // private Set<TtOrnament> ZBTLoadOrnamentByType(String typeHash,SysDictData type,List<SysDictData> allEnum){
    //
    //     HashSet<TtOrnament> result = new HashSet<>();
    //
    //     // 分批请求api
    //     int page = 1;
    //     boolean stopFlag = false;
    //     while (true) {
    //
    //         // 结束标记
    //         if (stopFlag) break;
    //
    //         // 请求平台数据
    //         SearchParams param = SearchParams.builder()
    //                 .page(String.valueOf(page))
    //                 .category(typeHash)
    //                 .limit("200")
    //                 .build();
    //         ResultZbt<SearchData> response = zbtService.search(param);
    //         SearchData data = response.getData();
    //         if (!response.getSuccess() || ObjectUtil.isEmpty(data)) break;
    //
    //         List<OrnamentZBT> list = JSONUtil.toList(JSONUtil.toJsonStr(data.getList()), OrnamentZBT.class);
    //
    //         if (list.size() < 200) stopFlag = !stopFlag;
    //
    //         // 转换为通用类型
    //         Set<TtOrnament> collect = list.stream().map(item -> {
    //
    //             Long id = Long.valueOf(HashUtil.rsHash(item.getMarketHashName()));
    //             if (id < 0) id = Math.abs(id);
    //
    //             TtOrnament ornament = TtOrnament.builder()
    //                     .id(id)
    //                     .zbtId(item.getItemId())
    //                     .marketHashName(item.getMarketHashName())
    //                     .name(item.getItemName())
    //                     .shortName(item.getShortName())
    //
    //                     .price(ObjectUtil.isNotEmpty(item.getPriceInfo()) ? item.getPriceInfo().getPrice() : null)
    //                     .usePrice(ObjectUtil.isNotEmpty(item.getPriceInfo()) ? item.getPriceInfo().getPrice() : null)
    //                     .quantity(ObjectUtil.isNotEmpty(item.getPriceInfo()) ? item.getPriceInfo().getQuantity() : null)
    //
    //                     .imageUrl(item.getImageUrl())
    //
    //                     .type(type.getDictValue())
    //                     .typeName(item.getTypeName())
    //
    //                     .quality(getCodeByNameOrHash(allEnum,item.getQuality()))
    //                     .qualityName(item.getQualityName())
    //                     .qualityColor(item.getQualityColor())
    //
    //                     .rarity(getCodeByNameOrHash(allEnum,item.getRarity()))
    //                     .rarityName(item.getRarityName())
    //                     .rarityColor(item.getRarityColor())
    //
    //                     .exterior(getCodeByNameOrHash(allEnum,item.getExterior()))
    //                     .exteriorHashName(item.getExterior())
    //                     .exteriorName(item.getExteriorName())
    //
    //                     .updateTime(item.getUpdateTime())
    //                     .build();
    //
    //             ornament.setUsePrice(ornament.getPrice());
    //
    //             return ornament;
    //         }).collect(Collectors.toSet());
    //
    //         result.addAll(collect);
    //
    //         // log.info("加载【扎比特】{}条类型为{}的饰品数据。",page*collect.size(),typeHash);
    //         System.out.print("\r加载【扎比特】"+page*collect.size()+"条类型为"+typeHash+"的饰品数据。");
    //
    //         page++;
    //     }
    //
    //     return result;
    //
    // }
    //
    // // 从yy平台获取第一组基础数据
    // private Set<TtOrnament> loadBaseData(PartyType partyType,List<SysDictData> allEnum) {
    //
    //     Set<TtOrnament> baseData = null;
    //
    //     if (partyType.equals(YY_YOU_PING)) {
    //         baseData = getBaseDataFromYY(allEnum);
    //     } else if (partyType.equals(ZBT)) {
    //
    //         baseData = getBaseDataFromZBT(allEnum);
    //
    //     } else {
    //         log.warn("非法的平台类型代码，获取饰品数据失败。");
    //         return baseData;
    //     }
    //
    //     return baseData;
    // }
    //
    // // 从ZBT获取基础数据
    // public Set<TtOrnament> getBaseDataFromZBT(List<SysDictData> allEnum){
    //
    //     Set<TtOrnament> baseData = null;
    //
    //     YYResult yyResult = yyyoupingService.yyApiTemplateQuery(new HashMap<String, Object>());
    //     if (!yyResult.isSuccess()) {
    //         log.warn(yyResult.getMsg());
    //         return baseData;
    //     }
    //     String fileUrl = (String) yyResult.getData();
    //     log.info("yyFile=" + fileUrl);
    //
    //     // 下载文件
    //     String fileName = YYOrnamentsFilePath + "yyOrn" + System.currentTimeMillis() + ".txt";
    //     System.out.println(fileName);
    //
    //     long size = HttpUtil.downloadFile(fileUrl, new File(fileName));
    //     log.info("yy饰品信息下载成功，文件大小: " + size);
    //
    //     // 解析json
    //     File file = FileUtil.file(fileName);
    //     JSONArray jsonArray = JSONUtil.readJSONArray(file, StandardCharsets.UTF_8);
    //     List<OrnamentYY> yyList = jsonArray.toList(OrnamentYY.class);
    //
    //     log.info("补充详细信息~~~");
    //     // 转换为TtOrnament
    //     baseData = yyList.stream().map(item -> {
    //
    //         Long id = Long.valueOf(HashUtil.rsHash(item.getHashName()));
    //         if (id < 0) {
    //             id = Math.abs(id);
    //         }
    //
    //         TtOrnament ornament = TtOrnament.builder()
    //                 .id(id)
    //                 .yyyoupingId(item.getId())
    //                 .marketHashName(item.getHashName())
    //
    //                 .type(getCodeByNameOrHash(allEnum,item.getTypeHashName()))
    //                 .typeName(item.getTypeName())
    //                 .typeHashName(item.getTypeHashName())
    //
    //                 .name(item.getName())
    //                 .shortName(item.getName())
    //
    //                 .isPutaway("1")
    //                 .isProprietaryProperty("1")
    //                 .updateTime(item.getUpdateTime())
    //                 .build();
    //
    //         return ornament;
    //     }).collect(Collectors.toSet());
    //
    //     // 分批补充 补充其他信息
    //     List<Map<String, String>> requestList = baseData.stream().map(item -> {
    //         return MapUtil.builder("templateId", String.valueOf(item.getYyyoupingId())).build();
    //     }).collect(Collectors.toList());
    //
    //     int page = 0;
    //     int pageSize = 200;
    //     Boolean stopFlag = false;
    //     ArrayList<TtOrnament> ornamentsList = new ArrayList<>(baseData);
    //     while (true) {
    //
    //         if (stopFlag) {
    //             System.out.println("-100% 补充完成✔");
    //             break;
    //         }
    //
    //         BigDecimal p = new BigDecimal(page * pageSize)
    //                 .divide(new BigDecimal(baseData.size()),2,BigDecimal.ROUND_DOWN);
    //         System.out.print("\r补充"+page * pageSize+"条基础记录"+p.multiply(new BigDecimal(100)).setScale(0)+"%");
    //
    //
    //         List<Map<String, String>> reqList = null;
    //         try {
    //             reqList = requestList.subList(page * pageSize, page * pageSize + pageSize); // 尽量大
    //         } catch (Exception e) {
    //             int lenth = requestList.size();
    //             reqList = requestList.subList(page * pageSize, lenth - 1); // 尽量大
    //             stopFlag = !stopFlag;
    //         }
    //
    //         HashMap<String, Object> map = new HashMap<>();
    //         map.put("requestList", reqList);
    //
    //         YYResult yyResult1 = yyyoupingService.yyApiBatchGetOnSaleCommodityInfo(map);
    //         List<JSONObject> list = JSONUtil.toList(JSONUtil.toJsonStr(yyResult1.getData()), JSONObject.class);
    //
    //         for (JSONObject obj : list) {
    //
    //             JSONObject saleTemplateResponse = JSONUtil.parseObj(obj.get("saleTemplateResponse"));
    //             JSONObject saleCommodityResponse = JSONUtil.parseObj(obj.get("saleCommodityResponse"));
    //
    //             String templateId = String.valueOf((Integer) saleTemplateResponse.get("templateId"));
    //
    //             for (int i = 0; i < ornamentsList.size(); i++) {
    //
    //                 TtOrnament ornament = ornamentsList.get(i);
    //                 if (templateId.equals(String.valueOf(ornament.getYyyoupingId()))) {
    //
    //                     ornament.setPrice(new BigDecimal(saleCommodityResponse.get("minSellPrice").toString()));
    //                     ornament.setUsePrice(new BigDecimal(saleCommodityResponse.get("minSellPrice").toString()));
    //
    //                     ornament.setImageUrl(saleTemplateResponse.get("iconUrl").toString());
    //
    //                     Object exteriorName = saleTemplateResponse.get("exteriorName");
    //                     if (ObjectUtil.isNotNull(exteriorName)){
    //                         ornament.setExterior(getCodeByNameOrHash(allEnum,exteriorName.toString()));
    //                         ornament.setExteriorName(exteriorName.toString());
    //                     }
    //
    //                     ornament.setQuality(getCodeByNameOrHash(allEnum,saleTemplateResponse.get("qualityName").toString()));
    //                     ornament.setQualityName(saleTemplateResponse.get("qualityName").toString());
    //                     // if (ObjectUtil.isEmpty(ornament.getQuality()) && ObjectUtil.isNotEmpty(ornament.getQualityName())){
    //                     //     System.out.println(ornament.getQuality());
    //                     //     System.out.println(ornament.getQualityName());
    //                     // }
    //
    //                     ornament.setRarity(getCodeByNameOrHash(allEnum,saleTemplateResponse.get("rarityName").toString()));
    //                     ornament.setRarityName(saleTemplateResponse.get("rarityName").toString());
    //
    //                     ornamentsList.set(i,ornament);
    //
    //                     break;
    //                 }
    //             }
    //         }
    //
    //         page++;
    //
    //     }
    //
    //     baseData = new HashSet<>(ornamentsList);
    //
    //     return baseData;
    // }
    //
    // // 从yy获取基础数据
    // public Set<TtOrnament> getBaseDataFromYY(List<SysDictData> allEnum){
    //
    //     Set<TtOrnament> baseData = null;
    //
    //     YYResult yyResult = yyyoupingService.yyApiTemplateQuery(new HashMap<String, Object>());
    //     if (!yyResult.isSuccess()) {
    //         log.warn(yyResult.getMsg());
    //         return baseData;
    //     }
    //     String fileUrl = (String) yyResult.getData();
    //     log.info("yyFile=" + fileUrl);
    //
    //     // 下载文件
    //     String fileName = YYOrnamentsFilePath + "yyOrn" + System.currentTimeMillis() + ".txt";
    //     System.out.println(fileName);
    //
    //     long size = HttpUtil.downloadFile(fileUrl, new File(fileName));
    //     log.info("yy饰品信息下载成功，文件大小: " + size);
    //
    //     // 解析json
    //     File file = FileUtil.file(fileName);
    //     JSONArray jsonArray = JSONUtil.readJSONArray(file, StandardCharsets.UTF_8);
    //     List<OrnamentYY> yyList = jsonArray.toList(OrnamentYY.class);
    //
    //     log.info("补充详细信息~~~");
    //     // 转换为TtOrnament
    //     baseData = yyList.stream().map(item -> {
    //
    //         Long id = Long.valueOf(HashUtil.rsHash(item.getHashName()));
    //         if (id < 0) {
    //             id = Math.abs(id);
    //         }
    //
    //         TtOrnament ornament = TtOrnament.builder()
    //                 .id(id)
    //                 .yyyoupingId(item.getId())
    //                 .marketHashName(item.getHashName())
    //
    //                 .type(getCodeByNameOrHash(allEnum,item.getTypeHashName()))
    //                 .typeName(item.getTypeName())
    //                 .typeHashName(item.getTypeHashName())
    //
    //                 .name(item.getName())
    //                 .shortName(item.getName())
    //
    //                 .isPutaway("1")
    //                 .isProprietaryProperty("1")
    //                 .updateTime(item.getUpdateTime())
    //                 .build();
    //
    //         return ornament;
    //     }).collect(Collectors.toSet());
    //
    //     // 分批补充 补充其他信息
    //     List<Map<String, String>> requestList = baseData.stream().map(item -> {
    //         return MapUtil.builder("templateId", String.valueOf(item.getYyyoupingId())).build();
    //     }).collect(Collectors.toList());
    //
    //     int page = 0;
    //     int pageSize = 200;
    //     Boolean stopFlag = false;
    //     ArrayList<TtOrnament> ornamentsList = new ArrayList<>(baseData);
    //     while (true) {
    //
    //         if (stopFlag) {
    //             System.out.println("-100% 补充完成✔");
    //             break;
    //         }
    //
    //         BigDecimal p = new BigDecimal(page * pageSize)
    //                 .divide(new BigDecimal(baseData.size()),2,BigDecimal.ROUND_DOWN);
    //         System.out.print("\r补充"+page * pageSize+"条基础记录"+p.multiply(new BigDecimal(100)).setScale(0)+"%");
    //
    //
    //         List<Map<String, String>> reqList = null;
    //         try {
    //             reqList = requestList.subList(page * pageSize, page * pageSize + pageSize); // 尽量大
    //         } catch (Exception e) {
    //             int lenth = requestList.size();
    //             reqList = requestList.subList(page * pageSize, lenth - 1); // 尽量大
    //             stopFlag = !stopFlag;
    //         }
    //
    //         HashMap<String, Object> map = new HashMap<>();
    //         map.put("requestList", reqList);
    //
    //         YYResult yyResult1 = yyyoupingService.yyApiBatchGetOnSaleCommodityInfo(map);
    //         List<JSONObject> list = JSONUtil.toList(JSONUtil.toJsonStr(yyResult1.getData()), JSONObject.class);
    //
    //         for (JSONObject obj : list) {
    //
    //             JSONObject saleTemplateResponse = JSONUtil.parseObj(obj.get("saleTemplateResponse"));
    //             JSONObject saleCommodityResponse = JSONUtil.parseObj(obj.get("saleCommodityResponse"));
    //
    //             String templateId = String.valueOf((Integer) saleTemplateResponse.get("templateId"));
    //
    //             for (int i = 0; i < ornamentsList.size(); i++) {
    //
    //                 TtOrnament ornament = ornamentsList.get(i);
    //                 if (templateId.equals(String.valueOf(ornament.getYyyoupingId()))) {
    //
    //                     ornament.setPrice(new BigDecimal(saleCommodityResponse.get("minSellPrice").toString()));
    //                     ornament.setUsePrice(new BigDecimal(saleCommodityResponse.get("minSellPrice").toString()));
    //
    //                     ornament.setImageUrl(saleTemplateResponse.get("iconUrl").toString());
    //
    //                     Object exteriorName = saleTemplateResponse.get("exteriorName");
    //                     if (ObjectUtil.isNotNull(exteriorName)){
    //                         ornament.setExterior(getCodeByNameOrHash(allEnum,exteriorName.toString()));
    //                         ornament.setExteriorName(exteriorName.toString());
    //                     }
    //
    //                     ornament.setQuality(getCodeByNameOrHash(allEnum,saleTemplateResponse.get("qualityName").toString()));
    //                     ornament.setQualityName(saleTemplateResponse.get("qualityName").toString());
    //                     // if (ObjectUtil.isEmpty(ornament.getQuality()) && ObjectUtil.isNotEmpty(ornament.getQualityName())){
    //                     //     System.out.println(ornament.getQuality());
    //                     //     System.out.println(ornament.getQualityName());
    //                     // }
    //
    //                     ornament.setRarity(getCodeByNameOrHash(allEnum,saleTemplateResponse.get("rarityName").toString()));
    //                     ornament.setRarityName(saleTemplateResponse.get("rarityName").toString());
    //
    //                     ornamentsList.set(i,ornament);
    //
    //                     break;
    //                 }
    //             }
    //         }
    //
    //         page++;
    //
    //     }
    //
    //     baseData = new HashSet<>(ornamentsList);
    //
    //     return baseData;
    // }
    //
    // public static void main(String[] args) {
    //     System.out.println("StatTrak™".equals(""));
    // }
    //
    // // 根据类型和标签获取code
    // private String getCodeByNameOrHash(List<SysDictData> typeList,String nameOrHash){
    //
    //     for (SysDictData type : typeList){
    //         if (type.getDictLabel().equals(nameOrHash)) return type.getDictValue();
    //         if (type.getDictType().equals(nameOrHash)) return type.getDictValue();
    //     }
    //     return null;
    // }


}
