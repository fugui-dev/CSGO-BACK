package com.ruoyi.playingmethod.newgame.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.admin.util.core.fight.LotteryMachine;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.playingmethod.newgame.constants.GameConstants;
import com.ruoyi.playingmethod.newgame.model.GamePlayer;
import com.ruoyi.playingmethod.newgame.model.GameRoom;
import com.ruoyi.playingmethod.newgame.service.GameHistoryService;
import com.ruoyi.playingmethod.newgame.service.GameService;
import com.ruoyi.playingmethod.newgame.websocket.GameWebSocketHandler;
import com.ruoyi.playingmethod.newgame.websocket.message.WsMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameServiceImpl implements GameService {

    private final RedisCache redisCache;
    private final RedisLock redisLock;
    private final GameWebSocketHandler webSocketHandler;
    private final GameHistoryService gameHistoryService;
    private final TtBoxService boxService;
    private final LotteryMachine lotteryMachine;
    private final TtOrnamentService ornamentService;
    private final TtUserService userService;

    public GameServiceImpl(RedisCache redisCache, RedisLock redisLock, GameWebSocketHandler webSocketHandler, GameHistoryService gameHistoryService, TtBoxService boxService, LotteryMachine lotteryMachine, TtOrnamentService ornamentService, TtUserService userService) {
        this.redisCache = redisCache;
        this.redisLock = redisLock;
        this.webSocketHandler = webSocketHandler;
        this.gameHistoryService = gameHistoryService;
        this.boxService = boxService;
        this.lotteryMachine = lotteryMachine;
        this.ornamentService = ornamentService;
        this.userService = userService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<GameRoom> createRoom(TtUser user, GameRoom room) {
        // 验证房间配置
        if (room.getBoxConfigs().size() > 15) {
            webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_ERROR, "每场最多选择15个盲盒");
            return R.fail("每场最多选择15个盲盒");
        }
        
        // 验证玩家数量
        int playerCount = room.getMaxPlayers();
        if (playerCount != 2 && playerCount != 3 && playerCount != 4) { // 4用于2V2模式
            return R.fail("房间人数只能是2人/3人/2V2");
        }

        // 计算入场费用（盲盒总价值）
        BigDecimal totalCost = calculateTotalBoxCost(room.getBoxConfigs());

        //扣费
        upgradeAccounting(totalCost,user);

        room.setTotalValue(totalCost);

        // 生成房间ID
        String roomId = generateRoomId();
        room.setRoomId(roomId);
        room.setStatus(GameConstants.ROOM_STATUS_WAITING);
        room.setCreateTime(new Date());
        room.setPlayers(new HashMap<>());
        
        // 添加创建者为第一个玩家
        GamePlayer creator = GamePlayer.fromTtUser(user, 1);
        creator.setUsername(user.getNickName());
        creator.setAvatar(user.getAvatar());
        creator.setReady(false);
        creator.setRobot(false);
        creator.setSeatNumber(1);
        creator.setOwner(true);
        room.getPlayers().put(creator.getUserId(), creator);
        
        // 保存房间信息到缓存
        String roomKey = String.format(GameConstants.ROOM_CACHE_KEY, room.getRoomId());
        redisCache.setCacheObject(roomKey, room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 保存用户房间关联
        String userRoomKey = String.format(GameConstants.USER_ROOM_CACHE_KEY, user.getUserId());
        redisCache.setCacheObject(userRoomKey, room.getRoomId(), (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 广播房间创建消息
        webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_ROOM_UPDATE, room);
        
        return R.ok(room);
    }

    /**
     * 计算盲盒总价值
     */
    private BigDecimal calculateTotalBoxCost(List<GameRoom.BoxConfig> boxConfigs) {
        BigDecimal totalCost = BigDecimal.ZERO;
        for (GameRoom.BoxConfig config : boxConfigs) {
            TtBox box = boxService.getById(config.getBoxId());
            if (box != null) {
                totalCost = totalCost.add(box.getPrice().multiply(new BigDecimal(config.getCount())));
            }
        }
        return totalCost;
    }

    /**
     * 处理游戏结果
     */
    private void handleGameResult(GameRoom room) {
        // 计算每个玩家的总价值
        Map<String, BigDecimal> playerValues = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : room.getPlayerResults().entrySet()) {
            String playerId = entry.getKey();
            List<String> ornamentIds = entry.getValue();
            
            BigDecimal totalValue = BigDecimal.ZERO;
            for (String ornamentId : ornamentIds) {
                // 使用 ornamentService 获取饰品价格
                TtOrnament ttOrnament = ornamentService.getById(ornamentId);
                if (ttOrnament != null) {
                    totalValue = totalValue.add(ttOrnament.getPrice());
                }
            }
            playerValues.put(playerId, totalValue);
        }

        // 根据游戏模式确定胜利者和失败者
        List<String> winners = new ArrayList<>();
        List<String> losers = new ArrayList<>();
        
        if (GameConstants.GAME_MODE_RICH.equals(room.getGameMode())) {
            // 欧皇模式：价值最高的获胜
            BigDecimal maxValue = playerValues.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            for (Map.Entry<String, BigDecimal> entry : playerValues.entrySet()) {
                if (entry.getValue().compareTo(maxValue) == 0) {
                    winners.add(entry.getKey());
                } else {
                    losers.add(entry.getKey());
                }
            }
        } else {
            // 非酋模式：价值最低的获胜
            BigDecimal minValue = playerValues.values().stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            for (Map.Entry<String, BigDecimal> entry : playerValues.entrySet()) {
                if (entry.getValue().compareTo(minValue) == 0) {
                    winners.add(entry.getKey());
                } else {
                    losers.add(entry.getKey());
                }
            }
        }

        // 重新分配饰品
        redistributeOrnaments(room, winners, losers);

        // 保存游戏结果
        room.setWinnerIds(winners);
        room.setStatus(GameConstants.ROOM_STATUS_ENDED);
        room.setEndTime(new Date());

        // 广播游戏结果
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("winners", winners);
        resultData.put("losers", losers);
        resultData.put("playerValues", playerValues);
        resultData.put("room", room);
        
        webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_GAME_END, resultData);

        // 保存游戏历史记录
        gameHistoryService.saveHistory(room);
    }

    /**
     * 重新分配饰品
     */
    private void redistributeOrnaments(GameRoom room, List<String> winners, List<String> losers) {
        // 获取0.01价值的安慰奖饰品ID
        String consolationOrnamentId = getConsolationOrnamentId();
        
        // 收集所有失败者的饰品
        List<String> allLoserOrnaments = new ArrayList<>();
        for (String loserId : losers) {
            allLoserOrnaments.addAll(room.getPlayerResults().get(loserId));
            // 给失败者设置安慰奖
            List<String> consolationList = new ArrayList<>();
            consolationList.add(consolationOrnamentId);
            room.getPlayerResults().put(loserId, consolationList);
        }
        
        // 将失败者的饰品分配给胜者（保留胜者原有饰品）
        if (winners.size() > 1) {
            // 多个胜者时平均分配失败者饰品
            distributeOrnaments(room, winners, allLoserOrnaments);
        } else if (!winners.isEmpty()) {
            // 单个胜者时直接添加所有失败者饰品
            List<String> winnerOrnaments = room.getPlayerResults().get(winners.get(0));
            winnerOrnaments.addAll(allLoserOrnaments);
            room.getPlayerResults().put(winners.get(0), winnerOrnaments);
        }
    }

    /**
     * 平均分配饰品
     */
    private void distributeOrnaments(GameRoom room, List<String> players, List<String> ornaments) {
        int playerCount = players.size();
        if (playerCount == 0 || ornaments.isEmpty()) {
            return;
        }

        // 按价值排序饰品
        ornaments.sort((o1, o2) -> {
            TtOrnament ornament1 = ornamentService.getById(o1);
            TtOrnament ornament2 = ornamentService.getById(o2);
            if (ornament1 == null || ornament2 == null) {
                return 0;
            }
            return ornament2.getPrice().compareTo(ornament1.getPrice());
        });

        // 平均分配
        for (int i = 0; i < ornaments.size(); i++) {
            String playerId = players.get(i % playerCount);
            room.getPlayerResults().computeIfAbsent(playerId, k -> new ArrayList<>())
                .add(ornaments.get(i));
        }
    }

    @Override
    @Transactional
    public R<GameRoom> joinRoom(TtUser user, String roomId) {
        // 获取房间信息
        GameRoom room = redisCache.getCacheObject(GameConstants.ROOM_CACHE_KEY + roomId);
        if (room == null) {
            return R.fail("房间不存在");
        }
        
        // 检查房间状态
        if (room.getStatus() != GameConstants.ROOM_STATUS_WAITING) {
            return R.fail("房间已开始游戏");
        }
        
        // 检查是否已满
        if (room.isFull()) {
            return R.fail("房间已满");
        }
        
        // 检查是否已在房间中
        if (room.getPlayers().containsKey(String.valueOf(user.getUserId()))) {
            return R.fail("已在房间中");
        }

        //扣费
        upgradeAccounting(room.getTotalValue(),user);
        
        // 分配座位号
        int seatNumber = 1;
        boolean seatTaken;
        do {
            final int currentSeat = seatNumber;
            seatTaken = false;
            for (GamePlayer p : room.getPlayers().values()) {
                if (p.getSeatNumber() == currentSeat) {
                    seatTaken = true;
                    break;
                }
            }
            if (!seatTaken) {
                break;
            }
            seatNumber++;
        } while (seatNumber <= GameConstants.MAX_PLAYERS);
        
        // 添加玩家
        GamePlayer player = GamePlayer.fromTtUser(user, seatNumber);
        room.getPlayers().put(String.valueOf(user.getUserId()), player);
        
        // 更新房间信息
        redisCache.setCacheObject(GameConstants.ROOM_CACHE_KEY + roomId, room);
        
        // 广播玩家加入消息
        webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_PLAYER_JOIN, player);
        
        return R.ok(room);
    }

    @Override
    public R<Void> leaveRoom(TtUser user, String roomId) {
        // 获取房间信息
        GameRoom room = redisCache.getCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId));
        if (room == null) {
            return R.fail("房间不存在");
        }
        
        // 只能在等待状态退出
        if (!GameConstants.ROOM_STATUS_WAITING.equals(room.getStatus())) {
            return R.fail("游戏已开始,无法退出");
        }
        
        // 倒计时中不能离开房间
        if (room.isCountingDown()) {
            return R.fail("倒计时中，无法离开房间");
        }
        
        // 移除玩家
        GamePlayer player = room.getPlayers().remove(user.getUserId().toString());
        if (player == null) {
            return R.fail("未在房间中");
        }
        
        // 如果房间空了,删除房间
        if (room.getPlayers().isEmpty()) {
            redisCache.deleteObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId));
            redisCache.deleteObject(String.format(GameConstants.ROOM_SPECTATOR_KEY, roomId));
        } else {
            // 更新房间信息
            redisCache.setCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId), room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
            // 广播玩家离开消息
            WsMessage<String> message = WsMessage.create(
                GameConstants.WS_TYPE_PLAYER_LEAVE,
                room.getRoomId(),
                user.getUserId().toString()
            );
            webSocketHandler.broadcastToRoom(room, message.getType(), message.getData());
        }
        
        // 清理用户房间关联
        redisCache.deleteObject(String.format(GameConstants.USER_ROOM_CACHE_KEY, user.getUserId()));
        
        return R.ok();
    }

    @Override
    public R<Void> ready(TtUser user, String roomId) {
        // 获取房间信息
        GameRoom room = redisCache.getCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId));
        if (room == null) {
            return R.fail("房间不存在");
        }
        
        // 检查玩家是否在房间中
        GamePlayer player = room.getPlayers().get(user.getUserId().toString());
        if (player == null) {
            return R.fail("未在房间中");
        }
        
        // 更新准备状态
        player.setReady(true);
        redisCache.setCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId), room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 广播准备状态
        WsMessage<GamePlayer> message = WsMessage.create(
            GameConstants.WS_TYPE_PLAYER_READY,
            room.getRoomId(),
            player
        );
        webSocketHandler.broadcastToRoom(room, message.getType(), message.getData());
        
        // 检查是否所有人都准备好了
        if (room.isAllReady() && room.getPlayers().size() >= GameConstants.MIN_PLAYERS) {
                // 启动倒计时
                startGameCountdown(room);
            }
        
        return R.ok();
    }

    /**
     * 启动游戏开始倒计时
     */
    private void startGameCountdown(GameRoom room) {
        // 设置倒计时状态
        room.setCountingDown(true);
        room.setCountdownSeconds(GameConstants.GAME_START_COUNTDOWN);
        redisCache.setCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, room.getRoomId()), room);

        // 创建倒计时任务
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                int remaining = room.getCountdownSeconds() - 1;
                room.setCountdownSeconds(remaining);
                redisCache.setCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, room.getRoomId()), room);

                // 广播倒计时状态
                Map<String, Object> countdownData = new HashMap<>();
                countdownData.put("roomId", room.getRoomId());
                countdownData.put("remainingSeconds", remaining);
                webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_COUNTDOWN, countdownData);

                if (remaining <= 0) {
                    executor.shutdown();
                    room.setCountingDown(false);
                    // 倒计时结束，开始游戏
                    startGame(null, room.getRoomId());
                }
            } catch (Exception e) {
                log.error("Countdown error for room {}", room.getRoomId(), e);
                executor.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> startGame(TtUser user, String roomId) {
        // 获取房间信息
        GameRoom room = redisCache.getCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId));
        if (room == null) {
            return R.fail("房间不存在");
        }
        
        // 检查房间状态
        if (!GameConstants.ROOM_STATUS_WAITING.equals(room.getStatus())) {
            return R.fail("房间状态错误");
        }
        
        // 检查玩家数量
        if (room.getPlayers().size() < GameConstants.MIN_PLAYERS) {
            return R.fail("玩家数量不足");
        }
        
        // 检查是否所有人都准备好了
        if (!room.isAllReady()) {
            return R.fail("还有玩家未准备");
        }
        
        try {
            // 更新房间状态
            room.setStatus(GameConstants.ROOM_STATUS_PLAYING);
            room.setStartTime(new Date());
            
            // 初始化游戏数据
            initializeGameData(room);
            
            // 更新房间信息到缓存
            redisCache.setCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId), room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
            
            // 广播游戏开始消息
            Map<String, Object> startData = new HashMap<>();
            startData.put("room", room);
            startData.put("totalRounds", room.getTotalRounds());
            webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_GAME_START, startData);

            // 开始第一轮开箱
            startNewRound(room);
            
            return R.ok();
        } catch (Exception e) {
            log.error("Start game failed for room {}", roomId, e);
            return R.fail("游戏开始失败");
        }
    }

    /**
     * 开始新一轮开箱
     */
    private void startNewRound(GameRoom room) {
        // 重置玩家开箱状态
        Map<String, Boolean> openingStatus = new HashMap<>();
        for (String playerId : room.getPlayers().keySet()) {
            openingStatus.put(playerId, false);
        }
        room.setPlayerOpeningStatus(openingStatus);

        // 获取当前回合的箱子配置
        GameRoom.BoxConfig currentBox = getCurrentRoundBox(room);
        if (currentBox == null) {
            // 如果没有箱子配置，说明游戏结束
            handleGameResult(room);
            return;
        }
        
        // 为每个玩家执行开箱
        for (Map.Entry<String, GamePlayer> entry : room.getPlayers().entrySet()) {
            String playerId = entry.getKey();
            GamePlayer player = entry.getValue();
            
            // 执行开箱
            performBoxOpening(room, player, currentBox);
        }

        // 更新房间信息
        redisCache.setCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, room.getRoomId()), room);
    }

    /**
     * 执行开箱操作
     */
    private void performBoxOpening(GameRoom room, GamePlayer player, GameRoom.BoxConfig boxConfig) {
        try {
            // 获取箱子信息
            TtBox box = boxService.getById(boxConfig.getBoxId());
            if (box == null) {
                log.error("Box not found: {}", boxConfig.getBoxId());
                return;
            }

            // 执行抽奖
            TtUser tempUser = new TtUser();
            tempUser.setUserId(Integer.parseInt(player.getUserId()));
            tempUser.setUserType(player.isRobot() ? "02" : "01");
            tempUser.setIsAnchorShow(false);

            String ornamentId = lotteryMachine.singleLottery(tempUser, box, player.isRobot());

            if (ornamentId != null) {
                List<String> playerResults = room.getPlayerResults().computeIfAbsent(player.getUserId(), k -> new ArrayList<>());
                playerResults.add(ornamentId);

                // 获取饰品信息
                TtOrnament ornament = ornamentService.getById(ornamentId);
                if (ornament != null) {
                    // 创建开箱结果消息
                    Map<String, Object> resultMessage = new HashMap<>();
                    resultMessage.put("roomId", room.getRoomId());
                    resultMessage.put("playerId", player.getUserId());
                    resultMessage.put("round", room.getCurrentRound());
                    resultMessage.put("boxId", boxConfig.getBoxId());
                    resultMessage.put("ornament", ornament);
                    
                    // 广播开箱结果
                    webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_BOX_RESULT, resultMessage);
                }
            }

            // 更新玩家开箱状态
            room.getPlayerOpeningStatus().put(player.getUserId(), true);

            // 检查是否所有玩家都完成开箱
            if (isAllPlayersFinished(room)) {
                handleRoundEnd(room);
            }

        } catch (Exception e) {
            log.error("Box opening failed for player {} in room {}", player.getUserId(), room.getRoomId(), e);
        }
    }

    /**
     * 处理回合结束
     */
    private void handleRoundEnd(GameRoom room) {
        try {
            // 计算当前回合的结果
            Map<String, BigDecimal> currentRoundValues = new HashMap<>();
            Map<String, List<String>> currentRoundOrnaments = new HashMap<>();
            
            // 获取当前回合每个玩家开出的饰品
            for (Map.Entry<String, List<String>> entry : room.getPlayerResults().entrySet()) {
                String playerId = entry.getKey();
                List<String> ornaments = entry.getValue();
                
                // 只取最后一个饰品（当前回合开出的）
                if (!ornaments.isEmpty()) {
                    String currentOrnamentId = ornaments.get(ornaments.size() - 1);
                    TtOrnament ttOrnament = ornamentService.getById(currentOrnamentId);
                    if (ttOrnament != null) {
                        currentRoundValues.put(playerId, ttOrnament.getPrice());
                        currentRoundOrnaments.put(playerId, Collections.singletonList(currentOrnamentId));
                    }
                }
            }

            // 确定当前回合的胜利者和失败者
            List<String> roundWinners = new ArrayList<>();
            List<String> roundLosers = new ArrayList<>();

            if (room.getMaxPlayers() == 4) {
                // 2V2模式：计算每个队伍的总价值
                Map<Integer, BigDecimal> teamValues = new HashMap<>();
                Map<Integer, List<String>> teamPlayers = new HashMap<>();

                // 计算每个队伍的总价值和玩家列表
                for (Map.Entry<String, GamePlayer> entry : room.getPlayers().entrySet()) {
                    String playerId = entry.getKey();
                    GamePlayer player = entry.getValue();
                    int teamNumber = player.getTeamNumber();
                    
                    // 累加队伍价值
                    BigDecimal playerValue = currentRoundValues.getOrDefault(playerId, BigDecimal.ZERO);
                    teamValues.merge(teamNumber, playerValue, BigDecimal::add);
                    
                    // 记录队伍玩家
                    teamPlayers.computeIfAbsent(teamNumber, k -> new ArrayList<>()).add(playerId);
                }

                if (GameConstants.GAME_MODE_RICH.equals(room.getGameMode())) {
                    // 欧皇模式：总价值高的队伍获胜
                    BigDecimal team1Value = teamValues.getOrDefault(1, BigDecimal.ZERO);
                    BigDecimal team2Value = teamValues.getOrDefault(2, BigDecimal.ZERO);
                    
                    if (team1Value.compareTo(team2Value) >= 0) {
                        roundWinners.addAll(teamPlayers.get(1));
                        roundLosers.addAll(teamPlayers.get(2));
                    } else {
                        roundWinners.addAll(teamPlayers.get(2));
                        roundLosers.addAll(teamPlayers.get(1));
                    }
                } else {
                    // 非酋模式：总价值低的队伍获胜
                    BigDecimal team1Value = teamValues.getOrDefault(1, BigDecimal.ZERO);
                    BigDecimal team2Value = teamValues.getOrDefault(2, BigDecimal.ZERO);
                    
                    if (team1Value.compareTo(team2Value) <= 0) {
                        roundWinners.addAll(teamPlayers.get(1));
                        roundLosers.addAll(teamPlayers.get(2));
                    } else {
                        roundWinners.addAll(teamPlayers.get(2));
                        roundLosers.addAll(teamPlayers.get(1));
                    }
                }
            } else {
                // 2人或3人模式：按个人价值判断
                if (GameConstants.GAME_MODE_RICH.equals(room.getGameMode())) {
                    // 欧皇模式：价值最高的获胜
                    BigDecimal maxValue = currentRoundValues.values().stream()
                            .max(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);
                    
                    for (Map.Entry<String, BigDecimal> entry : currentRoundValues.entrySet()) {
                        if (entry.getValue().compareTo(maxValue) == 0) {
                            roundWinners.add(entry.getKey());
                        } else {
                            roundLosers.add(entry.getKey());
                        }
                    }
                } else {
                    // 非酋模式：价值最低的获胜
                    BigDecimal minValue = currentRoundValues.values().stream()
                            .min(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);
                    
                    for (Map.Entry<String, BigDecimal> entry : currentRoundValues.entrySet()) {
                        if (entry.getValue().compareTo(minValue) == 0) {
                            roundWinners.add(entry.getKey());
                        } else {
                            roundLosers.add(entry.getKey());
                        }
                    }
                }
            }

            // 记录回合结果
            room.recordRoundResult(room.getCurrentRound(), roundWinners, roundLosers, currentRoundValues);

            // 重新分配当前回合的饰品
            redistributeRoundOrnaments(room, roundWinners, roundLosers, currentRoundOrnaments);

            // 广播回合结果
            Map<String, Object> roundResult = new HashMap<>();
            roundResult.put("round", room.getCurrentRound());
            roundResult.put("winners", roundWinners);
            roundResult.put("losers", roundLosers);
            roundResult.put("values", currentRoundValues);
            roundResult.put("ornaments", currentRoundOrnaments);
            if (room.getMaxPlayers() == 4) {
                // 2V2模式：添加队伍信息
                Map<Integer, BigDecimal> teamValues = new HashMap<>();
                for (Map.Entry<String, GamePlayer> entry : room.getPlayers().entrySet()) {
                    String playerId = entry.getKey();
                    GamePlayer player = entry.getValue();
                    BigDecimal value = currentRoundValues.getOrDefault(playerId, BigDecimal.ZERO);
                    teamValues.merge(player.getTeamNumber(), value, BigDecimal::add);
                }
                roundResult.put("teamValues", teamValues);
            }
            roundResult.put("isLastRound", room.getCurrentRound() >= room.getTotalRounds());
            webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_ROUND_RESULT, roundResult);

            // 更新当前回合
            room.setCurrentRound(room.getCurrentRound() + 1);

            // 更新房间信息到缓存
            redisCache.setCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, room.getRoomId()), room);

            // 检查是否是最后一轮
            if (room.getCurrentRound() > room.getTotalRounds()) {
                // 游戏结束，处理最终结果
                handleGameResult(room);
            } else {
                // 延迟一段时间后开始新一轮
                Thread.sleep(3000); // 延迟3秒
                
                // 开始新一轮
                startNewRound(room);
            }
        } catch (InterruptedException e) {
            log.error("Delay before next round failed for room {}", room.getRoomId(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Handle round end failed for room {}", room.getRoomId(), e);
            // 尝试恢复游戏状态
            try {
                startNewRound(room);
            } catch (Exception ex) {
                log.error("Failed to recover game state for room {}", room.getRoomId(), ex);
                // 如果恢复失败，结束游戏
                handleGameResult(room);
            }
        }
    }

    /**
     * 重新分配当前回合的饰品
     */
    private void redistributeRoundOrnaments(GameRoom room, List<String> winners, List<String> losers, 
            Map<String, List<String>> currentRoundOrnaments) {
        if (GameConstants.GAME_MODE_RICH.equals(room.getGameMode())) {
            // 欧皇模式：失败者的饰品分给胜利者
            List<String> winnerOrnaments = new ArrayList<>();
            
            // 收集失败者的饰品
            for (String loserId : losers) {
                List<String> loserOrnaments = currentRoundOrnaments.get(loserId);
                if (loserOrnaments != null) {
                    winnerOrnaments.addAll(loserOrnaments);
                    // 从玩家总结果中移除这些饰品
                    room.getPlayerResults().get(loserId).removeAll(loserOrnaments);
                }
            }
            
            // 分配给胜利者
            if (winners.size() > 1) {
                // 多个胜利者平均分配
                distributeOrnaments(room, winners, winnerOrnaments);
            } else if (!winners.isEmpty()) {
                // 单个胜利者获得所有
                String winnerId = winners.get(0);
                room.getPlayerResults().get(winnerId).addAll(winnerOrnaments);
            }
        } else {
            // 非酋模式：胜利者的饰品分给失败者
            List<String> loserOrnaments = new ArrayList<>();
            
            // 收集胜利者的饰品
            for (String winnerId : winners) {
                List<String> winnerOrnaments = currentRoundOrnaments.get(winnerId);
                if (winnerOrnaments != null) {
                    loserOrnaments.addAll(winnerOrnaments);
                    // 从玩家总结果中移除这些饰品
                    room.getPlayerResults().get(winnerId).removeAll(winnerOrnaments);
                }
            }
            
            // 分配给失败者
            if (!losers.isEmpty()) {
                distributeOrnaments(room, losers, loserOrnaments);
            }
        }
    }

    /**
     * 获取当前回合的箱子配置
     */
    private GameRoom.BoxConfig getCurrentRoundBox(GameRoom room) {
        int currentRound = room.getCurrentRound();
        int roundCount = 0;
        
        for (GameRoom.BoxConfig config : room.getBoxConfigs()) {
            int boxRounds = config.getCount(); // 这个箱子要开几回合
            roundCount += boxRounds;
            
            if (currentRound <= roundCount) {
                return config;
            }
        }
        
        return null;
    }

    /**
     * 检查是否所有玩家都完成开箱
     */
    private boolean isAllPlayersFinished(GameRoom room) {
        return room.getPlayerOpeningStatus().values().stream().allMatch(status -> status);
    }

    /**
     * 初始化游戏数据
     */
    private void initializeGameData(GameRoom room) {
        // 计算总回合数（所有箱子的count总和）
        room.setCurrentRound(1);
        room.calculateTotalRounds(); // 使用GameRoom中的方法计算总回合数

        // 初始化玩家结果
        room.setPlayerResults(new HashMap<>());
        for (String playerId : room.getPlayers().keySet()) {
            room.getPlayerResults().put(playerId, new ArrayList<>());
        }

        // 初始化开箱状态
        Map<String, Boolean> openingStatus = new HashMap<>();
        for (String playerId : room.getPlayers().keySet()) {
            openingStatus.put(playerId, false);
        }
        room.setPlayerOpeningStatus(openingStatus);

        // 初始化回合结果记录
        room.setRoundWinners(new HashMap<>());
        room.setRoundLosers(new HashMap<>());
        room.setRoundValues(new HashMap<>());
    }

    @Override
    public R<GameRoom> getRoomInfo(String roomId) {
        GameRoom room = redisCache.getCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId));
        if (room == null) {
            return R.fail("房间不存在");
        }
        return R.ok(room);
    }

    @Override
    public R<GameRoom[]> getRoomList(Integer mode, Integer status) {
        // 获取所有房间key
        Collection<String> roomKeys = Optional.ofNullable(redisCache.keys(GameConstants.ROOM_CACHE_KEY.replace("%s", "*")))
                .orElse(Collections.emptySet());
        
        if (roomKeys.isEmpty()) {
            return R.ok(new GameRoom[0]);
        }

        // 获取所有房间信息并过滤
        List<GameRoom> rooms = roomKeys.stream()
                .map(key -> {
                    GameRoom room = redisCache.getCacheObject(key);
                    if (room == null) {
                        return null;
                    }
                    // 过滤游戏模式
                    if (mode != null && !mode.toString().equals(room.getGameMode())) {
                        return null;
                    }
                    // 过滤房间状态
                    if (status != null && !status.toString().equals(room.getStatus())) {
                        return null;
                    }
                    return room;
                })
                .filter(Objects::nonNull)
                .sorted((r1, r2) -> r2.getCreateTime().compareTo(r1.getCreateTime()))
                .collect(Collectors.toList());

        return R.ok(rooms.toArray(new GameRoom[0]));
    }

    @Override
    public R<GameRoom> inviteRobot(TtUser user, String roomId) {
        // 获取房间信息
        GameRoom room = redisCache.getCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId));
        if (room == null) {
            return R.fail("房间不存在");
        }
        
        // 检查房间状态
        if (!GameConstants.ROOM_STATUS_WAITING.equals(room.getStatus())) {
            return R.fail("房间状态错误");
        }
        
        // 检查是否已满
        if (room.isFull()) {
            return R.fail("房间已满");
        }
        
        // 生成机器人ID
        int robotId = -room.getPlayers().size() - 1;  // 使用负数作为机器人ID
        
        // 分配座位号
        int seatNumber = 1;
        boolean seatTaken;
        do {
            final int currentSeat = seatNumber;
            seatTaken = false;
            for (GamePlayer p : room.getPlayers().values()) {
                if (p.getSeatNumber() == currentSeat) {
                    seatTaken = true;
                    break;
                }
            }
            if (!seatTaken) {
                break;
            }
            seatNumber++;
        } while (seatNumber <= GameConstants.MAX_PLAYERS);
        
        // 添加机器人
        GamePlayer robot = GamePlayer.createRobot(robotId, seatNumber);
        room.getPlayers().put(String.valueOf(robotId), robot);
        
        // 更新房间信息
        redisCache.setCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId), room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 广播机器人加入消息
        WsMessage<GamePlayer> message = WsMessage.create(
            GameConstants.WS_TYPE_PLAYER_JOIN,
            room.getRoomId(),
            robot
        );
        webSocketHandler.broadcastToRoom(room, message.getType(), message.getData());
        
        // 机器人自动准备
        robot.setReady(true);
        webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_PLAYER_READY, robot);
        
        // 检查是否所有人都准备好了
        if (room.isAllReady() && room.getPlayers().size() >= GameConstants.MIN_PLAYERS) {
            startGame(user, roomId);
        }
        
        return R.ok(room);
    }

    @Override
    public R<GameRoom> spectate(TtUser user, String roomId) {
        GameRoom room = getRoom(roomId);
        if (room == null) {
            return R.fail("房间不存在");
        }

        // 检查房间是否允许观战
        if (!room.isAllowSpectators()) {
            return R.fail("该房间不允许观战");
        }

        // 检查房间状态
        if (GameConstants.ROOM_STATUS_ENDED.equals(room.getStatus())) {
            return R.fail("游戏已结束");
        }

        // 检查用户是否已经在房间中
        if (room.getPlayers().containsKey(user.getUserId().toString())) {
            return R.fail("您已经是房间玩家,无需观战");
        }

        // 检查观战人数是否已满
        String spectatorKey = String.format(GameConstants.ROOM_SPECTATOR_KEY, roomId);
        Set<String> spectators = redisCache.getCacheSet(spectatorKey);
        if (spectators != null && spectators.size() >= GameConstants.MAX_SPECTATORS) {
            return R.fail("观战人数已满");
        }

        // 记录观战信息
        if (spectators == null) {
            spectators = new HashSet<>();
        }
        spectators.add(user.getUserId().toString());
        redisCache.setCacheSet(spectatorKey, spectators);

        // 通知房间内玩家有新观众加入
        Map<String, Object> spectatorInfo = new HashMap<>();
        spectatorInfo.put("userId", user.getUserId().toString());
        spectatorInfo.put("username", user.getNickName());
        spectatorInfo.put("avatar", user.getAvatar());
        
        WsMessage<Map<String, Object>> message = WsMessage.create(
            GameConstants.WS_TYPE_SPECTATOR_JOIN,
            room.getRoomId(),
            spectatorInfo
        );
        webSocketHandler.broadcastToRoom(room, message.getType(), message.getData());

        return R.ok(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endGame(GameRoom room) {
        try {
            // 1. 更新房间状态
            room.setStatus(GameConstants.ROOM_STATUS_ENDED);
            
            // 2. 计算游戏结果
            Map<String, List<String>> finalResults = room.getPlayerResults();
            
            // 3. 保存游戏历史记录
            gameHistoryService.saveHistory(room);
            
            // 4. 通知所有玩家游戏结束
            WsMessage<GameRoom> message = WsMessage.create(
                GameConstants.WS_TYPE_GAME_END,
                room.getRoomId(),
                room
            );
            webSocketHandler.broadcastToRoom(room, message.getType(), message.getData());
            
            // 5. 清理房间缓存
            String roomKey = String.format(GameConstants.ROOM_CACHE_KEY, room.getRoomId());
            redisCache.deleteObject(roomKey);
            redisCache.deleteObject(String.format(GameConstants.ROOM_SPECTATOR_KEY, room.getRoomId()));
            
            // 6. 清理玩家房间关联
            for (String playerId : room.getPlayers().keySet()) {
                String userRoomKey = String.format(GameConstants.USER_ROOM_CACHE_KEY, playerId);
                redisCache.deleteObject(userRoomKey);
            }
            
            return true;
        } catch (Exception e) {
            log.error("End game failed for room {}", room.getRoomId(), e);
            return false;
        }
    }

    @Override
    public GameRoom getUserRoom(String userId) {
        String userRoomKey = String.format(GameConstants.USER_ROOM_CACHE_KEY, userId);
        String roomId = redisCache.getCacheObject(userRoomKey);
        if (roomId == null) {
            return null;
        }
        return getRoom(roomId);
    }

    @Override
    public List<GameRoom> getRoomList(int page, int size) {
        // 获取所有房间key
        Collection<String> roomKeys = Optional.ofNullable(redisCache.keys(GameConstants.ROOM_CACHE_KEY.replace("%s", "*")))
                .orElse(Collections.emptySet());
                
        if (roomKeys.isEmpty()) {
            return new ArrayList<>();
        }

        // 过滤掉已结束的房间并获取房间信息
        List<GameRoom> activeRooms = roomKeys.stream()
                .map(key -> {
                    try {
                        return redisCache.getCacheObject(key);
                    } catch (Exception e) {
                        log.error("Failed to get room from redis, key: {}", key, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(obj -> {
                    if (obj instanceof GameRoom) {
                        return (GameRoom) obj;
                    }
                    log.error("Invalid room object type in redis: {}", obj.getClass());
                    return null;
                })
                .filter(Objects::nonNull)
                .filter(room -> !GameConstants.ROOM_STATUS_ENDED.equals(room.getStatus()))
                .sorted((r1, r2) -> r2.getCreateTime().compareTo(r1.getCreateTime()))
                .collect(Collectors.toList());

        // 分页处理
        int start = (page - 1) * size;
        int end = Math.min(start + size, activeRooms.size());
        
        if (start >= activeRooms.size()) {
            return new ArrayList<>();
        }
        
        return activeRooms.subList(start, end);
    }

    @Override
    public boolean addRobots(GameRoom room, int count) {
        if (room == null || count <= 0) {
            return false;
        }

        // 检查房间是否已满
        if (room.getPlayers().size() + count > room.getMaxPlayers()) {
            return false;
        }

        // 添加机器人
        for (int i = 0; i < count; i++) {
            GamePlayer robot = new GamePlayer();
            robot.setUserId("robot_" + UUID.randomUUID().toString());
            robot.setUsername("Robot_" + (i + 1));
            robot.setRobot(true);
            robot.setReady(true);
            room.getPlayers().put(robot.getUserId(), robot);
        }

        // 更新房间缓存
        String roomKey = String.format(GameConstants.ROOM_CACHE_KEY, room.getRoomId());
        redisCache.setCacheObject(roomKey, room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);

        return true;
    }

    @Override
    public boolean removeRobot(GameRoom room, final String robotId) {
        if (room == null || robotId == null) {
            return false;
        }

        // 移除机器人
        GamePlayer removedPlayer = room.getPlayers().remove(robotId);
        boolean removed = removedPlayer != null && removedPlayer.isRobot();

        if (removed) {
            // 更新房间缓存
            String roomKey = String.format(GameConstants.ROOM_CACHE_KEY, room.getRoomId());
            redisCache.setCacheObject(roomKey, room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
        }

        return removed;
    }

    @Override
    public boolean canJoinRoom(GameRoom room, TtUser user) {
        if (room == null || user == null) {
            return false;
        }

        // 检查房间状态
        if (!GameConstants.ROOM_STATUS_WAITING.equals(room.getStatus())) {
            return false;
        }

        // 检查房间是否已满
        if (room.isFull()) {
            return false;
        }

        // 检查用户是否已在其他房间
        if (getUserRoom(user.getUserId().toString()) != null) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canStartGame(GameRoom room, String userId) {
        if (room == null || userId == null) {
            return false;
        }

        // 检查是否是房主
        if (!userId.equals(room.getCreatorId())) {
            return false;
        }

        // 检查房间状态
        if (!GameConstants.ROOM_STATUS_WAITING.equals(room.getStatus())) {
            return false;
        }

        // 检查人数是否达到最小要求
        if (!room.canStart()) {
            return false;
        }

        // 检查所有玩家是否准备就绪
        return room.isAllPlayersFinished();
    }

    public R<Map<String, BigDecimal>> upgradeAccounting(BigDecimal consumption, TtUser player) {

        // 再次检查余额
        player = userService.getById(player.getUserId());
        if (player.getAccountAmount().add(player.getAccountCredits()).compareTo(consumption) < 0) {
            return R.fail("余额不足");
        }

        LambdaUpdateWrapper<TtUser> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate.eq(TtUser::getUserId, player.getUserId());

        Map<String, BigDecimal> map;
        if (player.getAccountAmount().compareTo(consumption) >= 0) {
            userUpdate.set(TtUser::getAccountAmount, player.getAccountAmount().subtract(consumption));
            map = MapUtil.builder("Amount", consumption).map();
        } else {
            BigDecimal subtract = consumption.subtract(player.getAccountAmount());
            userUpdate
                    .set(TtUser::getAccountAmount, 0)
                    .set(TtUser::getAccountCredits, player.getAccountCredits().subtract(subtract));
            map = MapUtil.builder("Amount", player.getAccountAmount()).map();
            map.put("Credits", subtract);
        }

        userService.update(userUpdate);

        return R.ok(map);
    }

    private GameRoom getRoom(String roomId) {
        if (roomId == null) {
            return null;
        }
        return redisCache.getCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId));
    }

    /**
     * 获取安慰奖饰品ID（价值0.01）
     */
    private String getConsolationOrnamentId() {
        // 实际实现可能需要查询数据库或配置文件
        // 这里假设存在一个固定ID为"consolation_001"的安慰奖饰品
        return "consolation_001";
    }

    /**
     * 生成房间ID
     */
    private String generateRoomId() {
        // 生成6位随机数字
        int randomNum = (int) ((Math.random() * 9 + 1) * 100000);
        String roomId = String.valueOf(randomNum);
        
        // 检查房间ID是否已存在
        while (redisCache.getCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId)) != null) {
            randomNum = (int) ((Math.random() * 9 + 1) * 100000);
            roomId = String.valueOf(randomNum);
        }
        
        return roomId;
    }
}