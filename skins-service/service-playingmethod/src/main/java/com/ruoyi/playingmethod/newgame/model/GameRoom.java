package com.ruoyi.playingmethod.newgame.model;

import com.ruoyi.domain.entity.TtOrnament;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 游戏房间
 */
@Data
public class GameRoom {
    
    /**
     * 房间ID
     */
    private String roomId;
    
    /**
     * 房间状态
     */
    private String status;
    
    /**
     * 游戏模式
     */
    private String gameMode;
    
    /**
     * 房间创建者ID
     */
    private String creatorId;
    
    /**
     * 房间创建时间
     */
    private Date createTime;
    
    /**
     * 游戏开始时间
     */
    private Date startTime;

    /**
     * 游戏结束时间
     */
    private Date endTime;
    
    /**
     * 参与玩家列表
     */
    private Map<String, GamePlayer> players = new ConcurrentHashMap<>();
    
    /**
     * 箱子配置
     */
    private List<BoxConfig> boxConfigs = new ArrayList<>();
    
    /**
     * 当前回合
     */
    private int currentRound;
    
    /**
     * 总回合数
     */
    private int totalRounds;
    
    /**
     * 每个玩家的开箱结果
     * Map<玩家ID, List<饰品ID>>
     */
    private Map<String, List<String>> playerResults = new ConcurrentHashMap<>();
    
    /**
     * 玩家开箱状态
     * Map<玩家ID, 是否完成开箱>
     */
    private Map<String, Boolean> playerOpeningStatus = new ConcurrentHashMap<>();
    
    /**
     * 最大玩家数
     */
    private int maxPlayers;
    
    /**
     * 最小玩家数
     */
    private int minPlayers;
    
    /**
     * 是否允许观战
     */
    private boolean allowSpectators = true;

    /**
     * 房间总价值（所有盲盒的总价值）
     */
    private BigDecimal totalValue;

    /**
     * 胜利者ID列表
     */
    private List<String> winnerIds = new ArrayList<>();

    /**
     * 失败者ID列表
     */
    private List<String> loserIds = new ArrayList<>();
    
    /**
     * 玩家价值统计
     * key: 玩家ID
     * value: 总价值
     */
    private Map<String, BigDecimal> playerValues = new HashMap<>();

    /**
     * 观战玩家列表
     */
    private Map<String, GamePlayer> spectators = new ConcurrentHashMap<>();

    /**
     * 每回合的胜利者
     * Map<回合数, List<玩家ID>>
     */
    private Map<Integer, List<String>> roundWinners = new ConcurrentHashMap<>();

    /**
     * 每回合的失败者
     * Map<回合数, List<玩家ID>>
     */
    private Map<Integer, List<String>> roundLosers = new ConcurrentHashMap<>();

    /**
     * 每回合的饰品价值
     * Map<回合数, Map<玩家ID, 价值>>
     */
    private Map<Integer, Map<String, BigDecimal>> roundValues = new ConcurrentHashMap<>();
    
    /**
     * 当前回合的开箱结果
     * Map<玩家ID, 饰品信息>
     */
    private Map<String, TtOrnament> currentRoundResults = new HashMap<>();

    /**
     * 所有回合的开箱结果
     * Map<回合数, Map<玩家ID, 饰品信息>>
     */
    private Map<Integer, Map<String, TtOrnament>> allRoundResults = new HashMap<>();
    
    /**
     * 倒计时剩余秒数
     */
    private int countdownSeconds;
    
    /**
     * 是否正在倒计时
     */
    private boolean isCountingDown;

    /**
     * 箱子配置
     */
    @Data
    public static class BoxConfig {
        /**
         * 箱子ID
         */
        private Long boxId;
        
        /**
         * 开箱次数
         */
        private int count;
    }
}