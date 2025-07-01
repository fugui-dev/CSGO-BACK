package com.ruoyi.playingmethod.newgame.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.ruoyi.playingmethod.newgame.config.GameConfig;
import com.ruoyi.playingmethod.newgame.constants.GameConstants;
import com.ruoyi.playingmethod.newgame.constants.RedisKeyConstants;
import com.ruoyi.playingmethod.newgame.model.GamePlayer;
import com.ruoyi.playingmethod.newgame.model.GameRoom;
import com.ruoyi.playingmethod.newgame.service.GamePersistenceService;
import com.ruoyi.playingmethod.newgame.service.GameResultService;
import com.ruoyi.playingmethod.newgame.service.GameService;
import com.ruoyi.playingmethod.newgame.service.GameRoomService;
import com.ruoyi.playingmethod.newgame.websocket.GameWebSocketHandler;
import com.ruoyi.playingmethod.newgame.websocket.message.WsMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private final TtBoxService boxService;
    private final LotteryMachine lotteryMachine;
    private final TtOrnamentService ornamentService;
    private final TtUserService userService;
    private final GamePersistenceService gamePersistenceService;
    private final ScheduledExecutorService gameScheduledExecutor;
    private final GameConfig gameConfig;
    private final GameResultService gameResultService;
    private final GameRoomService gameRoomService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public GameServiceImpl(RedisCache redisCache, RedisLock redisLock, GameWebSocketHandler webSocketHandler,  TtBoxService boxService, LotteryMachine lotteryMachine, TtOrnamentService ornamentService, TtUserService userService, GamePersistenceService gamePersistenceService,  ScheduledExecutorService gameScheduledExecutor, GameConfig gameConfig, GameResultService gameResultService, GameRoomService gameRoomService, RedisTemplate<String, Object> redisTemplate) {
        this.redisCache = redisCache;
        this.redisLock = redisLock;
        this.webSocketHandler = webSocketHandler;
        this.boxService = boxService;
        this.lotteryMachine = lotteryMachine;
        this.ornamentService = ornamentService;
        this.userService = userService;
        this.gamePersistenceService = gamePersistenceService;
        this.gameScheduledExecutor = gameScheduledExecutor;
        this.gameConfig = gameConfig;
        this.gameResultService = gameResultService;
        this.gameRoomService = gameRoomService;
        this.redisTemplate = redisTemplate;
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
        room.setPlayers(new ConcurrentHashMap<>());
        
        // 添加创建者为第一个玩家
        GamePlayer creator = GamePlayer.fromTtUser(user, 1);
        creator.setUsername(user.getNickName());
        creator.setAvatar(user.getAvatar());
        creator.setReady(false);
        creator.setRobot(false);
        creator.setSeatNumber(1);
        creator.setOwner(true);
        room.getPlayers().put(creator.getUserId(), creator);
        
        // 计算总回合数
        gameRoomService.calculateTotalRounds(room);
        
        // 保存房间信息到缓存
        saveRoom(room);
        
        // 保存用户房间关联
        String userRoomKey = String.format(RedisKeyConstants.KEY_USER_ROOM, user.getUserId());
        redisCache.setCacheObject(userRoomKey, room.getRoomId(), (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 广播房间创建消息
        webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_ROOM_UPDATE, room);
        
        // 持久化房间数据
        gamePersistenceService.createRoom(room);
        
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
     * 处理回合结束
     */
    private void handleRoundEnd(GameRoom room) {
        // 计算玩家价值和胜负
        gameResultService.calculateResults(room, room.getPlayerResults());
        
        // 保存到Redis
        saveRoom(room);
        
        // 广播回合结果
        Map<String, Object> roundResult = new HashMap<>();
        roundResult.put("round", room.getCurrentRound());
        roundResult.put("winners", room.getWinnerIds());
        roundResult.put("losers", room.getLoserIds());
        roundResult.put("values", room.getPlayerValues());
        roundResult.put("ornaments", room.getPlayerResults());
        roundResult.put("isLastRound", room.getCurrentRound() >= room.getTotalRounds());
        webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_ROUND_RESULT, roundResult);
        
        // 记录回合结果
        gameResultService.recordRoundResult(
            room,
            room.getCurrentRound(),
            room.getWinnerIds(),
            room.getLoserIds(),
            room.getPlayerValues()
        );
        
        // 检查是否是最后一轮
        if (room.getCurrentRound() >= room.getTotalRounds()) {
            // 延迟显示最终结果
            gameScheduledExecutor.schedule(() -> {
                endGame(room);
            }, gameConfig.getGameEndDuration(), TimeUnit.MILLISECONDS);
        } else {
            // 延迟一段时间后开始新一轮
            gameScheduledExecutor.schedule(() -> {
                startNewRound(room);
            }, gameConfig.getRoundInterval(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    @Transactional
    public R<GameRoom> joinRoom(TtUser user, String roomId) {
        // 获取房间信息
        GameRoom room = getRoom(roomId);
        if (room == null) {
            return R.fail("房间不存在");
        }
        
        // 检查房间状态
        if (!GameConstants.ROOM_STATUS_WAITING.equals(room.getStatus())) {
            return R.fail("房间已开始游戏");
        }
        
        // 检查是否已满
        if (gameRoomService.isFull(room)) {
            return R.fail("房间已满");
        }
        
        // 检查是否已在房间中
        if (room.getPlayers().containsKey(String.valueOf(user.getUserId()))) {
            return R.fail("已在房间中");
        }

        //扣费
        upgradeAccounting(room.getTotalValue(),user);
        
        // 分配座位号
        int seatNumber = gameRoomService.getNextAvailableSeat(room);
        
        // 添加玩家
        GamePlayer player = GamePlayer.fromTtUser(user, seatNumber);
        player.setUsername(user.getNickName());
        player.setAvatar(user.getAvatar());
        player.setReady(false);
        player.setRobot(false);
        room.getPlayers().put(player.getUserId(), player);
        
        // 保存房间信息
        saveRoom(room);
        
        // 保存用户房间关联
        String userRoomKey = String.format(RedisKeyConstants.KEY_USER_ROOM, user.getUserId());
        redisCache.setCacheObject(userRoomKey, roomId, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 广播玩家加入消息
        webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_PLAYER_JOIN, player);
        
        // 持久化玩家数据
        gamePersistenceService.addPlayer(roomId, player);
        
        return R.ok(room);
    }

    @Override
    public R<Void> leaveRoom(TtUser user, String roomId) {
        // 获取房间信息
        GameRoom room = redisCache.getCacheObject(String.format(RedisKeyConstants.KEY_ROOM, roomId));
        if (room == null) {
            return R.fail("房间不存在");
        }
        
        // 只能在等待状态退出
        if (!GameConstants.ROOM_STATUS_WAITING.equals(room.getStatus())) {
            return R.fail("游戏已开始,无法退出");
        }
        
        // 倒计时中不能离开房间
        if (gameRoomService.isCountingDown(room)) {
            return R.fail("倒计时中，无法离开房间");
        }
        
        // 移除玩家
        GamePlayer player = room.getPlayers().remove(user.getUserId().toString());
        if (player == null) {
            return R.fail("未在房间中");
        }
        
        // 如果房间空了,删除房间
        if (room.getPlayers().isEmpty()) {
            redisCache.deleteObject(String.format(RedisKeyConstants.KEY_ROOM, roomId));
            redisCache.deleteObject(String.format(RedisKeyConstants.KEY_ROOM_SPECTATOR, roomId));
        } else {
            // 更新房间信息
            redisCache.setCacheObject(String.format(RedisKeyConstants.KEY_ROOM, roomId), room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
            // 广播玩家离开消息
            WsMessage<String> message = WsMessage.create(
                GameConstants.WS_TYPE_PLAYER_LEAVE,
                room.getRoomId(),
                user.getUserId().toString()
            );
            webSocketHandler.broadcastToRoom(room, message.getType(), message.getData());
        }
        
        // 清理用户房间关联
        redisCache.deleteObject(String.format(RedisKeyConstants.KEY_USER_ROOM, user.getUserId()));
        
        // 持久化玩家离开记录
        gamePersistenceService.playerLeave(roomId, user.getUserId().toString());
        
        return R.ok();
    }

    @Override
    public R<Void> ready(TtUser user, String roomId) {
        GameRoom room = getRoom(roomId);
        if (room == null) {
            return R.fail("房间不存在");
        }
        
        GamePlayer player = room.getPlayers().get(String.valueOf(user.getUserId()));
        if (player == null) {
            return R.fail("玩家不在房间中");
        }
        
        // 设置准备状态
        player.setReady(true);
        
        // 保存房间信息
        saveRoom(room);
        
        // 广播准备消息
        Map<String, Object> readyData = new HashMap<>();
        readyData.put("roomId", roomId);
        readyData.put("playerId", player.getUserId());
        readyData.put("ready", true);
        webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_PLAYER_READY, readyData);
        
        // 检查是否所有人都准备好了
        if (gameRoomService.isAllReady(room) && room.getPlayers().size() >= GameConstants.MIN_PLAYERS) {
            // 启动倒计时
            startGameCountdown(room);
        }
        
        // 持久化玩家状态
        gamePersistenceService.updatePlayerStatus(roomId, user.getUserId().toString(), true);
        
        return R.ok();
    }

    /**
     * 启动游戏开始倒计时
     */
    private void startGameCountdown(GameRoom room) {
        // 设置倒计时状态
        room.setCountingDown(true);
        room.setCountdownSeconds(gameConfig.getStartCountdown());
        saveRoom(room);

        // 创建倒计时任务
        gameScheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                // 获取最新的房间状态
                GameRoom currentRoom = getRoom(room.getRoomId());
                if (currentRoom == null || !GameConstants.ROOM_STATUS_WAITING.equals(currentRoom.getStatus())) {
                    // 房间不存在或状态已改变，取消倒计时
                    throw new InterruptedException("Room state changed");
                }

                int remaining = currentRoom.getCountdownSeconds() - 1;
                currentRoom.setCountdownSeconds(remaining);
                saveRoom(currentRoom);

                // 广播倒计时状态
                Map<String, Object> countdownData = new HashMap<>();
                countdownData.put("roomId", currentRoom.getRoomId());
                countdownData.put("remainingSeconds", remaining);
                webSocketHandler.broadcastToRoom(currentRoom, GameConstants.WS_TYPE_COUNTDOWN, countdownData);

                if (remaining <= 0) {
                    // 倒计时结束，开始游戏
                    currentRoom.setCountingDown(false);
                    saveRoom(currentRoom);
                    startGame(null, currentRoom.getRoomId());
                    throw new InterruptedException("Countdown finished");
                }
            } catch (InterruptedException e) {
                // 正常结束倒计时
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Countdown error for room {}", room.getRoomId(), e);
                // 发生异常，通知客户端
                webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_ERROR, "游戏开始失败，请重试");
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> startGame(TtUser user, String roomId) {
        // 获取房间信息
        GameRoom room = redisCache.getCacheObject(String.format(RedisKeyConstants.KEY_ROOM, roomId));
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
        if (!gameRoomService.isAllReady(room)) {
            return R.fail("还有玩家未准备");
        }
        
        try {
            // 更新房间状态
            room.setStatus(GameConstants.ROOM_STATUS_PLAYING);
            room.setStartTime(new Date());
            
            // 初始化游戏数据
            initializeGameData(room);
            
            // 更新房间信息到缓存
            redisCache.setCacheObject(String.format(RedisKeyConstants.KEY_ROOM, roomId), room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
            
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
        // 创建新回合
        room.setCurrentRound(room.getCurrentRound() + 1);
        
        // 获取当前回合的盲盒配置
        GameRoom.BoxConfig currentBox = gameRoomService.getCurrentBoxConfig(room);
        if (currentBox == null) {
            log.error("No box config found for round {} in room {}", room.getCurrentRound(), room.getRoomId());
            return;
        }
        
        // 重置开箱状态
        gameRoomService.resetOpeningStatus(room);
        
        // 保存到Redis
        saveRoom(room);
        
        // 持久化回合数据
        Long roundId = gamePersistenceService.createRound(
            room.getRoomId(), 
            room.getCurrentRound(),
            currentBox.getBoxId()
        );
        
        // 保存roundId到Redis，用于后续开箱记录
        String roundKey = RedisKeyConstants.KEY_CURRENT_ROUND + room.getRoomId();
        redisCache.setCacheObject(roundKey, roundId);
        
        // 广播新回合开始
        Map<String, Object> roundInfo = new HashMap<>();
        roundInfo.put("round", room.getCurrentRound());
        roundInfo.put("box", currentBox);
        webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_ROUND_START, roundInfo);

        // 自动执行开箱
        for (GamePlayer player : room.getPlayers().values()) {
           performBoxOpening(room, player, currentBox);
        }
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
                TtOrnament ornament = gameResultService.getOrnamentInfo(ornamentId);
                if (ornament != null) {
                    // 更新当前回合结果
                    room.getCurrentRoundResults().put(player.getUserId(), ornament);
                    
                    // 创建开箱结果消息
                    Map<String, Object> resultMessage = new HashMap<>();
                    resultMessage.put("roomId", room.getRoomId());
                    resultMessage.put("playerId", player.getUserId());
                    resultMessage.put("round", room.getCurrentRound());
                    resultMessage.put("boxId", boxConfig.getBoxId());
                    resultMessage.put("ornament", ornament);
                    
                    // 广播开箱结果
                    webSocketHandler.broadcastToRoom(room, GameConstants.WS_TYPE_BOX_RESULT, resultMessage);

                    // 延迟更新开箱状态，等待动画播放完成
                    gameScheduledExecutor.schedule(() -> {
                        room.getPlayerOpeningStatus().put(player.getUserId(), true);
                        saveRoom(room);

                        // 检查是否所有玩家都完成开箱
                        if (isAllPlayersFinished(room)) {
                            // 保存回合结果
                            Map<String, TtOrnament> roundResults = new HashMap<>(room.getCurrentRoundResults());
                            room.getAllRoundResults().put(room.getCurrentRound(), roundResults);
                            room.getCurrentRoundResults().clear();
                            
                            // 延迟显示回合结果
                            gameScheduledExecutor.schedule(() -> {
                                handleRoundEnd(room);
                            }, gameConfig.getRoundResultDuration(), TimeUnit.MILLISECONDS);
                        }
                    }, gameConfig.getBoxOpeningDuration(), TimeUnit.MILLISECONDS);
                }
            }
        } catch (Exception e) {
            log.error("Box opening failed for player {} in room {}", player.getUserId(), room.getRoomId(), e);
        }
    }

    /**
     * 获取当前回合的箱子配置
     */
    private GameRoom.BoxConfig getCurrentBoxConfig(GameRoom room) {
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
        return gameRoomService.isAllPlayersFinished(room);
    }

    /**
     * 初始化游戏数据
     */
    private void initializeGameData(GameRoom room) {
        // 计算总回合数（所有箱子的count总和）
        room.setCurrentRound(1);
        gameRoomService.calculateTotalRounds(room); // 使用GameRoomService中的方法计算总回合数

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
        GameRoom room = redisCache.getCacheObject(String.format(RedisKeyConstants.KEY_ROOM, roomId));
        if (room == null) {
            return R.fail("房间不存在");
        }
        return R.ok(room);
    }

    @Override
    public R<GameRoom[]> getRoomList(Integer mode, Integer status) {
        // 获取所有房间key
        Collection<String> roomKeys = Optional.ofNullable(redisCache.keys(RedisKeyConstants.KEY_ROOM.replace("%s", "*")))
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
        GameRoom room = redisCache.getCacheObject(String.format(RedisKeyConstants.KEY_ROOM, roomId));
        if (room == null) {
            return R.fail("房间不存在");
        }
        
        // 检查房间状态
        if (!GameConstants.ROOM_STATUS_WAITING.equals(room.getStatus())) {
            return R.fail("房间状态错误");
        }
        
        // 检查是否已满
        if (gameRoomService.isFull(room)) {
            return R.fail("房间已满");
        }
        
        // 生成机器人ID
        int robotId = -room.getPlayers().size() - 1;  // 使用负数作为机器人ID
        
        // 分配座位号
        int seatNumber = gameRoomService.getNextAvailableSeat(room);
        
        // 添加机器人
        GamePlayer robot = GamePlayer.createRobot(robotId, seatNumber);
        room.getPlayers().put(String.valueOf(robotId), robot);
        
        // 更新房间信息
        redisCache.setCacheObject(String.format(RedisKeyConstants.KEY_ROOM, roomId), room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
        
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
        if (gameRoomService.isAllReady(room) && room.getPlayers().size() >= GameConstants.MIN_PLAYERS) {
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
        String spectatorKey = String.format(RedisKeyConstants.KEY_ROOM_SPECTATOR, roomId);
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
            room.getPlayerResults();

            
            // 4. 通知所有玩家游戏结束
            WsMessage<GameRoom> message = WsMessage.create(
                GameConstants.WS_TYPE_GAME_END,
                room.getRoomId(),
                room
            );
            webSocketHandler.broadcastToRoom(room, message.getType(), message.getData());
            
            // 5. 清理房间缓存
            String roomKey = String.format(RedisKeyConstants.KEY_ROOM, room.getRoomId());
            redisCache.deleteObject(roomKey);
            redisCache.deleteObject(String.format(RedisKeyConstants.KEY_ROOM_SPECTATOR, room.getRoomId()));
            
            // 6. 清理玩家房间关联
            for (String playerId : room.getPlayers().keySet()) {
                String userRoomKey = String.format(RedisKeyConstants.KEY_USER_ROOM, playerId);
                redisCache.deleteObject(userRoomKey);
            }
            
            // 7. 持久化游戏结果
            gamePersistenceService.saveGameResult(room);
            
            return true;
        } catch (Exception e) {
            log.error("End game failed for room {}", room.getRoomId(), e);
            return false;
        }
    }

    @Override
    public GameRoom getUserRoom(String userId) {
        String userRoomKey = String.format(RedisKeyConstants.KEY_USER_ROOM, userId);
        String roomId = redisCache.getCacheObject(userRoomKey);
        if (roomId == null) {
            return null;
        }
        return getRoom(roomId);
    }

    @Override
    public List<GameRoom> getRoomList(int page, int size) {
        // 获取所有房间key
        Collection<String> roomKeys = Optional.ofNullable(redisCache.keys(RedisKeyConstants.KEY_ROOM.replace("%s", "*")))
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
        String roomKey = String.format(RedisKeyConstants.KEY_ROOM, room.getRoomId());
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
            String roomKey = String.format(RedisKeyConstants.KEY_ROOM, room.getRoomId());
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
        if (gameRoomService.isFull(room)) {
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
        if (!gameRoomService.canStart(room)) {
            return false;
        }

        // 检查所有玩家是否准备就绪
        return gameRoomService.isAllReady(room);
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
        return redisCache.getCacheObject(String.format(RedisKeyConstants.KEY_ROOM, roomId));
    }

    /**
     * 获取安慰奖饰品ID（价值0.01）
     */
    private String getConsolationOrnamentId() {

        LambdaQueryWrapper<TtOrnament> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TtOrnament::getPrice, BigDecimal.valueOf(0.01))
                .last("limit 1");

        TtOrnament ornament = ornamentService.getOne(queryWrapper);
        return ornament == null ? null : ornament.getId().toString();
    }

    /**
     * 生成房间ID
     */
    private String generateRoomId() {
        // 生成6位随机数字
        int randomNum = (int) ((Math.random() * 9 + 1) * 100000);
        String roomId = String.valueOf(randomNum);
        
        // 检查房间ID是否已存在
        while (redisCache.getCacheObject(String.format(RedisKeyConstants.KEY_ROOM, roomId)) != null) {
            randomNum = (int) ((Math.random() * 9 + 1) * 100000);
            roomId = String.valueOf(randomNum);
        }
        
        return roomId;
    }

    private void saveRoom(GameRoom room) {
        String roomKey = String.format(RedisKeyConstants.KEY_ROOM, room.getRoomId());
        redisCache.setCacheObject(roomKey, room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
    }


}