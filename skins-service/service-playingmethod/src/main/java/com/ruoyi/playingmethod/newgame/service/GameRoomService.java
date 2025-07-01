package com.ruoyi.playingmethod.newgame.service;

import com.ruoyi.playingmethod.newgame.model.GameRoom;
import java.util.Map;

/**
 * 游戏房间服务
 */
public interface GameRoomService {
    
    /**
     * 计算总回合数
     */
    void calculateTotalRounds(GameRoom room);
    
    /**
     * 检查房间是否已满
     */
    boolean isFull(GameRoom room);
    
    /**
     * 检查所有玩家是否准备就绪
     */
    boolean isAllReady(GameRoom room);
    
    /**
     * 检查是否可以开始游戏
     */
    boolean canStart(GameRoom room);
    
    /**
     * 检查是否正在倒计时
     */
    boolean isCountingDown(GameRoom room);
    
    /**
     * 获取当前回合的盲盒配置
     */
    GameRoom.BoxConfig getCurrentBoxConfig(GameRoom room);
    
    /**
     * 检查是否所有玩家都完成开箱
     */
    boolean isAllPlayersFinished(GameRoom room);
    
    /**
     * 获取下一个可用的座位号
     */
    int getNextAvailableSeat(GameRoom room);

    /**
     * 重置玩家开箱状态
     */
    void resetOpeningStatus(GameRoom room);

} 