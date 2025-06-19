package com.ruoyi.playingmethod.websocket;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.common.constant.TtboxRecordSource;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.common.constant.sys.UserStatus;
import com.ruoyi.domain.dto.boxRecords.queryCondition;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import com.ruoyi.playingmethod.service.ApiBoxRecordsService;
import com.ruoyi.playingmethod.websocket.constant.SMsgKey;
import com.ruoyi.playingmethod.websocket.util.WsResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 绑定箱子
 */
@Slf4j
@Component
@ServerEndpoint("/ws/bindBox/{boxId}/{userId}")
public class WsBindBox {

    // 用来记录当前连接数的变量
    private static volatile int onlineCount = 0;
    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    private static ConcurrentHashMap<String, WsBindBox> allRoomUserMap = new ConcurrentHashMap<>();
    // 与某个客户端的连接会话，需要通过它来与客户端进行数据收发
    private Session session;
    private static ApiBoxRecordsService apiBoxRecordsService;
    private static TtBoxService boxService;
    private static TtUserService userService;
    private static TtUserMapper userMapper;
    private Integer userId = null;
    private Integer boxId = null;
    private String key = "";

    @Autowired
    public void ttFightService(ApiBoxRecordsService apiBoxRecordsService){
        WsBindBox.apiBoxRecordsService = apiBoxRecordsService;
    }

    @Autowired
    public void ttFightService(TtBoxService boxService){
        WsBindBox.boxService = boxService;
    }

    @Autowired
    public void ttFightService(TtUserService userService){
        WsBindBox.userService = userService;
    }

    @Autowired
    public void ttFightService(TtUserMapper userMapper){
        WsBindBox.userMapper = userMapper;
    }

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("boxId") Integer boxId,
                       @PathParam("userId") Integer userId) throws IOException {
        // 更新连接池
        addBoxRoomUser(userId, boxId, session);

        // 用户信息
        TtUser player = new LambdaQueryChainWrapper<>(userMapper)
                .eq(TtUser::getUserId, userId)
                .eq(TtUser::getStatus, UserStatus.NORMAL.getCode())
                .eq(TtUser::getDelFlag, 0)
                .one();
        // 检查连接
        R check = connectCheck(player, boxId);
        if (!check.getCode().equals(200)) {
            session.getBasicRemote().sendText(check.getMsg());
            session.close();
            return;
        }

        log.debug("/ws/bindBox > > onOpen");
        log.info("用户{}进入盲盒游戏房间，在线人数{}", userId, WsBindBox.onlineCount);
        sendMessage("用户" + userId + "进入盲盒游戏房间，在线人数" + WsBindBox.onlineCount);

        // 首次连接获取最新的一组盲盒开箱数据
        List<Integer> sources = Arrays.asList(TtboxRecordSource.BLIND_BOX.getCode());
        List<Integer> status = Arrays.asList(
                TtboxRecordStatus.IN_PACKSACK_ON.getCode(),
                TtboxRecordStatus.DELIVERY_YET.getCode(),
                TtboxRecordStatus.APPLY_DELIVERY.getCode(),
                TtboxRecordStatus.RESOLVE.getCode());
        queryCondition param = queryCondition.builder()
                .boxId(boxId)
                .userType(player.getUserType())
                .source(sources)
                .status(status)
                .orderByFie(1)
                .page(1)
                .size(10)
                .build();
        List<TtBoxRecordsVO> ttBoxRecordsVOS = apiBoxRecordsService.byCondition(param);

        sendMessage(WsResult.ok(SMsgKey.Blind_Box_Init_Data.name(),ttBoxRecordsVOS,"初始化历史开箱记录"));
    }

    @OnClose
    public void onClose(Session session) {
        removeRoomUser();
        log.info("关闭连接，正常在线人数：" + WsBindBox.onlineCount);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到消息");
    }

    @OnError
    public void onError(Session session, Throwable exception) throws Exception {
        log.info("出现错误");
    }

    /**
     * 连接检查
     */
    private R connectCheck(TtUser player, Integer boxId) {
        if (ObjectUtil.isNotEmpty(WsFightRoom.allRoomUserMap.get(player.getUserId() + "_" + boxId))) {
            R.fail("用户" + player.getUserId() + "已经连接宝箱房间" + boxId + "。请勿重复调用。");
        }
        TtBox box = boxService.getById(boxId);
        if (ObjectUtil.isEmpty(box)) {
            return R.fail("不存在的宝箱,id：" + boxId);
        }
        return R.ok();
    }

    /**
     * 更新在线人数
     */
    private int addOnlineCount(int number) {
        synchronized (this){
            WsBindBox.onlineCount += number;
            return WsBindBox.onlineCount;
        }
    }

    /**
     * 全局广播
     */
    public static void broadcast(Object message) {

        // ObjectMapper objectMapper = new ObjectMapper();
        String msg = JSON.toJSONString(message);

        Collection<WsBindBox> wss = WsBindBox.allRoomUserMap.values();
        for (WsBindBox ws : wss){
            try {
                ws.session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                log.warn("WS大厅广播消息异常。错误信息：{}", msg);
            }
        }
    }

    /**
     * 房间广播
     */
    public static void broadcastToBoxRoom(Integer boxId, Object message) {

        String msg = JSON.toJSONString(message);

        Collection<WsBindBox> wslist = WsBindBox.allRoomUserMap.values();

        for (WsBindBox ws : wslist) {
            try {
                if (!ws.boxId.equals(boxId)) continue;
                // log.info(String.valueOf(ws.session.isOpen()));
                if (!ws.session.isOpen()) continue;
                RemoteEndpoint.Basic basicRemote = ws.session.getBasicRemote();
                basicRemote.sendText(msg);
            } catch (IOException e) {
                log.warn("WS推送广播给{}_{}消息异常。", ws.userId, ws.boxId);
            }
        }
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(Object message) {
        try {
            this.session.getBasicRemote().sendText(JSON.toJSONString(message));
        } catch (IOException e) {
            log.warn("服务器推送消息异常。");
        }
    }

    /**
     * 添加连接用户
     */
    private WsBindBox addBoxRoomUser(Integer userId, Integer boxId, Session session) {
        addOnlineCount(1);
        this.userId = userId;
        this.boxId = boxId;
        this.key = userId + "_" + boxId;
        this.session = session;
        WsBindBox.allRoomUserMap.put(key, this);
        return this;
    }

    /**
     * 移除连接用户
     */
    private WsBindBox removeRoomUser(){
        addOnlineCount(-1);
        WsBindBox.allRoomUserMap.remove(this.key);
        return this;
    }
}
