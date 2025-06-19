package com.ruoyi.playingmethod.websocket;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.admin.config.DeleteFlag;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.domain.vo.fight.FightBoxVO;
import com.ruoyi.playingmethod.service.ApiFightService;
import com.ruoyi.playingmethod.websocket.constant.SMsgKey;
import com.ruoyi.playingmethod.websocket.util.WsResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对战大厅
 */
@Slf4j
@Component
@ServerEndpoint("/ws/fight/hall/{userId}")
public class WsFightHall {

    // 用来记录当前连接数的变量
    private static int onlineCount = 0;

    public static synchronized void increaseOnlineCount() {
        onlineCount++;
    }

    public static synchronized void decreaseOnlineCount() {
        if (onlineCount > 0) {
            onlineCount--;
        }
    }
    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    private static ConcurrentHashMap<String, WsFightHall> allHallUserMap = new ConcurrentHashMap<>();
    // 与某个客户端的连接会话，需要通过它来与客户端进行数据收发
    private Session session;
    private static ApiFightService apiFightService;
    private String userId = "";
    
    @Autowired
    public void ttFightService(ApiFightService apiFightService) {
        WsFightHall.apiFightService = apiFightService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) throws IOException {
        // 获取请求参数
        Map<String, List<String>> parameters = session.getRequestParameterMap();
        int pageNum = 1;
        int pageSize = 10;
        String status = null;
        String model = null;
        BigDecimal boxPriceTotalMin = null;
        BigDecimal boxPriceTotalMax = null;
        List<String> pageNums = parameters.get("pageNum");
        if (!Objects.isNull(pageNums) && !pageNums.isEmpty()) {
            pageNum = Integer.parseInt(pageNums.get(0));
        }
        List<String> pageSizes = parameters.get("pageSize");
        if (!Objects.isNull(pageSizes) && !pageSizes.isEmpty()) {
            pageSize = Integer.parseInt(pageSizes.get(0));
            pageSize = Math.min(pageSize, 10);
        }
        List<String> statuses = parameters.get("status");
        if (!Objects.isNull(statuses) && !statuses.isEmpty()) {
            status = statuses.get(0);
        }
        List<String> models = parameters.get("model");
        if (!Objects.isNull(models) && !models.isEmpty()) {
            model = models.get(0);
        }
        List<String> boxPriceTotalMines = parameters.get("boxPriceTotalMin");
        if (!Objects.isNull(boxPriceTotalMines) && !boxPriceTotalMines.isEmpty()) {
            boxPriceTotalMin = new BigDecimal(boxPriceTotalMines.get(0));
        }
        List<String> boxPriceTotalMaxes = parameters.get("boxPriceTotalMax");
        if (!Objects.isNull(boxPriceTotalMaxes) && !boxPriceTotalMaxes.isEmpty()) {
            boxPriceTotalMax = new BigDecimal(boxPriceTotalMaxes.get(0));
        }

        if (ObjectUtil.isNotEmpty(WsFightHall.allHallUserMap.get(userId))) {
            session.getBasicRemote().sendText("用户" + userId + "已连接，请勿重复调用");
            session.close();
            return;
        }
        addHallUser(userId, session);

        log.debug("/ws/fight/hall > > onOpen");
        // sendMessage("用户：" + userId + "进入对战游戏大厅，在线人数" + WsFightHall.onlineCount);

        // 首次连接获取所有等待中的对局
        Page<TtFight> pageInfo = new Page<>(pageNum, pageSize);
        pageInfo.setOptimizeCountSql(false);

        LambdaQueryWrapper<TtFight> fightQuery = new LambdaQueryWrapper<>();
        fightQuery
                .eq(status != null, TtFight::getStatus, status)
                .eq(model != null, TtFight::getModel, model)
                .eq(TtFight::getDelFlag, DeleteFlag.NORMAL)
                .between(boxPriceTotalMin != null && boxPriceTotalMax != null,
                        TtFight::getBoxPriceTotal, boxPriceTotalMin, boxPriceTotalMax)
                .ge(boxPriceTotalMin == null && boxPriceTotalMax != null,
                        TtFight::getBoxPriceTotal, boxPriceTotalMax)
                .orderByAsc(TtFight::getStatus)
                .orderByDesc(TtFight::getCreateTime);
        List<TtFight> fightList = apiFightService.page(pageInfo, fightQuery).getRecords();
        Map<String, Object> map = new HashMap<>();
        map.put("rows", fightList);
        map.put("total", apiFightService.page(pageInfo, fightQuery).getTotal());

        sendMessage(WsResult.ok(SMsgKey.ALL_FIGHT_ROOM.name(), map, "所有对战房间"));

        log.info("用户{}进入对战游戏大厅，广播{}个房间，在线人数{}", userId, fightList.size(), WsFightHall.onlineCount);
    }

    @OnClose
    public void onClose(Session session) {
        removeHallUser();
        log.info("关闭连接，正常在线人数：" + WsFightHall.onlineCount);
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
     * 更新在线人数
     */
    private int addOnlineCount(int number) {
        synchronized (new Object()){
            WsFightHall.onlineCount += number;
            return WsFightHall.onlineCount;
        }
    }

    /**
     * 全局广播
     */
    public static void broadcast(Object message) {

        // ObjectMapper objectMapper = new ObjectMapper();
        String msg = JSON.toJSONString(message);

        Collection<WsFightHall> wss = WsFightHall.allHallUserMap.values();

        for (WsFightHall ws : wss) {
            try {
                ws.session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                log.warn("ws大厅广播给消息异常。msg:{}", msg);
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
            log.warn("服务器推送消息异常");
        }

        // ObjectMapper objectMapper = new ObjectMapper();
        // try {
        //     this.session.getBasicRemote().sendText(objectMapper.writeValueAsString(message));
        // } catch (IOException e) {
        //
        // }
    }

    /**
     * 添加连接用户
     */
    private WsFightHall addHallUser(String userId, Session session) {
        addOnlineCount(1);
        this.userId = userId;
        this.session = session;
        WsFightHall.allHallUserMap.put(userId, this);
        return this;
    }

    /**
     * 移除连接用户
     */
    private WsFightHall removeHallUser() {
        addOnlineCount(-1);
        WsFightHall.allHallUserMap.remove(userId);
        return this;
    }
}
