package com.ruoyi.playingmethod.newgame.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.playingmethod.newgame.model.GameRoom;
import com.ruoyi.playingmethod.newgame.service.GameService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "新版对战模式")
@Slf4j
@RestController
@RequestMapping("/api/v2/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    private TtUser getCurrentUser(HttpServletRequest request) {
        return (TtUser) request.getAttribute("currentUser");
    }

    @ApiOperation("创建房间")
    @PostMapping("/room/create")
    public R<GameRoom> createRoom(@RequestBody CreateRoomRequest request, HttpServletRequest httpRequest) {
        TtUser user = getCurrentUser(httpRequest);
        return gameService.createRoom(
            user,
            request.getMaxPlayers(),
            request.getMode(),
            request.getBoxIds(),
            request.getBoxCounts()
        );
    }

    @ApiOperation("加入房间")
    @PostMapping("/room/join")
    public R<GameRoom> joinRoom(@RequestParam String roomId, HttpServletRequest request) {
        TtUser user = getCurrentUser(request);
        return gameService.joinRoom(user, roomId);
    }

    @ApiOperation("退出房间")
    @PostMapping("/room/leave")
    public R<Void> leaveRoom(@RequestParam String roomId, HttpServletRequest request) {
        TtUser user = getCurrentUser(request);
        return gameService.leaveRoom(user, roomId);
    }

    @ApiOperation("准备游戏")
    @PostMapping("/room/ready")
    public R<Void> ready(@RequestParam String roomId, HttpServletRequest request) {
        TtUser user = getCurrentUser(request);
        return gameService.ready(user, roomId);
    }

    @ApiOperation("开始游戏")
    @PostMapping("/room/start")
    public R<Void> startGame(@RequestParam String roomId, HttpServletRequest request) {
        TtUser user = getCurrentUser(request);
        return gameService.startGame(user, roomId);
    }

    @ApiOperation("获取房间信息")
    @GetMapping("/room/info")
    public R<GameRoom> getRoomInfo(@RequestParam String roomId) {
        return gameService.getRoomInfo(roomId);
    }

    @ApiOperation("获取房间列表")
    @GetMapping("/room/list")
    public R<GameRoom[]> getRoomList(
            @RequestParam(required = false) Integer mode,
            @RequestParam(required = false) Integer status) {
        return gameService.getRoomList(mode, status);
    }

    @ApiOperation("邀请机器人")
    @PostMapping("/room/invite-robot")
    public R<GameRoom> inviteRobot(@RequestParam String roomId, HttpServletRequest request) {
        TtUser user = getCurrentUser(request);
        return gameService.inviteRobot(user, roomId);
    }

    @ApiOperation("观战")
    @PostMapping("/room/spectate")
    public R<GameRoom> spectate(@RequestParam String roomId, HttpServletRequest request) {
        TtUser user = getCurrentUser(request);
        return gameService.spectate(user, roomId);
    }
}

// 创建房间请求
class CreateRoomRequest {
    private int maxPlayers;
    private int mode;
    private int[] boxIds;
    private int[] boxCounts;

    // Getters and setters
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public int getMode() { return mode; }
    public void setMode(int mode) { this.mode = mode; }
    public int[] getBoxIds() { return boxIds; }
    public void setBoxIds(int[] boxIds) { this.boxIds = boxIds; }
    public int[] getBoxCounts() { return boxCounts; }
    public void setBoxCounts(int[] boxCounts) { this.boxCounts = boxCounts; }
} 