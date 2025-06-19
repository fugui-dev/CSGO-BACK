package com.ruoyi.thirdparty.common.NetworkDataLoader;

import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.domain.dto.zbt.OrnamentZBT;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.thirdparty.zbt.param.ProductListParams;
import com.ruoyi.thirdparty.zbt.param.SearchParams;
import com.ruoyi.thirdparty.zbt.result.ResultZbt;
import com.ruoyi.thirdparty.zbt.result.product.AvailableMarket;
import com.ruoyi.thirdparty.zbt.result.product.AvailableMarketList;
import com.ruoyi.thirdparty.zbt.result.product.SearchData;
import com.ruoyi.thirdparty.zbt.service.ZBTService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
// @Data
public class ZBTNetworkDataLoader extends BaseNetworkDataLoader implements NetworkDataLoader{

    private ZBTService zbtService;

    @Override
    public Set<TtOrnament> load() {

        Set<TtOrnament> res = new HashSet<>();

        for (SysDictData type : allEnum){
            // 根据指定的饰品类型加载数据
            if (!type.getDictType().equals("ornaments_type")) continue;

            log.info("正在加载{}类型的饰品资源~",type.getDictLabel());

            Set<TtOrnament> data = ZBTLoadOrnamentByType(type);

            res.addAll(data);
        }

        return res;
    }

    private Set<TtOrnament> ZBTLoadOrnamentByType(SysDictData type){

        HashSet<TtOrnament> result = new HashSet<>();

        // 分批请求api
        int page = 1;
        boolean stopFlag = false;
        while (true) {

            // 结束标记
            if (stopFlag) break;

            //1 请求平台数据
            SearchParams param = SearchParams.builder()
                    .page(String.valueOf(page))
                    .category(type.getDictLabel())
                    .limit("200")
                    .build();
            ResultZbt<SearchData> response = zbtService.search(param);

            // 解析平台数据
            SearchData data = response.getData();
            if (!response.getSuccess() || ObjectUtil.isEmpty(data)) break;
            List<OrnamentZBT> list = JSONUtil.toList(JSONUtil.toJsonStr(data.getList()), OrnamentZBT.class);

            if (list.size() < 200) stopFlag = !stopFlag;

            // 转换为通用类型
            Set<TtOrnament> collect = list.stream().map(item -> {

                // 生成本平台id
                Long id = createOrnamentId(item.getMarketHashName());

                TtOrnament ornament = TtOrnament.builder()
                        .id(id)
                        .zbtId(item.getItemId())
                        .marketHashName(item.getMarketHashName())
                        .name(item.getItemName())
                        .shortName(item.getShortName())

                        // 价格信息
                        .price(ObjectUtil.isNotEmpty(item.getPriceInfo()) ? item.getPriceInfo().getPrice() : null)
                        .quantity(ObjectUtil.isNotEmpty(item.getPriceInfo()) ? item.getPriceInfo().getQuantity() : null)

                        .imageUrl(item.getImageUrl())

                        .type(type.getDictValue())
                        .typeName(item.getTypeName())
                        .typeHashName(item.getType())

                        .quality(getCodeByNameOrHash(allEnum,item.getQuality()))
                        .qualityName(item.getQualityName())
                        .qualityHashName(item.getQuality())
                        .qualityColor(item.getQualityColor())

                        .rarity(getCodeByNameOrHash(allEnum,item.getRarity()))
                        .rarityName(item.getRarityName())
                        .rarityHashName(item.getRarity())
                        .rarityColor(item.getRarityColor())

                        .exterior(getCodeByNameOrHash(allEnum,item.getExterior()))
                        .exteriorHashName(item.getExterior())
                        .exteriorName(item.getExteriorName())

                        .updateTime(new Date())
                        .build();

                // 补充价格信息
                AvailableMarket sellInfo = apiSellInfo(String.valueOf(ornament.getZbtId()));
                if (ObjectUtil.isNotEmpty(sellInfo)) {
                    ornament.setUsePrice(sellInfo.getCnyPrice());
                }

                return ornament;
            }).collect(Collectors.toSet());

            result.addAll(collect);

            // log.info("加载【扎比特】{}条类型为{}的饰品数据。",page*collect.size(),typeHash);
            System.out.print("\r加载【扎比特】"+page*collect.size()+"条类型为"+type.getDictLabel()+"的饰品数据。请耐心等待");

            page++;
        }

        return result;

    }

    // 查询价格信息
    private AvailableMarket apiSellInfo(String itemId){
        ProductListParams p = ProductListParams.builder()
                .itemId(itemId)
                .page("1")
                .limit("1")
                .language("zh_CN")
                .build();
        ResultZbt<AvailableMarketList> zbtData = zbtService.productList(p);
        if (!zbtData.getSuccess()) return null;
        List<AvailableMarket> list = zbtData.getData().getList();
        if (list.isEmpty()) return null;
        return list.get(0);
    }

}
