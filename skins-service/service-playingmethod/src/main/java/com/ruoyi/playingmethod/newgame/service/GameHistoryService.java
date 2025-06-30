package com.ruoyi.playingmethod.newgame.service;

import com.ruoyi.playingmethod.newgame.model.GameHistory;
import com.ruoyi.playingmethod.newgame.model.GameRoom;

import java.util.List;

public interface GameHistoryService {
    
    /**
     * 保存游戏历史记录
     * @param room 游戏房间
     * @return 历史记录ID
     */
    String saveHistory(GameRoom room);
    
    /**
     * 获取玩家的游戏历史
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 历史记录列表
     */
    List<GameHistory> getUserHistory(String userId, int page, int size);
    
    /**
     * 获取房间的历史记录
     * @param roomId 房间ID
     * @return 历史记录
     */
    GameHistory getRoomHistory(String roomId);
    
    /**
     * 获取用户的胜率统计
     * @param userId 用户ID
     * @return 胜率(0-1)
     */
    double getUserWinRate(String userId);
    
    /**
     * 获取用户的总收益
     * @param userId 用户ID
     * @return 总收益(可能为负)
     */
    double getUserTotalProfit(String userId);
} 