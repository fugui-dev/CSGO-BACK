package com.ruoyi.playingmethod.newgame.service.impl;

import com.ruoyi.playingmethod.newgame.constants.GameConstants;
import com.ruoyi.playingmethod.newgame.model.GamePlayer;
import com.ruoyi.playingmethod.newgame.model.GameRoom;
import com.ruoyi.playingmethod.newgame.service.GameRoomService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameRoomServiceImpl implements GameRoomService {
    
    @Override
    public boolean isFull(GameRoom room) {
        return room.getPlayers().size() >= room.getMaxPlayers();
    }
    
    @Override
    public boolean isAllReady(GameRoom room) {
        return room.getPlayers().values().stream()
                .allMatch(GamePlayer::isReady);
    }
    
    @Override
    public boolean canStart(GameRoom room) {
        return room.getPlayers().size() >= room.getMinPlayers() && isAllReady(room);
    }
    
    @Override
    public void calculateTotalRounds(GameRoom room) {
        int totalRounds = room.getBoxConfigs().stream()
                .mapToInt(GameRoom.BoxConfig::getCount)
                .sum();
        room.setTotalRounds(totalRounds);
    }
    
    @Override
    public boolean isAllPlayersFinished(GameRoom room) {
        return room.getPlayers().values().stream()
                .allMatch(player -> 
                    player.isRobot() || 
                    Boolean.TRUE.equals(room.getPlayerOpeningStatus().get(player.getUserId()))
                );
    }
    
    @Override
    public void resetOpeningStatus(GameRoom room) {
        room.getPlayerOpeningStatus().clear();
        room.getPlayers().values().forEach(player -> 
            room.getPlayerOpeningStatus().put(player.getUserId(), false)
        );
    }
    
    @Override
    public GameRoom.BoxConfig getCurrentBoxConfig(GameRoom room) {
        int roundCount = 0;
        for (GameRoom.BoxConfig config : room.getBoxConfigs()) {
            roundCount += config.getCount();
            if (room.getCurrentRound() <= roundCount) {
                return config;
            }
        }
        return null;
    }



    @Override
    public int getNextAvailableSeat(GameRoom room) {
        int seatNumber = 1;
        while (seatNumber <= GameConstants.MAX_PLAYERS) {
            final int currentSeat = seatNumber;
            boolean seatTaken = room.getPlayers().values().stream()
                .anyMatch(p -> p.getSeatNumber() == currentSeat);
            
            if (!seatTaken) {
                return seatNumber;
            }
            seatNumber++;
        }
        throw new IllegalStateException("No available seats in room " + room.getRoomId());
    }

    @Override
    public boolean isCountingDown(GameRoom room) {
        return room.isCountingDown();
    }
} 