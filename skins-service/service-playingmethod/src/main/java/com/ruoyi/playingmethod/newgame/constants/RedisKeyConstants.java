package com.ruoyi.playingmethod.newgame.constants;

/**
 * Redis键常量
 */
public class RedisKeyConstants {
    
    /** 游戏历史记录 */
    public static final String REDIS_KEY_GAME_HISTORY = "game:history:";
    
    /** 用户历史记录 */
    public static final String REDIS_KEY_USER_HISTORY = "game:user:history:";
    
    /** 游戏房间 */
    public static final String REDIS_KEY_ROOM = "game:room:";
    
    /** 游戏模式 - 欧皇模式 */
    public static final String GAME_MODE_RICH = "RICH";
    
    /** 游戏模式 - 非酋模式 */
    public static final String GAME_MODE_POOR = "POOR";
} 