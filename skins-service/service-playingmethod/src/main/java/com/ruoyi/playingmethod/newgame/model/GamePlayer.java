package com.ruoyi.playingmethod.newgame.model;

import com.ruoyi.domain.entity.sys.TtUser;
import lombok.Data;


/**
 * 游戏玩家
 */
@Data
public class GamePlayer {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 座位号
     */
    private int seatNumber;
    
    /**
     * 是否准备
     */
    private boolean ready;
    
    /**
     * 是否是机器人
     */
    private boolean robot;
    
    /**
     * 从TtUser创建GamePlayer
     */
    public static GamePlayer fromTtUser(TtUser user, int seatNumber) {
        GamePlayer player = new GamePlayer();
        player.setUserId(user.getUserId().toString());
        player.setUsername(user.getNickName());
        player.setAvatar(user.getAvatar());
        player.setSeatNumber(seatNumber);
        player.setReady(false);
        player.setRobot(false);
        return player;
    }
    
    // 创建机器人玩家
    public static GamePlayer createRobot(int robotId, int seatNumber) {
        GamePlayer player = new GamePlayer();
        player.setUserId(robotId + "");
        player.setUsername("Robot-" + robotId);
        player.setAvatar("robot.png");
        player.setSeatNumber(seatNumber);
        player.setRobot(true);
        player.setReady(true);  // 机器人自动准备
        return player;
    }
} 