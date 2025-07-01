package com.ruoyi.playingmethod.newgame.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.domain.entity.game.*;
import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.playingmethod.newgame.mapper.*;
import com.ruoyi.playingmethod.newgame.model.GameRoom;
import com.ruoyi.playingmethod.newgame.model.GamePlayer;
import com.ruoyi.playingmethod.newgame.service.GamePersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 游戏数据持久化Service实现
 */
@Slf4j
@Service
public class GamePersistenceServiceImpl implements GamePersistenceService {

    @Autowired
    private TtGameRoomMapper roomMapper;
    
    @Autowired
    private TtGamePlayerMapper playerMapper;
    
    @Autowired
    private TtGameRoomBoxMapper roomBoxMapper;
    
    @Autowired
    private TtGameRoundMapper roundMapper;
    
    @Autowired
    private TtGameBoxRecordMapper boxRecordMapper;
    
    @Autowired
    private TtGameResultMapper resultMapper;
    
    @Autowired
    private TtBoxService boxService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRoom(GameRoom room) {
        try {
            // 1. 保存房间基本信息
            TtGameRoom gameRoom = new TtGameRoom();
            gameRoom.setRoomId(room.getRoomId());
            gameRoom.setStatus("waiting");
            gameRoom.setGameMode(room.getGameMode());
            gameRoom.setMaxPlayers(room.getMaxPlayers());
            gameRoom.setTotalRounds(room.getTotalRounds());
            gameRoom.setAllowSpectators(room.isAllowSpectators());
            gameRoom.setCreatorId(room.getCreatorId());
            gameRoom.setCreateTime(new Date());
            roomMapper.insert(gameRoom);
            
            // 2. 保存房间盲盒配置
            int sortOrder = 1;
            for (GameRoom.BoxConfig boxConfig : room.getBoxConfigs()) {
                // 获取盲盒信息
                TtBox box = boxService.getById(boxConfig.getBoxId());
                if (box == null) {
                    log.error("Box not found: {}", boxConfig.getBoxId());
                    continue;
                }
                
                TtGameRoomBox roomBox = new TtGameRoomBox();
                roomBox.setRoomId(room.getRoomId());
                roomBox.setBoxId(boxConfig.getBoxId());
                roomBox.setBoxName(box.getBoxName());  // 从TtBox获取名称
                roomBox.setBoxPrice(box.getPrice()); // 从TtBox获取价格
                roomBox.setBoxCount(boxConfig.getCount());
                roomBox.setSortOrder(sortOrder++);
                roomBoxMapper.insert(roomBox);
            }
            
            // 3. 保存创建者玩家记录
            GamePlayer creator = room.getPlayers().get(room.getCreatorId());
            if (creator != null) {
                addPlayer(room.getRoomId(), creator);
            }
        } catch (Exception e) {
            log.error("Failed to create room record for room {}", room.getRoomId(), e);
            throw e;
        }
    }

    @Override
    public void updateRoomStatus(String roomId, String status) {
        try {
            TtGameRoom room = roomMapper.selectById(roomId);
            if (room != null) {
                room.setStatus(status);
                if ("playing".equals(status)) {
                    room.setStartTime(new Date());
                } else if ("ended".equals(status)) {
                    room.setEndTime(new Date());
                }
                roomMapper.updateById(room);
            }
        } catch (Exception e) {
            log.error("Failed to update room status for room {}", roomId, e);
            throw e;
        }
    }

    @Override
    public void addPlayer(String roomId, GamePlayer player) {
        try {
            TtGamePlayer gamePlayer = new TtGamePlayer();
            gamePlayer.setRoomId(roomId);
            gamePlayer.setUserId(player.getUserId());
            gamePlayer.setSeatNumber(player.getSeatNumber());
            gamePlayer.setTeamNumber(player.getTeamNumber());
            gamePlayer.setIsReady(player.isReady());
            gamePlayer.setIsRobot(player.isRobot());
            gamePlayer.setJoinTime(new Date());
            playerMapper.insert(gamePlayer);
        } catch (Exception e) {
            log.error("Failed to add player {} to room {}", player.getUserId(), roomId, e);
            throw e;
        }
    }

    @Override
    public void updatePlayerStatus(String roomId, String userId, boolean isReady) {
        try {
            TtGamePlayer player = playerMapper.selectOne(
                new LambdaQueryWrapper<TtGamePlayer>()
                    .eq(TtGamePlayer::getRoomId, roomId)
                    .eq(TtGamePlayer::getUserId, userId)
                    .isNull(TtGamePlayer::getLeaveTime)
            );
            if (player != null) {
                player.setIsReady(isReady);
                playerMapper.updateById(player);
            }
        } catch (Exception e) {
            log.error("Failed to update player status for player {} in room {}", userId, roomId, e);
            throw e;
        }
    }

