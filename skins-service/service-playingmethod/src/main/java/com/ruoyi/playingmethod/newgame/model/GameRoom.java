package com.ruoyi.playingmethod.newgame.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

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
    
    /**
     * 检查房间是否已满
     */
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
    
    /**
     * 检查是否所有玩家都准备好了
     */
    public boolean isAllReady() {
        return players.values().stream().allMatch(GamePlayer::isReady);
    }
    
    /**
     * 检查是否可以开始游戏
     */
    public boolean canStart() {
        return players.size() >= minPlayers && isAllReady();
    }
    
    /**
     * 计算总回合数
     */
    public void calculateTotalRounds() {
        totalRounds = boxConfigs.stream()
                .mapToInt(BoxConfig::getCount)
                .sum();
    }
    
    /**
     * 检查是否所有玩家都完成了当前回合
     * 机器人玩家自动视为完成
     */
    public boolean isAllPlayersFinished() {
        return players.values().stream()
                .allMatch(player -> 
                    player.isRobot() || // 机器人自动视为完成
                    Boolean.TRUE.equals(playerOpeningStatus.get(player.getUserId())) // 真实玩家需要检查状态
                );
    }
    
    /**
     * 重置玩家开箱状态
     */
    public void resetOpeningStatus() {
        playerOpeningStatus.clear();
        for (GamePlayer player : players.values()) {
            playerOpeningStatus.put(player.getUserId(), false);
        }
    }
} 