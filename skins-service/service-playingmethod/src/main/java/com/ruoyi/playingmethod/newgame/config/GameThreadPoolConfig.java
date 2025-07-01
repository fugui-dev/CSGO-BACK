package com.ruoyi.playingmethod.newgame.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class GameThreadPoolConfig {
    
    @Bean(name = "gameScheduledExecutor")
    public ScheduledExecutorService gameScheduledExecutor() {
        // 创建线程池，核心线程数设为5，足够处理多个房间的倒计时
        return new ScheduledThreadPoolExecutor(5,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
} 