    @Override
    public void playerLeave(String roomId, String userId) {
        try {
            TtGamePlayer player = playerMapper.selectOne(
                new LambdaQueryWrapper<TtGamePlayer>()
                    .eq(TtGamePlayer::getRoomId, roomId)
                    .eq(TtGamePlayer::getUserId, userId)
                    .isNull(TtGamePlayer::getLeaveTime)
            );
            if (player != null) {
                player.setLeaveTime(new Date());
                playerMapper.updateById(player);
            }
        } catch (Exception e) {
            log.error("Failed to record player leave for player {} in room {}", userId, roomId, e);
            throw e;
        }
    }

    @Override
    public Long createRound(String roomId, int roundNumber, Long boxId) {
        try {
            TtGameRound round = new TtGameRound();
            round.setRoomId(roomId);
            round.setRoundNumber(roundNumber);
            round.setBoxId(boxId);
            round.setStatus("preparing");
            round.setStartTime(new Date());
            roundMapper.insert(round);
            return round.getRoundId();
        } catch (Exception e) {
            log.error("Failed to create round record for room {} round {}", roomId, roundNumber, e);
            throw e;
        }
    }

    @Override
    public void updateRoundStatus(Long roundId, String status) {
        try {
            TtGameRound round = roundMapper.selectById(roundId);
            if (round != null) {
                round.setStatus(status);
                if ("finished".equals(status)) {
                    round.setEndTime(new Date());
                }
                roundMapper.updateById(round);
            }
        } catch (Exception e) {
            log.error("Failed to update round status for round {}", roundId, e);
            throw e;
        }
    }

    @Override
    public void recordBoxOpen(Long roundId, String roomId, String userId, Long boxId,
                            String ornamentId, String ornamentName, BigDecimal ornamentValue) {
        try {
            TtGameBoxRecord record = new TtGameBoxRecord();
            record.setRoundId(roundId);
            record.setRoomId(roomId);
            record.setUserId(userId);
            record.setBoxId(boxId);
            record.setOrnamentId(ornamentId);
            record.setOrnamentName(ornamentName);
            record.setOrnamentValue(ornamentValue);
            record.setOpenTime(new Date());
            boxRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("Failed to record box open for player {} in round {}", userId, roundId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGameResult(GameRoom room) {
        try {
            // 1. 更新房间状态
            updateRoomStatus(room.getRoomId(), "ended");
            
            // 2. 保存每个玩家的游戏结果
            for (Map.Entry<String, GamePlayer> entry : room.getPlayers().entrySet()) {
                String userId = entry.getKey();
                GamePlayer player = entry.getValue();
                
                // 计算玩家总价值
                BigDecimal totalValue = calculatePlayerTotalValue(room, userId);
                
                TtGameResult result = new TtGameResult();
                result.setRoomId(room.getRoomId());
                result.setUserId(userId);
                result.setTotalValue(totalValue);
                result.setIsWinner(room.getWinnerIds().contains(userId));
                result.setTeamNumber(player.getTeamNumber());
                result.setCreateTime(new Date());
                resultMapper.insert(result);
                
                // 3. 更新玩家离开时间（如果还没离开）
                playerLeave(room.getRoomId(), userId);
            }
            
            // 4. 更新所有未完成的回合状态
            List<TtGameRound> unfinishedRounds = roundMapper.selectList(
                new LambdaQueryWrapper<TtGameRound>()
                    .eq(TtGameRound::getRoomId, room.getRoomId())
                    .ne(TtGameRound::getStatus, "finished")
            );
            for (TtGameRound round : unfinishedRounds) {
                updateRoundStatus(round.getRoundId(), "finished");
            }
        } catch (Exception e) {
            log.error("Failed to save game result for room {}", room.getRoomId(), e);
            throw e;
        }
    }
    
    /**
     * 计算玩家在游戏中的总价值
     */
    private BigDecimal calculatePlayerTotalValue(GameRoom room, String userId) {
        List<TtGameBoxRecord> records = boxRecordMapper.selectList(
            new LambdaQueryWrapper<TtGameBoxRecord>()
                .eq(TtGameBoxRecord::getRoomId, room.getRoomId())
                .eq(TtGameBoxRecord::getUserId, userId)
        );
        
        return records.stream()
                .map(TtGameBoxRecord::getOrnamentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 