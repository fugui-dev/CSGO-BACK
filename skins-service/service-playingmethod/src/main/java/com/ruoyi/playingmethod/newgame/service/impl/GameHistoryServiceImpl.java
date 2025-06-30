package com.ruoyi.playingmethod.newgame.service.impl;

import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.playingmethod.newgame.constants.RedisKeyConstants;
import com.ruoyi.playingmethod.newgame.model.GameHistory;
import com.ruoyi.playingmethod.newgame.model.GamePlayer;
import com.ruoyi.playingmethod.newgame.model.GameRoom;
import com.ruoyi.playingmethod.newgame.service.GameHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameHistoryServiceImpl implements GameHistoryService {

    private final RedisCache redisCache;
    private final TtOrnamentService ornamentService;

    public GameHistoryServiceImpl(RedisCache redisCache,
                                TtOrnamentService ornamentService) {
        this.redisCache = redisCache;
        this.ornamentService = ornamentService;
    }

    @Override
    public String saveHistory(GameRoom room) {
        GameHistory history = new GameHistory();
        
        // 设置基本信息
        history.setRoomId(room.getRoomId());
        history.setGameMode(room.getGameMode());
        history.setCreatorId(room.getCreatorId());
        history.setCreateTime(room.getCreateTime());
        history.setStartTime(room.getStartTime());
        history.setEndTime(new Date());
        
        // 复制玩家列表(去除敏感信息)
        history.setPlayers(room.getPlayers().values().stream()
                .map(this::copyPlayerWithoutSensitiveInfo)
                .collect(Collectors.toList()));
        
        // 复制箱子配置
        history.setBoxConfigs(new ArrayList<>(room.getBoxConfigs()));
        
        // 复制开箱结果
        history.setPlayerResults(new HashMap<>(room.getPlayerResults()));
        
        // 计算总价值和每个玩家的价值
        Map<String, BigDecimal> playerValues = new HashMap<>();
        BigDecimal totalValue = BigDecimal.ZERO;
        
        for (Map.Entry<String, List<String>> entry : room.getPlayerResults().entrySet()) {
            String playerId = entry.getKey();
            List<String> ornamentIds = entry.getValue();
            
            BigDecimal playerValue = ornamentIds.stream()
                    .map(id -> {
                        TtOrnament ornament = ornamentService.getById(id);
                        return ornament != null ? ornament.getPrice() : BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            playerValues.put(playerId, playerValue);
            totalValue = totalValue.add(playerValue);
        }
        
        history.setPlayerValues(playerValues);
        history.setTotalValue(totalValue);
        
        // 确定胜利者
        List<String> winners = determineWinners(room.getGameMode(), playerValues);
        history.setWinners(winners);
        
        // 保存到Redis
        String historyKey = RedisKeyConstants.REDIS_KEY_GAME_HISTORY + room.getRoomId();
        redisCache.setCacheObject(historyKey, history, 7, TimeUnit.DAYS);
        
        // 更新用户历史记录索引
        for (GamePlayer player : room.getPlayers().values()) {
            String userHistoryKey = RedisKeyConstants.REDIS_KEY_USER_HISTORY + player.getUserId();
            List<String> userHistory = redisCache.getCacheObject(userHistoryKey);
            if (userHistory == null) {
                userHistory = new ArrayList<>();
            }
            userHistory.add(room.getRoomId());
            redisCache.setCacheObject(userHistoryKey, userHistory, 30, TimeUnit.DAYS);
        }
        
        return room.getRoomId();
    }

    @Override
    public List<GameHistory> getUserHistory(String userId, int page, int size) {
        String userHistoryKey = RedisKeyConstants.REDIS_KEY_USER_HISTORY + userId;
        List<String> roomIds = redisCache.getCacheObject(userHistoryKey);
        if (roomIds == null || roomIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 计算分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, roomIds.size());
        if (start >= roomIds.size()) {
            return Collections.emptyList();
        }
        
        // 获取指定页的历史记录
        return roomIds.subList(start, end).stream()
                .map(this::getRoomHistory)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public GameHistory getRoomHistory(String roomId) {
        String historyKey = RedisKeyConstants.REDIS_KEY_GAME_HISTORY + roomId;
        return redisCache.getCacheObject(historyKey);
    }

    @Override
    public double getUserWinRate(String userId) {
        String userHistoryKey = RedisKeyConstants.REDIS_KEY_USER_HISTORY + userId;
        List<String> roomIds = redisCache.getCacheObject(userHistoryKey);
        if (roomIds == null || roomIds.isEmpty()) {
            return 0.0;
        }
        
        long totalGames = roomIds.size();
        long wins = roomIds.stream()
                .map(this::getRoomHistory)
                .filter(Objects::nonNull)
                .filter(history -> history.getWinners().contains(userId))
                .count();
        
        return (double) wins / totalGames;
    }

    @Override
    public double getUserTotalProfit(String userId) {
        String userHistoryKey = RedisKeyConstants.REDIS_KEY_USER_HISTORY + userId;
        List<String> roomIds = redisCache.getCacheObject(userHistoryKey);
        if (roomIds == null || roomIds.isEmpty()) {
            return 0.0;
        }
        
        return roomIds.stream()
                .map(this::getRoomHistory)
                .filter(Objects::nonNull)
                .mapToDouble(history -> calculateUserProfit(history, userId))
                .sum();
    }
    
    // 复制玩家信息(去除敏感信息)
    private GamePlayer copyPlayerWithoutSensitiveInfo(GamePlayer original) {
        GamePlayer copy = new GamePlayer();
        copy.setUserId(original.getUserId());
        copy.setUsername(original.getUsername());
        copy.setAvatar(original.getAvatar());
        copy.setRobot(original.isRobot());
        copy.setSeatNumber(original.getSeatNumber());
        return copy;
    }
    
    // 确定胜利者
    private List<String> determineWinners(String gameMode, Map<String, BigDecimal> playerValues) {
        if (playerValues.isEmpty()) {
            return Collections.emptyList();
        }
        
        if (RedisKeyConstants.GAME_MODE_RICH.equals(gameMode)) {
            // 欧皇模式:最高价值获胜
            BigDecimal maxValue = playerValues.values().stream()
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            return playerValues.entrySet().stream()
                    .filter(e -> e.getValue().compareTo(maxValue) == 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } else if (RedisKeyConstants.GAME_MODE_POOR.equals(gameMode)) {
            // 非酋模式:最低价值获胜
            BigDecimal minValue = playerValues.values().stream()
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            return playerValues.entrySet().stream()
                    .filter(e -> e.getValue().compareTo(minValue) == 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } else {
            // 默认模式:每个人保留自己的开箱结果
            return new ArrayList<>(playerValues.keySet());
        }
    }
    
    // 计算用户在某局游戏中的收益
    private double calculateUserProfit(GameHistory history, String userId) {
        // 如果用户是胜利者
        if (history.getWinners().contains(userId)) {
            // 在欧皇/非酋模式下,获得所有价值
            if (RedisKeyConstants.GAME_MODE_RICH.equals(history.getGameMode()) ||
                RedisKeyConstants.GAME_MODE_POOR.equals(history.getGameMode())) {
                return history.getTotalValue().doubleValue();
            }
        }
        
        // 默认模式或非胜利者,只获得自己开出的价值
        return history.getPlayerValues().getOrDefault(userId, BigDecimal.ZERO).doubleValue();
    }
} 