package com.ruoyi.playingmethod.newgame.service.impl;

import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.admin.util.core.fight.LotteryMachine;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.playingmethod.newgame.config.GameConfig;
import com.ruoyi.playingmethod.newgame.constants.GameConstants;
import com.ruoyi.playingmethod.newgame.model.GamePlayer;
import com.ruoyi.playingmethod.newgame.model.GameRoom;
import com.ruoyi.playingmethod.newgame.service.GameResultService;
import com.ruoyi.playingmethod.newgame.service.GameRoomService;
import com.ruoyi.playingmethod.newgame.websocket.GameWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ruoyi.playingmethod.newgame.constants.GameConstants.GAME_MODE_POOR;
import static com.ruoyi.playingmethod.newgame.constants.GameConstants.GAME_MODE_RICH;

@Slf4j
@Service
public class GameResultServiceImpl implements GameResultService {

    private final TtOrnamentService ornamentService;
    private final LotteryMachine lotteryMachine;
    private final GameWebSocketHandler webSocketHandler;
    private final ScheduledExecutorService gameScheduledExecutor;
    private final GameConfig gameConfig;
    private final GameRoomService gameRoomService;

    @Autowired
    public GameResultServiceImpl(TtOrnamentService ornamentService, 
                               LotteryMachine lotteryMachine,
                               GameWebSocketHandler webSocketHandler,
                               ScheduledExecutorService gameScheduledExecutor,
                               GameConfig gameConfig,
                               GameRoomService gameRoomService) {
        this.ornamentService = ornamentService;
        this.lotteryMachine = lotteryMachine;
        this.webSocketHandler = webSocketHandler;
        this.gameScheduledExecutor = gameScheduledExecutor;
        this.gameConfig = gameConfig;
        this.gameRoomService = gameRoomService;
    }

    @Override
    public void calculateResults(GameRoom room, Map<String, List<String>> playerOrnaments) {
        Map<String, BigDecimal> values = new HashMap<>();
        
        // 计算每个玩家的总价值
        for (Map.Entry<String, List<String>> entry : playerOrnaments.entrySet()) {
            String playerId = entry.getKey();
            List<String> ornamentIds = entry.getValue();
            
            BigDecimal totalValue = ornamentIds.stream()
                .map(this::getOrnamentInfo)
                .filter(Objects::nonNull)
                .map(TtOrnament::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            values.put(playerId, totalValue);
        }
        
        // 更新房间的玩家价值
        room.setPlayerValues(values);
        
        // 根据游戏模式确定胜利者和失败者
        if (GAME_MODE_RICH.equals(room.getGameMode())) {
            // 欧皇模式：最高价值获胜
            calculateRichModeWinners(room, values);
        } else if (GAME_MODE_POOR.equals(room.getGameMode())) {
            // 非酋模式：最低价值获胜
            calculatePoorModeWinners(room, values);
        } else {
            // 普通模式：每个人保留自己的开箱结果
            room.setWinnerIds(new ArrayList<>(room.getPlayers().keySet()));
            room.setLoserIds(Collections.emptyList());
        }
    }

    @Override
    public TtOrnament getOrnamentInfo(String ornamentId) {
        return ornamentService.getById(ornamentId);
    }

    @Override
    public void recordRoundResult(GameRoom room, int round, List<String> winners, List<String> losers, Map<String, BigDecimal> values) {
        room.getRoundWinners().put(round, winners);
        room.getRoundLosers().put(round, losers);
        room.getRoundValues().put(round, values);
    }

    private void calculateRichModeWinners(GameRoom room, Map<String, BigDecimal> values) {
        BigDecimal maxValue = values.values().stream()
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
            
        List<String> winners = values.entrySet().stream()
            .filter(e -> e.getValue().compareTo(maxValue) == 0)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
            
        List<String> losers = values.entrySet().stream()
            .filter(e -> e.getValue().compareTo(maxValue) != 0)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
            
        room.setWinnerIds(winners);
        room.setLoserIds(losers);
    }

    private void calculatePoorModeWinners(GameRoom room, Map<String, BigDecimal> values) {
        BigDecimal minValue = values.values().stream()
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
            
        List<String> winners = values.entrySet().stream()
            .filter(e -> e.getValue().compareTo(minValue) == 0)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
            
        List<String> losers = values.entrySet().stream()
            .filter(e -> e.getValue().compareTo(minValue) != 0)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
            
        room.setWinnerIds(winners);
        room.setLoserIds(losers);
    }


} 