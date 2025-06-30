package com.ruoyi.playingmethod.newgame.constants;

/**
 * 游戏常量
 */
public class GameConstants {
    
    /**
     * 房间缓存key
     */
    public static final String ROOM_CACHE_KEY = "game:room:%s";
    
    /**
     * 用户房间关联缓存key
     */
    public static final String USER_ROOM_CACHE_KEY = "game:user:room:%s";
    
    /**
     * 房间观战者缓存key
     */
    public static final String ROOM_SPECTATOR_KEY = "game:room:spectator:%s";
    
    /**
     * 房间过期时间(分钟)
     */
    public static final long ROOM_EXPIRE_TIME = 30;
    
    /**
     * 房间状态 - 等待中
     */
    public static final String ROOM_STATUS_WAITING = "waiting";
    
    /**
     * 房间状态 - 游戏中
     */
    public static final String ROOM_STATUS_PLAYING = "playing";
    
    /**
     * 房间状态 - 已结束
     */
    public static final String ROOM_STATUS_ENDED = "ended";
    
    /**
     * 游戏模式 - 欧皇
     */
    public static final String GAME_MODE_RICH = "rich";
    
    /**
     * 游戏模式 - 非酋
     */
    public static final String GAME_MODE_POOR = "poor";
    
    /**
     * 最大玩家数
     */
    public static final int MAX_PLAYERS = 4;
    
    /**
     * 最小玩家数
     */
    public static final int MIN_PLAYERS = 2;
    
    /**
     * 最大观战人数
     */
    public static final int MAX_SPECTATORS = 20;
    
    /**
     * WebSocket消息类型
     */
    public static final String WS_TYPE_ROOM_UPDATE = "room_update";
    public static final String WS_TYPE_PLAYER_JOIN = "player_join";
    public static final String WS_TYPE_PLAYER_LEAVE = "player_leave";
    public static final String WS_TYPE_PLAYER_READY = "player_ready";
    public static final String WS_TYPE_GAME_START = "game_start";
    public static final String WS_TYPE_GAME_END = "game_end";
    public static final String WS_TYPE_BOX_OPENING = "box_opening";
    public static final String WS_TYPE_BOX_OPENED = "box_opened";
    public static final String WS_TYPE_BOX_RESULT = "box_result";
    public static final String WS_TYPE_ROUND_START = "round_start";
    public static final String WS_TYPE_ROUND_RESULT = "round_result";
    public static final String WS_TYPE_ERROR = "error";
    public static final String WS_TYPE_SPECTATOR_JOIN = "spectator_join";
    public static final String WS_TYPE_SPECTATOR_LEAVE = "spectator_leave";
    
    /**
     * Redis锁前缀
     */
    public static final String LOCK_CREATE_ROOM = "lock:create_room:";
    public static final String LOCK_JOIN_ROOM = "lock:join_room:";
    public static final String LOCK_START_GAME = "lock:start_game:";
    
    /**
     * 玩家状态
     */
    public static final String PLAYER_STATUS_WAITING = "waiting";
    public static final String PLAYER_STATUS_READY = "ready";
    public static final String PLAYER_STATUS_PLAYING = "playing";
    public static final String PLAYER_STATUS_FINISHED = "finished";
    
    /**
     * 开箱动画相关
     */
    public static final int ANIMATION_ITEMS_COUNT = 30;
    public static final int ANIMATION_DURATION = 3000;
    
    /**
     * 概率调整系数
     */
    public static final double ROBOT_NORMAL_RATE = 1.2;
    public static final double ROBOT_HIGH_VALUE_RATE = 0.8;
    public static final double HIGH_VALUE_THRESHOLD = 5.0;
    
    /**
     * 游戏历史记录保存天数
     */
    public static final int GAME_HISTORY_DAYS = 7;
    
    /**
     * 用户历史索引保存天数
     */
    public static final int USER_HISTORY_DAYS = 30;
    
    /**
     * 库存缓存时间(小时)
     */
    public static final int INVENTORY_CACHE_HOURS = 1;
} 