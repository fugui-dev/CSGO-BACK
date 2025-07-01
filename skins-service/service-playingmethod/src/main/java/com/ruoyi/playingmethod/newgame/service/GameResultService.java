package com.ruoyi.playingmethod.newgame.service;

import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.playingmethod.newgame.model.GameRoom;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 游戏结果服务
 */
public interface GameResultService {
    
    /**
     * 计算游戏结果
     */
    void calculateResults(GameRoom room, Map<String, List<String>> playerResults);
    
    /**
     * 获取饰品信息
     */
    TtOrnament getOrnamentInfo(String ornamentId);
    
    /**
     * 记录回合结果
     */
    void recordRoundResult(GameRoom room, int round, List<String> winners, List<String> losers, Map<String, BigDecimal> values);
} 