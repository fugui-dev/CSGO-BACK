package com.ruoyi.admin.config;

public class RedisConstants {

    // 基础抽奖机
    public static final String OPEN_BOX_ODDS = "open_box_odds:boxId_";
    public static final String REAL_ODDS_SUFFIX = ":realList";
    public static final String ANCHOR_ODDS_SUFFIX = ":anchorList";

    //用户玩法操作锁，通用(不管哪种玩法进来直接上锁，不允许并发)
    public static final String USER_PLAY_COMMON = "user_play_common:";


    // 对战模式
    public static final String JOIN_FIGHT_LOCK = "join_fight:lock_";
    public static final String JOIN_FIGHT_BEGIN_LOCK = "join_fight:lock_begin";
    public static final String JOIN_FIGHT_SEAT_READY_LOCK = "join_fight:lock_seat_ready_";
    public static final String JOIN_FIGHT_END_LOCK = "join_fight:lock_end";


    // roll房间
    public static final String JOIN_ROLL_LOCK = "join_roll:lock_";


    public static final String RECEIVE_RED_PACKET_LOCK = "receive_red_packet:lock_";
    public static final String CARD_PAY_LOCK = "CARD_PAY:lock_";


    // 幸运升级
    public static final String UPGRADE_RANGE = "upgrade_range:";        // 概率区间 '业务key:饰品id:用户类型'
    public static final String UPGRADE_RANGE_FIXED = "upgrade_range_fixed:";        // 固定概率 '业务key:饰品id:用户类型'


    /**
     * 抽奖box的奖品空间
     * open_box_goods_apace:
     * box_id:
     * odds_key:
     * valua
     */
    public static final String OPEN_BOX_GOODS_SPACE = "open_box_goods_apace:";
    public static final String OPEN_BOX_LOTTERY = "open_box_lottery:";


    //订单处理
    public static final String NOTIFY_PAY = "order_process_no:";

    //VIP升级后短暂的主播爆率
    public static final String VIP_ANCHOR_EXPERIENCE_KEY = "vip_anchor_experience_user_id:";


}
