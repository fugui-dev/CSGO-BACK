package com.ruoyi.playingmethod.newgame.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "game")
public class GameConfig {
    
    /**
     * 游戏开始倒计时时间（秒）
     */
    private int startCountdown = 3;
    
    /**
     * 开箱动画时长（毫秒）
     */
    private int boxOpeningDuration = 3000;
    
    /**
     * 回合结果展示时长（毫秒）
     */
    private int roundResultDuration = 3000;
    
    /**
     * 回合间隔时间（毫秒）
     */
    private int roundInterval = 2000;
    
    /**
     * 游戏结束结果展示时长（毫秒）
     */
    private int gameEndDuration = 5000;
} 