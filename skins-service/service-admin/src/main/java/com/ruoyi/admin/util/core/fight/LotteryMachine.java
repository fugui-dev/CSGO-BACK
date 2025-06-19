package com.ruoyi.admin.util.core.fight;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.mapper.TtBoxOrnamentsMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.other.TtBoxOrnaments;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ruoyi.admin.config.RedisConstants.VIP_ANCHOR_EXPERIENCE_KEY;

// 抽奖机
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LotteryMachine {

    /**
     * 奖池key： baseKey:boxId:playerType
     */
    private static final String BASE_POOL_KEY = "prize_pool:";

    // 抽奖锁
    private static final String LOTTERY_LOCK = "lotteryLock_";

    // 从mysql加载抽奖空间锁
    private static final String PRIZE_POOL_LOAD_LOCK = "prize_pool_load_lock";

    /**
     * 奖池信息预热key
     */
    private static final String All_PRIZE_POOL_KEY = "all_prize_pool:";

    private RedisLock redisLock;

    private RedisCache redisCache;

    private TtBoxOrnamentsMapper boxOrnamentsMapper;

    // 奖池(实时的) 空的时候加锁同步更新奖池
    // 奖池key:PrizePool
    private Map<String, PrizePool> prizePools;

    //  预热抽奖机，初始化所有奖池
    // TODO: 2024/3/22 缓存预热的数据要在数据更新时自动更新
    public void preheat() {

        try {
            LambdaQueryWrapper<TtBoxOrnaments> boxOrnamentsQuery = new LambdaQueryWrapper<>();
            boxOrnamentsQuery.orderByDesc(TtBoxOrnaments::getBoxId);
            List<TtBoxOrnaments> all = boxOrnamentsMapper.selectList(boxOrnamentsQuery);

            Integer boxId = -1;
            PrizePool anchorPrizePool = null;
            PrizePool realPrizePool = null;
            for (TtBoxOrnaments item : all) {

                if (!item.getBoxId().equals(boxId)) {

                    // 新箱子
                    boxId = item.getBoxId();

                    // 把上个箱子的数据保存
                    if (ObjectUtil.isNotEmpty(anchorPrizePool) && ObjectUtil.isNotEmpty(realPrizePool)) {
                        // 奖池写入内存
                        String anchorKey = anchorPrizePool.getKey();
                        String realKey = realPrizePool.getKey();
                        this.prizePools.put(anchorKey, anchorPrizePool);
                        this.prizePools.put(realKey, realPrizePool);
                        // 奖池写入redis（先删再存）
                        redisCache.deleteObject(anchorKey);
                        redisCache.deleteObject(realKey);

                        redisCache.setCacheMap(anchorKey, anchorPrizePool.getBoxSpace(), 600, TimeUnit.SECONDS);
                        redisCache.setCacheMap(realKey, realPrizePool.getBoxSpace(), 600, TimeUnit.SECONDS);
                    }

                    // 01
                    HashMap<String, Integer> anchorBoxSpace = new HashMap<>();
                    anchorBoxSpace.put(String.valueOf(item.getOrnamentId()), item.getAnchorOdds());

                    anchorPrizePool = PrizePool.builder()
                            .key(BASE_POOL_KEY + item.getBoxId() + ":" + "01")
                            .boxId(String.valueOf(item.getBoxId()))
                            .playerType("01")
                            .goodsNumber(item.getAnchorOdds())
                            .boxSpace(anchorBoxSpace)
                            .build();

                    // 02
                    HashMap<String, Integer> realBoxSpace = new HashMap<>();
                    realBoxSpace.put(String.valueOf(item.getOrnamentId()), item.getRealOdds());

                    realPrizePool = PrizePool.builder()
                            .key(BASE_POOL_KEY + item.getBoxId() + ":" + "02")
                            .boxId(String.valueOf(item.getBoxId()))
                            .playerType("02")
                            .goodsNumber(item.getRealOdds())
                            .boxSpace(realBoxSpace)
                            .build();
                } else {
                    // 累加
                    anchorPrizePool.setGoodsNumber(anchorPrizePool.getGoodsNumber() + item.getAnchorOdds());
                    Integer i1 = anchorPrizePool.getBoxSpace().get(String.valueOf(item.getOrnamentId()));
                    i1 = ObjectUtil.isEmpty(i1) ? 0 : i1;
                    anchorPrizePool.getBoxSpace().put(String.valueOf(item.getOrnamentId()), i1 + item.getAnchorOdds());

                    realPrizePool.setGoodsNumber(realPrizePool.getGoodsNumber() + item.getRealOdds());
                    Integer i2 = realPrizePool.getBoxSpace().get(String.valueOf(item.getOrnamentId()));
                    i2 = ObjectUtil.isEmpty(i2) ? 0 : i2;
                    realPrizePool.getBoxSpace().put(String.valueOf(item.getOrnamentId()), i2 + item.getRealOdds());
                }

            }

            // 保存最后一个箱子的奖池
            // 奖池写入内存
            if (ObjectUtil.isEmpty(anchorPrizePool) || ObjectUtil.isEmpty(realPrizePool)) {
                log.info("没有宝箱绑定饰品信息，奖池预热结束。");
                return;
            }
            ;
            String anchorKey = anchorPrizePool.getKey();
            String realKey = realPrizePool.getKey();
            this.prizePools.put(anchorKey, anchorPrizePool);
            this.prizePools.put(realKey, realPrizePool);
            // 奖池写入redis（先删再存）
            redisCache.deleteObject(anchorKey);
            redisCache.deleteObject(realKey);

            redisCache.setCacheMap(anchorKey, anchorPrizePool.getBoxSpace(), 600, TimeUnit.SECONDS);
            redisCache.setCacheMap(realKey, realPrizePool.getBoxSpace(), 600, TimeUnit.SECONDS);

            log.info("抽奖机预热成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("抽奖机预热失败!");
        }
    }

    // 单次加载一种宝箱的奖池
    public void singlePreheat(Integer bId) {

        try {
            LambdaQueryWrapper<TtBoxOrnaments> boxOrnamentsQuery = new LambdaQueryWrapper<>();
            boxOrnamentsQuery.eq(TtBoxOrnaments::getBoxId, bId);
            List<TtBoxOrnaments> all = boxOrnamentsMapper.selectList(boxOrnamentsQuery);

            Integer boxId = -1;
            PrizePool anchorPrizePool = null;
            PrizePool realPrizePool = null;
            for (TtBoxOrnaments item : all) {

                if (!item.getBoxId().equals(boxId)) {

                    // 新箱子
                    boxId = item.getBoxId();

                    // 把上个箱子的数据保存
                    if (ObjectUtil.isNotEmpty(anchorPrizePool) && ObjectUtil.isNotEmpty(realPrizePool)) {
                        // 奖池写入内存
                        String anchorKey = anchorPrizePool.getKey();
                        String realKey = realPrizePool.getKey();
                        this.prizePools.put(anchorKey, anchorPrizePool);
                        this.prizePools.put(realKey, realPrizePool);
                        // 奖池写入redis（先删再存）
                        redisCache.deleteObject(anchorKey);
                        redisCache.deleteObject(realKey);

                        redisCache.setCacheMap(anchorKey, anchorPrizePool.getBoxSpace(), 600, TimeUnit.SECONDS);
                        redisCache.setCacheMap(realKey, realPrizePool.getBoxSpace(), 600, TimeUnit.SECONDS);
                    }

                    // 01
                    HashMap<String, Integer> anchorBoxSpace = new HashMap<>();
                    anchorBoxSpace.put(String.valueOf(item.getOrnamentId()), item.getAnchorOdds());

                    anchorPrizePool = PrizePool.builder()
                            .key(BASE_POOL_KEY + item.getBoxId() + ":" + "01")
                            .boxId(String.valueOf(item.getBoxId()))
                            .playerType("01")
                            .goodsNumber(item.getAnchorOdds())
                            .boxSpace(anchorBoxSpace)
                            .build();

                    // 02
                    HashMap<String, Integer> realBoxSpace = new HashMap<>();
                    realBoxSpace.put(String.valueOf(item.getOrnamentId()), item.getRealOdds());

                    realPrizePool = PrizePool.builder()
                            .key(BASE_POOL_KEY + item.getBoxId() + ":" + "02")
                            .boxId(String.valueOf(item.getBoxId()))
                            .playerType("02")
                            .goodsNumber(item.getRealOdds())
                            .boxSpace(realBoxSpace)
                            .build();
                } else {
                    // 累加
                    anchorPrizePool.setGoodsNumber(anchorPrizePool.getGoodsNumber() + item.getAnchorOdds());
                    Integer i1 = anchorPrizePool.getBoxSpace().get(String.valueOf(item.getOrnamentId()));
                    i1 = ObjectUtil.isEmpty(i1) ? 0 : i1;
                    anchorPrizePool.getBoxSpace().put(String.valueOf(item.getOrnamentId()), i1 + item.getAnchorOdds());

                    realPrizePool.setGoodsNumber(realPrizePool.getGoodsNumber() + item.getRealOdds());
                    Integer i2 = realPrizePool.getBoxSpace().get(String.valueOf(item.getOrnamentId()));
                    i2 = ObjectUtil.isEmpty(i2) ? 0 : i2;
                    realPrizePool.getBoxSpace().put(String.valueOf(item.getOrnamentId()), i2 + item.getRealOdds());
                }

            }

            // 保存最后一个箱子的奖池
            // 奖池写入内存
            if (ObjectUtil.isEmpty(anchorPrizePool) || ObjectUtil.isEmpty(realPrizePool)) {
                log.info("没有宝箱绑定饰品信息，奖池预热结束。");
                return;
            }
            ;
            String anchorKey = anchorPrizePool.getKey();
            String realKey = realPrizePool.getKey();
            this.prizePools.put(anchorKey, anchorPrizePool);
            this.prizePools.put(realKey, realPrizePool);
            // 奖池写入redis（先删再存）
            redisCache.deleteObject(anchorKey);
            redisCache.deleteObject(realKey);

            redisCache.setCacheMap(anchorKey, anchorPrizePool.getBoxSpace(), 600, TimeUnit.SECONDS);
            redisCache.setCacheMap(realKey, realPrizePool.getBoxSpace(), 600, TimeUnit.SECONDS);

            log.info("箱子{}奖池已重载。", bId);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("箱子{}奖池重载失败。", bId);
        }
    }


    // 单次抽奖(要加锁)(为了兼容没有机器人写法，这里套了一层方法默认开箱不为机器人)
    public String singleLottery(TtUser player, TtBox box) {
        return singleLottery(player, box, false);
    }

    // 单次抽奖(要加锁)
    public String singleLottery(TtUser player, TtBox box, Boolean rebootFlag) {

        // 1 构建抽奖key
        String prizePoolKey = BASE_POOL_KEY + box.getBoxId() + ":" + player.getUserType();

        String lotteryLock = LOTTERY_LOCK + box.getBoxId() + "_" + player.getUserType();

        //如果存在VIP体验主播爆率
        Integer experienceNum = redisCache.getCacheObject(VIP_ANCHOR_EXPERIENCE_KEY + player.getUserId()); //剩余体验次数
        if (experienceNum != null && experienceNum > 0 && "02".equals(player.getUserType())){
            prizePoolKey = BASE_POOL_KEY + box.getBoxId() + ":" + "01";
            lotteryLock = LOTTERY_LOCK + box.getBoxId() + "_" + "01";
            redisCache.setCacheObject(VIP_ANCHOR_EXPERIENCE_KEY + player.getUserId(), experienceNum - 1); //缓存次数减一
            log.info("用户【{}】体验主播爆率，本次剩余体验次数：【{}】", player.getUserId(), experienceNum);
        }

        Boolean lock = false;
        int doTry = 0;
        while (doTry < 3) {
            lock = redisLock.tryLock(lotteryLock, 3L, 10L, TimeUnit.SECONDS);
            if (!lock) {

                doTry++;
                continue;

                // try {
                //     Thread.sleep(200);
                //     doTry++;
                //     continue;
                // } catch (InterruptedException e) {
                //     log.warn("抽奖tryLock sleep warn");
                // }
            }
            break;
        }

        if (!lock) return null;

        try {

            // 2 选中奖池
            PrizePool prizePool = this.prizePools.get(prizePoolKey);

            // 如果为空，则初始化
            if (ObjectUtil.isNull(prizePool)) {
                prizePool = PrizePool.builder()
                        .key(BASE_POOL_KEY + box.getBoxId() + ":" + player.getUserType())
                        .boxId(String.valueOf(box.getBoxId()))
                        .playerType(player.getUserType())
                        .boxSpace(new HashMap<String, Integer>())
                        .goodsNumber(0)
                        .build();
            }

            // 3 检查奖池
            PrizePool check = checkPool(prizePool);
            if (ObjectUtil.isEmpty(check)) return null;

            // 4 抽奖
            Boolean isAnchorShow = false; //是否主播秀 FIXME
            isAnchorShow = player.getIsAnchorShow();
            return doLottery(check, rebootFlag, isAnchorShow);

        } catch (Exception e) {
            e.printStackTrace();
            log.warn("用户{}抽奖异常", player.getUserId());
            return null;
        } finally {
            redisLock.unlock(lotteryLock);
        }

    }

    // 抽奖算法

    /**
     *
     * @param prizePool 奖池
     * @param rebootFlag 是否机器人
     * @param anchorShowFlag 是否主播秀（不出蓝）
     * @return
     */
    private String doLottery(PrizePool prizePool, Boolean rebootFlag, Boolean anchorShowFlag) {

        //如果开启了主播不出蓝，去除奖池中蓝色饰品(生成新奖池）
        if (anchorShowFlag){
            String boxId = prizePool.getBoxId();
            List<TtBoxOrnaments> boxOrnamentsList = boxOrnamentsMapper.selectList(Wrappers.lambdaQuery(TtBoxOrnaments.class)
                    .eq(TtBoxOrnaments::getBoxId, prizePool.getBoxId())
                    .in(TtBoxOrnaments::getLevel, 1, 2, 3));


            Map<String, Integer> boxSpace1 = new HashMap<String, Integer>();
            Integer GoodsNumber = 0;
            for (TtBoxOrnaments ornaments : boxOrnamentsList) {
                boxSpace1.put(ornaments.getOrnamentId().toString(), ornaments.getAnchorOdds());
                GoodsNumber += ornaments.getAnchorOdds();
            }

            PrizePool pool = new PrizePool();
//            pool.setKey(prizePool.getKey() + "not:blue");
            pool.setBoxId(boxId);
            pool.setPlayerType("01");
            pool.setGoodsNumber(GoodsNumber);
            pool.setBoxSpace(boxSpace1);

            prizePool = pool;
        }

        Map<String, Integer> boxSpace = prizePool.getBoxSpace();

        log.info("奖池：{}, 总数{},抽奖空间：{}",prizePool.getKey(), prizePool.getGoodsNumber() , boxSpace);

        // 2 计算随机数，开始抽奖（5次尝试）
        String targetOrnamentId = null;
        for (int c = 0; c < 5; c++) {

            Random random = new Random();
            int r = 0;
//            for (int i = 0; i < 5; i++) {
                r = random.nextInt(prizePool.getGoodsNumber());
//            }

            log.info("抽奖随机数：{}", r);

            int count = 0;  // 宝箱的第几个饰品
//            boolean flag = false;
            for (String ornamentId : boxSpace.keySet()) {
                int kucun = boxSpace.get(ornamentId) == null ? 0 : boxSpace.get(ornamentId); //取出剩余库存数
                count = count + kucun; //累加库存
//                if (flag) break;
                if (count >=  r && kucun > 1){
                    targetOrnamentId = ornamentId;
//                    flag = true;
                    break;
                }
//                for (int i = 0; i < boxSpace.get(ornamentId); i++) {
//                    if (count == r) {
//                        targetOrnamentId = ornamentId;
//                        flag = true;
//                        break;
//                    }
//                    count++;
//                }
            }
//            for (String ornamentId : boxSpace.keySet()) {
//                if (flag) break;
//                for (int i = 0; i < boxSpace.get(ornamentId); i++) {
//                    if (count == r) {
//                        targetOrnamentId = ornamentId;
//                        flag = true;
//                        break;
//                    }
//                    count++;
//                }
//            }

            if (!StringUtils.isBlank(targetOrnamentId)) {
                break;
            }
        }

        // 多次尝试依然没抽到东西，按序给一个
        if (ObjectUtil.isEmpty(targetOrnamentId)) {
//            log.info("多次抽奖无结果，按顺序给一个...");
//            for (String ornamentId : boxSpace.keySet()) {
//                if (boxSpace.get(ornamentId) > 0) {
//                    targetOrnamentId = ornamentId;
//                    break;
//                }
//            }
            throw new ServiceException("访问人数太多，请重试~");
        }

        // 3 库存减一,更新奖池(非机器人并且不是主播秀才扣减库存)
        if (!rebootFlag && !anchorShowFlag){
            PrizePool newPool = prizePool.sub(targetOrnamentId);
            this.prizePools.put(newPool.getKey(), newPool);
            log.info("抽奖奖品：{}", targetOrnamentId);
            log.info("最终抽奖空间：{}", newPool.getBoxSpace());
        }

        return targetOrnamentId;

    }

    // 检查奖池，如果没有自动补充
    private PrizePool checkPool(PrizePool prizePool) {

        Boolean isEmpty = true;
        Map<String, Integer> boxSpace = prizePool.getBoxSpace();
        if (ObjectUtil.isNotNull(boxSpace)) {
            Set<String> keySet = boxSpace.keySet();
            for (String ornamentId : keySet) {
                if (prizePool.getBoxSpace().get(ornamentId) > 0) {
                    isEmpty = false;
                    break;
                }
            }
        }

        // 有库存
        if (prizePool.getGoodsNumber() > 0 && !isEmpty) {
            return prizePool;
        }

        // 没库存，从redis补充
        Map<String, Integer> prizeSpace = redisCache.getCacheMap(prizePool.getKey());
        if (ObjectUtil.isNotEmpty(prizeSpace)) {
            Integer number = 0;
            for (Integer p : prizeSpace.values()) {
                number = number + p;
            }
            prizePool.setGoodsNumber(number);
            prizePool.setBoxSpace(prizeSpace);
            return prizePool;
        }

        // redis也没有，从mysql加载
        Boolean tryLoad = false;
        for (int i = 0; i < 2; i++) {

            tryLoad = redisLock.tryLock(PRIZE_POOL_LOAD_LOCK, 2L, 7L, TimeUnit.SECONDS);

            if (!tryLoad) {

                prizeSpace = redisCache.getCacheMap(prizePool.getKey());

                if (ObjectUtil.isNotEmpty(prizeSpace)) {
                    Integer number = 0;
                    for (Integer p : prizeSpace.values()) {
                        number = number + p;
                    }
                    prizePool.setGoodsNumber(number);
                    prizePool.setBoxSpace(prizeSpace);
                    return prizePool;
                } else {
                    // try {
                    //     Thread.sleep(200);
                    // } catch (InterruptedException e) {
                    //     log.warn("prize pool try load warn。");
                    // }
                }
            } else {
                break;
            }
        }

        if (!tryLoad) return null;

        //从数据库加载
        try {
            List<TtBoxOrnaments> list = new LambdaQueryChainWrapper<>(boxOrnamentsMapper)
                    .eq(TtBoxOrnaments::getBoxId, prizePool.getBoxId())
                    .list();

            HashMap<String, Integer> space = new HashMap<>();
            Integer goodsNumber = 0;
            String pt = prizePool.getPlayerType();
            for (TtBoxOrnaments item : list) {
                space.put(String.valueOf(item.getOrnamentId()), pt.equals("01") ? item.getAnchorOdds() : item.getRealOdds());
                goodsNumber += pt.equals("01") ? item.getAnchorOdds() : item.getRealOdds();
            }

            prizePool.setGoodsNumber(goodsNumber);
            prizePool.setBoxSpace(space);
            redisCache.setCacheMap(prizePool.getKey(), space, 600, TimeUnit.SECONDS);
            return prizePool;
        } catch (Exception e) {
            log.warn("prize load warn");
            return null;
        } finally {
            redisLock.unlock(PRIZE_POOL_LOAD_LOCK);
        }

    }

    // 对战模式抽奖（组队抽奖）
    // private List<TtBoxRecords> lotteryTogether()

    // 删除内存奖池
    public boolean removeMemoryPrizePool(String key) {
        this.prizePools.remove(key);
        return true;
    }

    // 删除redis奖池
    public boolean removeRedisPrizePool(String key) {
        return redisCache.deleteObject(key);
    }

    // 清空box奖池
    public boolean clearBoxPrizePool(Integer boxId) {

        String[] userTypes = new String[]{"01", "02"};

        for (String userType : userTypes) {
            String key = BASE_POOL_KEY + boxId + ":" + userType;
            this.removeMemoryPrizePool(key);
            this.removeRedisPrizePool(key);
        }
        return true;
    }

}
