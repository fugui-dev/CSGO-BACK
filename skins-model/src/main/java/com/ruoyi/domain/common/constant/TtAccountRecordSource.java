package com.ruoyi.domain.common.constant;

// 账户流水来源类型(这个类型金币和弹药通用)
// 注意和TtAccountRecordSource区分
public enum TtAccountRecordSource {

    // 充值
    RECHARGE(1,"充值收入"),

    // 玩转盲盒
    GAME_TYPE_01(2,"玩转盲盒消费"),

    // 玩转盲盒游戏奖励
    game_type_01_award(3,"玩转盲盒游戏奖励"),

    // 急速对战
    GAME_TYPE_02(4,"急速对战消费"),

    // 急速对战游戏奖励
    GAME_TYPE_02_AWARD(5,"急速对战游戏奖励"),

    // roll房
    GAME_TYPE_03(6,"roll房消费"),

    // roll房游戏奖励
    GAME_TYPE_03_AWARD(7,"roll房游戏奖励"),

    // 逐梦前行
    GAME_TYPE_04(8,"逐梦前行消费"),

    // 逐梦前行游戏奖励
    GAME_TYPE_04_AWARD(9,"逐梦前行游戏奖励"),

    // 分解饰品
    DECOMPOSE_ORNAMENT(10,"分解饰品（提货）收入"),

    // 任务
    TASK(11,"首次下载任务奖励"),

    // 商城兑换
    EXCHANGE(12,"商城兑换消费"),

    // 推广福利
    P_WELFARE(13,"推广福利收入"),

    // 后台操作
    ADMIN_UPDATA(14,"后台操作"),

    // 奖励津贴（暂时没啥用）
    BONUS(15,"奖励津贴"),

    // 弹药兑换金币
    INTEGRATING_CONVERSION(16,"弹药兑换金币"),

    // 口令红包
    RECEIVE_RED_PACKET(17,"口令红包收入"),

    // 注册奖励
    REGIST_AWARD(18,"注册奖励"),

    RANK_BLEND_ERCASH(19,"综合消费排行榜奖励"),
    RANK_AMOUNT(20,"金币消费排行榜奖励"),
    RANK_CREDITS(21,"弹药消费排行榜奖励"),
    RANK_PROD_ORN(22,"出货排行榜奖励"),

    FIRST_CHARGE(23, "首充赠送"),

    PROMOTION_LEVEL_CHARGE(24, "推广等级充值赠送"),

    VIP_LEVEL_FANXIAN(25, "VIP等级返现"),

    VIP_LEVEL_RED_PACK(26, "VIP等级红包奖励"),

    DAY_TOTAL_CHARGE(27, "单日累计充值奖励"),

    //根据推广流水反佣金直接给用户账户
    PROMOTION_COMMISSION_WELFARE(28, "推广流水奖励"),

    REAL_NAME_GIFT_MONEY1(31, "新人实名金币奖励"),
    REAL_NAME_GIFT_MONEY2(32, "新人实名弹药奖励");

    private final Integer code;

    private final String msg;

    TtAccountRecordSource(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode(){
        return this.code;
    }

    public String getMsg(){
        return this.msg;
    }

}
