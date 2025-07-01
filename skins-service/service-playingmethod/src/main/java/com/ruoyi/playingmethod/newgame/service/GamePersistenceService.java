package com.ruoyi.playingmethod.newgame.service;

import com.ruoyi.playingmethod.newgame.model.GameRoom;
import com.ruoyi.playingmethod.newgame.model.GamePlayer;
import java.math.BigDecimal;

/**
 * 游戏数据持久化Service接口
 */
public interface GamePersistenceService {
    
    /**
     * 创建房间记录
     */
    void createRoom(GameRoom room);
    
    /**
     * 更新房间状态
     */
    void updateRoomStatus(String roomId, String status);
    
    /**
     * 添加玩家记录
     */
    void addPlayer(String roomId, GamePlayer player);
    
    /**
     * 更新玩家状态
     */
    void updatePlayerStatus(String roomId, String userId, boolean isReady);
    
    /**
     * 玩家离开房间
     */
    void playerLeave(String roomId, String userId);
    
    /**
     * 创建回合记录
     * 
     * @return 回合ID
     */
    Long createRound(String roomId, int roundNumber, Long boxId);
    
    /**
     * 更新回合状态
     */
    void updateRoundStatus(Long roundId, String status);
    
    /**
     * 记录开箱结果
     */
    void recordBoxOpen(Long roundId, String roomId, String userId, Long boxId, 
                      String ornamentId, String ornamentName, BigDecimal ornamentValue);
    
    /**
     * 保存游戏结果
     */
    void saveGameResult(GameRoom room);
} 