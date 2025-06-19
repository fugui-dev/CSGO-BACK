package com.ruoyi.playingmethod.config;

import com.ruoyi.admin.mapper.TtBoxOrnamentsMapper;
import com.ruoyi.admin.util.core.fight.LotteryMachine;
import com.ruoyi.admin.util.core.fight.PrizePool;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.redis.config.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class LotteryMachineConfig {

    @Autowired
    RedisLock redisLock;
    @Autowired
    RedisCache redisCache;
    @Autowired
    TtBoxOrnamentsMapper boxOrnamentsMapper;

    @Bean
    public LotteryMachine lotteryMachine(){

        LotteryMachine machine = LotteryMachine.builder()
                .boxOrnamentsMapper(boxOrnamentsMapper)
                .redisLock(redisLock)
                .redisCache(redisCache)
                .prizePools(new HashMap<String, PrizePool>())
                .build();

        machine.preheat();

        return machine;
    }

}
