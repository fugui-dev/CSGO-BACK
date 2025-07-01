package com.ruoyi.playingmethod.newgame.constants;

/**
 * Redis键常量
 */
public class RedisKeyConstants {
    
    /**
     * 游戏历史记录
     */
    public static final String KEY_GAME_HISTORY = "game:history:";
    
    /**
     * 用户历史记录
     */
    public static final String KEY_USER_HISTORY = "game:user:history:";
    
    /**
     * 游戏房间
     */
    public static final String KEY_ROOM = "game:room:%s";
    
    /**
     * 用户房间关联
     */
    public static final String KEY_USER_ROOM = "game:user:room:%s";
    
    /**
     * 房间观战者
     */
    public static final String KEY_ROOM_SPECTATOR = "game:room:spectator:%s";
    
    /**
     * 当前回合ID
     */
    public static final String KEY_CURRENT_ROUND = "game:round:current:";
    
    /**
     * Redis锁前缀
     */
    public static final String LOCK_CREATE_ROOM = "lock:create_room:";
    public static final String LOCK_JOIN_ROOM = "lock:join_room:";
    public static final String LOCK_START_GAME = "lock:start_game:";
} 