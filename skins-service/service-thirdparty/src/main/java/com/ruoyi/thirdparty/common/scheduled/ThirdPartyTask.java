package com.ruoyi.thirdparty.common.scheduled;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.thirdparty.common.NetworkDataLoader.BaseNetworkDataLoader;
import com.ruoyi.thirdparty.common.NetworkDataLoader.NetworkDataLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Configuration      // 1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class ThirdPartyTask {

    @Autowired
    private TtOrnamentService ttOrnamentService;

    @Value("${mkcsgo.startLoadOrnaments}")
    private Boolean startLoadOrnaments;

    // 资源加载器集合
    @Autowired
    private List<NetworkDataLoader> networkDataLoaderList;

    // 定时加载发货平台的物品数据
//    @Scheduled(cron = "0 50 23 * * ?")
    // @Scheduled(cron = "0 6 17 * * ?")
    public void networkDataLoad(){

        log.info("是否加载网络ornaments资源{}", startLoadOrnaments);
        if (!startLoadOrnaments) return;

        // 1 加载各个平台数据
        Set<TtOrnament> ttOrnaments = new HashSet<>();
        for (NetworkDataLoader loader : networkDataLoaderList) {
            log.info("【{}】开始加载网络资源~",((BaseNetworkDataLoader)loader).name);
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

}
