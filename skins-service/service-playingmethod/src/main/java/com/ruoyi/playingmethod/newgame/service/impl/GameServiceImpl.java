package com.ruoyi.playingmethod.newgame.service.impl;

import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.admin.util.core.fight.LotteryMachine;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.playingmethod.newgame.constants.GameConstants;
import com.ruoyi.playingmethod.newgame.model.GamePlayer;
import com.ruoyi.playingmethod.newgame.model.GameRoom;
import com.ruoyi.playingmethod.newgame.model.message.BoxOpeningMessage;
import com.ruoyi.playingmethod.newgame.model.message.BoxResultMessage;
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

    public GameServiceImpl(RedisCache redisCache, RedisLock redisLock, GameWebSocketHandler webSocketHandler, GameHistoryService gameHistoryService, TtBoxService boxService, LotteryMachine lotteryMachine) {
        this.redisCache = redisCache;
        this.redisLock = redisLock;
        this.webSocketHandler = webSocketHandler;
        this.gameHistoryService = gameHistoryService;
        this.boxService = boxService;
        this.lotteryMachine = lotteryMachine;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<GameRoom> createRoom(TtUser user, int maxPlayers, int mode, int[] boxIds, int[] boxCounts) {
        // 参数验证
        if (maxPlayers < GameConstants.MIN_PLAYERS || maxPlayers > GameConstants.MAX_PLAYERS) {
            return R.fail("玩家数量必须在2-4之间");
        }
        if (boxIds == null || boxCounts == null || boxIds.length != boxCounts.length) {
            return R.fail("箱子配置错误");
        }
        if (boxIds.length == 0) {
            return R.fail("箱子配置不能为空");
        }
        
        // 验证箱子是否存在并创建配置
        List<GameRoom.BoxConfig> boxConfigs = new ArrayList<>();
        for (int i = 0; i < boxIds.length; i++) {
            TtBox box = boxService.getById(boxIds[i]);
            if (box == null) {
                return R.fail("箱子不存在: " + boxIds[i]);
            }
            if (boxCounts[i] <= 0) {
                return R.fail("开箱次数必须大于0");
            }
            GameRoom.BoxConfig config = new GameRoom.BoxConfig();
            config.setBoxId((long) boxIds[i]);
            config.setCount(boxCounts[i]);
            boxConfigs.add(config);
        }
        
        // 创建房间
        GameRoom room = new GameRoom();
        room.setRoomId(UUID.randomUUID().toString());
        room.setStatus(GameConstants.ROOM_STATUS_WAITING);
        room.setGameMode(mode == 0 ? GameConstants.GAME_MODE_RICH : GameConstants.GAME_MODE_POOR);
        room.setMaxPlayers(maxPlayers);
        room.setMinPlayers(GameConstants.MIN_PLAYERS);
        room.setCreatorId(user.getUserId().toString());
        room.setCreateTime(new Date());
        room.setBoxConfigs(boxConfigs);
        room.calculateTotalRounds();
        
        // 添加创建者为第一个玩家
        GamePlayer creator = new GamePlayer();
        creator.setUserId(user.getUserId().toString());
        creator.setUsername(user.getNickName());
        creator.setAvatar(user.getAvatar());
        creator.setReady(false);
        creator.setRobot(false);
        creator.setSeatNumber(1);  // 创建者默认坐第一个位置
        room.getPlayers().put(creator.getUserId(), creator);
        
        // 保存房间信息到缓存
        String roomKey = String.format(GameConstants.ROOM_CACHE_KEY, room.getRoomId());
        redisCache.setCacheObject(roomKey, room, (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 保存用户房间关联
        String userRoomKey = String.format(GameConstants.USER_ROOM_CACHE_KEY, user.getUserId());
        redisCache.setCacheObject(userRoomKey, room.getRoomId(), (int) GameConstants.ROOM_EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 广播房间创建消息
        WsMessage<GameRoom> message = WsMessage.create(
            GameConstants.WS_TYPE_ROOM_UPDATE,
            room.getRoomId(),
            room
        );
        webSocketHandler.broadcastToRoom(room, message.getType(), message.getData());
        
        return R.ok(room);
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
            startGame(user, roomId);
        }
        
        return R.ok();
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
            WsMessage<GameRoom> message = WsMessage.create(
                GameConstants.WS_TYPE_GAME_START,
                room.getRoomId(),
                room
            );
            webSocketHandler.broadcastToRoom(room, message.getType(), message.getData());
            
            return R.ok();
        } catch (Exception e) {
            log.error("Start game failed for room {}", roomId, e);
            return R.fail("游戏开始失败");
        }
    }

    /**
     * 初始化游戏数据
     */
    private void initializeGameData(GameRoom room) {
        // 初始化当前回合
        room.setCurrentRound(1);
        
        // 初始化玩家开箱状态
        Map<String, Boolean> openingStatus = new HashMap<>();
        for (String playerId : room.getPlayers().keySet()) {
            openingStatus.put(playerId, false);
        }
        room.setPlayerOpeningStatus(openingStatus);
        
        // 初始化玩家结果
        Map<String, List<String>> results = new HashMap<>();
        for (String playerId : room.getPlayers().keySet()) {
            results.put(playerId, new ArrayList<>());
        }
        room.setPlayerResults(results);
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

    private GameRoom getRoom(String roomId) {
        if (roomId == null) {
            return null;
        }
        return redisCache.getCacheObject(String.format(GameConstants.ROOM_CACHE_KEY, roomId));
    }
} 