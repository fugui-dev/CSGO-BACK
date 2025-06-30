package com.ruoyi.playingmethod.newgame.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.playingmethod.newgame.model.GamePlayer;
import com.ruoyi.playingmethod.newgame.model.GameRoom;

import java.util.List;

public interface GameService {
    
    /**
     * 创建游戏房间
     */
    R<GameRoom> createRoom(TtUser user, GameRoom room);
    
    /**
     * 加入房间
     */
    R<GameRoom> joinRoom(TtUser user, String roomId);
    
    /**
     * 退出房间
     */
    R<Void> leaveRoom(TtUser user, String roomId);
    
    /**
     * 准备游戏
     */
    R<Void> ready(TtUser user, String roomId);
    
    /**
     * 开始游戏
     */
    R<Void> startGame(TtUser user, String roomId);
    
    /**
     * 获取房间信息
     */
    R<GameRoom> getRoomInfo(String roomId);
    
    /**
     * 获取房间列表
     */
    R<GameRoom[]> getRoomList(Integer mode, Integer status);
    
    /**
     * 邀请机器人
     */
    R<GameRoom> inviteRobot(TtUser user, String roomId);
    
    /**
     * 观战
     */
    R<GameRoom> spectate(TtUser user, String roomId);
    
    /**
     * 结束游戏
     * @param room 游戏房间
     * @return 是否成功
     */
    boolean endGame(GameRoom room);
    
    /**
     * 获取用户当前房间
     * @param userId 用户ID
     * @return 房间信息
     */
    GameRoom getUserRoom(String userId);
    
    /**
     * 获取房间列表
     * @param page 页码
     * @param size 每页大小
     * @return 房间列表
     */
    List<GameRoom> getRoomList(int page, int size);
    
    /**
     * 添加机器人到房间
     * @param room 房间
     * @param count 机器人数量
     * @return 是否成功
     */
    boolean addRobots(GameRoom room, int count);
    
    /**
     * 移除机器人
     * @param room 房间
     * @param robotId 机器人ID
     * @return 是否成功
     */
    boolean removeRobot(GameRoom room, String robotId);
    
    /**
     * 检查用户是否可以加入房间
     * @param room 房间
     * @param user 用户
     * @return 是否可以加入
     */
    boolean canJoinRoom(GameRoom room, TtUser user);
    
    /**
     * 检查用户是否可以开始游戏
     * @param room 房间
     * @param userId 用户ID
     * @return 是否可以开始
     */
    boolean canStartGame(GameRoom room, String userId);
} 