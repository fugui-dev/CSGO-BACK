package com.ruoyi.thirdparty.common.config;

// 网络资源加载器配置

import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.system.mapper.SysDictDataMapper;
import com.ruoyi.thirdparty.common.NetworkDataLoader.NetworkDataLoader;
import com.ruoyi.thirdparty.common.NetworkDataLoader.ZBTNetworkDataLoader;
import com.ruoyi.thirdparty.zbt.service.ZBTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class NetworkDataLoaderConfig {

    @Autowired
    private ZBTService zbtService;

    @Autowired
    private SysDictDataMapper dictDataMapper;

    @Bean
    public List<NetworkDataLoader> networkDataLoaderList(){

        // 加载必要的枚举数据
        List<String> dictTypes = Arrays.asList(
                "ornaments_type", "ornaments_type_name",
                "ornaments_exterior", "ornaments_exterior_name",
                "ornaments_quality", "ornaments_quality_name",
                "ornaments_rarity", "ornaments_rarity_name");
        List<SysDictData> enumList = dictDataMapper.selectDictDataByTypes(dictTypes);

        ArrayList<NetworkDataLoader> networkDataLoaders = new ArrayList<>();

        // zbt加载器
        ZBTNetworkDataLoader zbtNetworkDataLoader = new ZBTNetworkDataLoader(zbtService);
        zbtNetworkDataLoader.setAllEnum(enumList);
        zbtNetworkDataLoader.setName("扎比特加载器");

        networkDataLoaders.add(zbtNetworkDataLoader);

        return networkDataLoaders;

    }

}
