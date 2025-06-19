package com.ruoyi.playingmethod.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.config.RedisConstants;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtOrnamentsLevelService;
import com.ruoyi.admin.service.TtRollUserService;
import com.ruoyi.admin.util.RandomUtils;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtboxRecordSource;
import com.ruoyi.domain.common.constant.UserType;
import com.ruoyi.domain.common.constant.roll.RollGetPrizeWay;
import com.ruoyi.domain.common.constant.roll.RollStatus;
import com.ruoyi.domain.common.constant.roll.RollType;
import com.ruoyi.domain.dto.roll.GetRollOpenPrizeParam;
import com.ruoyi.domain.dto.roll.GetRollPlayersParam;
import com.ruoyi.domain.dto.roll.GetRollPrizePool;
import com.ruoyi.domain.entity.*;
import com.ruoyi.domain.entity.roll.*;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.*;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import com.ruoyi.domain.vo.roll.RollJackpotOrnamentsVO;
import com.ruoyi.domain.vo.roll.RollUserPrizeVO;
import com.ruoyi.domain.vo.roll.RollUserVO;
import com.ruoyi.domain.vo.roll.SimpleRollOrnamentVO;
import com.ruoyi.playingmethod.controller.ApiRollController.GetRollListParam;
import com.ruoyi.playingmethod.controller.ApiRollController.JoinRollParam;
import com.ruoyi.playingmethod.mapper.ApiRollMapper;
import com.ruoyi.playingmethod.service.ApiRollService;
import com.ruoyi.domain.vo.RollDetailsDataVO;
import com.ruoyi.domain.vo.RollListDataVO;
import com.ruoyi.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ruoyi.domain.common.constant.TtboxRecordStatus.IN_PACKSACK_ON;

@Service
@Slf4j
public class ApiRollServiceImpl extends ServiceImpl<TtRollMapper, TtRoll> implements ApiRollService {

    private final ApiRollMapper apiRollMapper;
    private final TtUserMapper userMapper;
    private final TtOrnamentMapper ornamentsMapper;
    private final TtBoxRecordsService boxRecordsService;
    private final TtRollMapper rollMapper;
    private final TtRollUserService rollUserService;
    private final TtRollJackpotOrnamentsMapper rollJackpotOrnamentsMapper;
    private final TtRechargeRecordMapper rechargeRecordMapper;
    private final RedisLock redisLock;

    private final ISysConfigService configService;

    public ApiRollServiceImpl(ApiRollMapper apiRollMapper,
                              TtUserMapper userMapper,
                              TtOrnamentMapper ornamentsMapper,
                              TtBoxRecordsService boxRecordsService,
                              TtRollMapper rollMapper,
                              TtRollUserService rollUserService,
                              TtRollJackpotOrnamentsMapper rollJackpotOrnamentsMapper,
                              TtRechargeRecordMapper rechargeRecordMapper,
                              RedisLock redisLock,
                              ISysConfigService configService) {
        this.apiRollMapper = apiRollMapper;
        this.userMapper = userMapper;
        this.ornamentsMapper = ornamentsMapper;
        this.boxRecordsService = boxRecordsService;
        this.rollMapper = rollMapper;
        this.rollUserService = rollUserService;
        this.rollJackpotOrnamentsMapper = rollJackpotOrnamentsMapper;
        this.rechargeRecordMapper = rechargeRecordMapper;
        this.redisLock = redisLock;
        this.configService = configService;
    }

    @Autowired
    private TtRollUserMapper ttRollUserMapper;

    @Autowired
    private TtRollUserPrizeMapper ttRollUserPrizeMapper;

    @Autowired
    private TtRollJackpotMapper ttRollJackpotMapper;

    @Autowired
    private TtRollJackpotOrnamentsMapper ttRollJackpotOrnamentsMapper;

    @Autowired
    private TtRollMapper ttRollMapper;

    @Autowired
    private TtOrnamentMapper ttOrnamentMapper;

    @Autowired
    private TtBoxRecordsService ttBoxRecordsService;

    @Autowired
    private TtOrnamentsLevelMapper ttOrnamentsLevelMapper;

    @Autowired
    private TtOrnamentsLevelService ttOrnamentsLevelService;

    @Autowired
    private TtBoxRecordsMapper ttBoxRecordsMapper;

    public R<TtRoll> joinRollCheck(TtRoll ttRoll, TtRollUser rollUser, TtUser player, JoinRollParam param) {

        if (StringUtils.isNull(ttRoll)) return R.fail("当前Roll房已结束");
        if (StringUtils.isNotNull(rollUser)) return R.fail("您当前已在该房间内，请勿重复加入房间！");

        String password = ttRoll.getRollPassword();
        if (StringUtils.isNotEmpty(password)) {
            if (StringUtils.isEmpty(param.getRollPassword())) return R.fail("请输入密码！");
            if (!password.equals(param.getRollPassword())) return R.fail("密码错误！");
        }

        // 检查加入条件：充值下限
        BigDecimal minRecharge = ttRoll.getMinRecharge();
        if (minRecharge.compareTo(BigDecimal.ZERO) > 0 && !isReboots(player)) {
            BigDecimal rechargeTotalAmount = getRechargeTotalAmount(player.getUserId(), ttRoll.getRechargeStartTime());
            if (minRecharge.compareTo(rechargeTotalAmount) > 0) return R.fail("未满足该房间的充值条件！");
        }

        return R.ok();
    }

    private Boolean isReboots(TtUser player) {
        String rebootIds = configService.selectConfigByKey("rollRebootIds");
        String[] split = rebootIds.replace("，", ",").split(",");

        if (split.length < 1){
            return false;
        }

        Set<String> collect = Arrays.stream(split).collect(Collectors.toSet());
        return collect.contains(player.getUserId().toString());
    }

    @Override
    public R joinRoll(JoinRollParam param, TtUser player) {

        // roll房
        TtRoll ttRoll = new LambdaQueryChainWrapper<>(rollMapper)
                .eq(TtRoll::getId, param.getRollId())
                .eq(TtRoll::getRollStatus, "0")
                .gt(TtRoll::getEndTime, DateUtils.getNowDate())
                .one();
        // roll房成员
        TtRollUser rollUser = new LambdaQueryChainWrapper<>(rollUserService.getBaseMapper())
                .eq(TtRollUser::getUserId, player.getUserId())
                .eq(TtRollUser::getRollId, param.getRollId())
                .one();

        // 检查
        R<TtRoll> check = joinRollCheck(ttRoll, rollUser, player, param);
        if (!check.getCode().equals(200)) return check;

        // 如果是主播房
        if (ttRoll.getRollType().equals(RollType.ANCHOR.getCode())) {
            TtUser anchor = new LambdaQueryChainWrapper<>(userMapper)
                    .eq(TtUser::getUserId, ttRoll.getUserId())
                    .eq(TtUser::getUserType, UserType.ANCHOR.getCode())
                    .eq(TtUser::getDelFlag, "0")
                    .one();
            // 检查是否粉丝
            Integer parentId = player.getParentId();
            if (StringUtils.isNull(parentId) || !anchor.getUserId().equals(parentId)) {
                return R.fail("您不是该主播的粉丝，请先加入主播粉丝团才可加入房间！");
            }
        }

        // 加入房间
        Boolean lock = false;
        for (int i = 0; i < 2; i++) {

            lock = redisLock.tryLock(RedisConstants.JOIN_ROLL_LOCK + param.getRollId(), 2L, 7L, TimeUnit.SECONDS);

            if (!lock) {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (Exception e) {
                    log.warn("try lock sleep warn");
                } finally {
                    continue;
                }
            } else {
                break;
            }

        }

        if (lock) {
            try {

                List<TtRollUser> rollUserList = new LambdaQueryChainWrapper<>(rollUserService.getBaseMapper())
                        .eq(TtRollUser::getRollId, param.getRollId())
                        .list();

                if (ttRoll.getPeopleNum() <= rollUserList.size()) {
                    return R.fail("roll房人满。");
                }

                TtRollUser ttRollUser = TtRollUser.builder()
                        .rollId(param.getRollId())
                        .userId(player.getUserId())
                        .userName(player.getUserName())
                        .nickName(player.getNickName())
                        .avatar(player.getAvatar())
                        .joinTime(DateUtils.getNowDate())
                        .build();

                rollUserService.getBaseMapper().insert(ttRollUser);

                return R.ok();

            } catch (Exception e) {
                e.printStackTrace();
                return R.fail("加入失败，请稍后重试。");
            } finally {
                redisLock.unlock(RedisConstants.JOIN_ROLL_LOCK + param.getRollId());
            }
        }

        redisLock.unlock(RedisConstants.JOIN_ROLL_LOCK + param.getRollId());
        return R.fail("服务器繁忙。");

    }

    // roll房开奖
    @Override
    public R endROLL(Integer rollId) {

        System.out.println("server roll房开奖" + rollId);

        // 1 检查
        TtRoll ttRoll = new LambdaQueryChainWrapper<>(rollMapper)
                .eq(TtRoll::getId, rollId)
                .eq(TtRoll::getDelFlag, "0")
                .one();
        if (ObjectUtil.isEmpty(ttRoll)) return R.fail("不存在的roll房。");
        if(ttRoll.getRollStatus().equals("1")) return R.ok("已开奖，拒绝重复开奖");

        List<TtRollUser> list = new LambdaQueryChainWrapper<>(rollUserService.getBaseMapper())
                .eq(TtRollUser::getRollId, rollId)
                .list();
        if (ObjectUtil.isEmpty(list) || list.isEmpty()) {
            ttRoll.setRollStatus(RollStatus.END.getCode());
            rollMapper.updateById(ttRoll);
            return R.ok("roll房无人参与。");
        }

        // 2 分配系统指定，并返回剩余奖品
        List<TtRollUser> rollUsers = new LambdaQueryChainWrapper<>(ttRollUserMapper)
                .eq(TtRollUser::getRollId, ttRoll.getId())
                .eq(TtRollUser::getGetPrizeWay, RollGetPrizeWay.SYS.getCode())
                .list();

        log.info("所有参与人员" + rollUsers);

        Map<Long, TtRollJackpotOrnaments> surplusPrize = rollSurplusOrnaments(rollUsers, ttRoll);

        // 3 过滤出未分配奖品的用户
        LambdaQueryWrapper<TtRollUser> rollUserQuery = new LambdaQueryWrapper<>();
        rollUserQuery
                .eq(TtRollUser::getRollId, rollId)
                .eq(TtRollUser::getStatus, "0");
        if (!rollUsers.isEmpty()) {
            List<Integer> rollUserIds = rollUsers.stream()
                    .map(TtRollUser::getId)
                    .collect(Collectors.toList());
            rollUserQuery.notIn(TtRollUser::getId, rollUserIds);
        }
        List<TtRollUser> rollUserList = rollUserService.list(rollUserQuery);

        // 4 打乱用户顺序
        Collections.shuffle(rollUserList);
        log.info("过滤系统指定以后" + rollUserList);

        // 5 分配奖品、
        List<TtBoxRecords> res = new ArrayList<>();


        // 一人拿一件
        boolean iGetIt = false;
        for (TtRollUser rollUser : rollUserList) {

            // if (iGetIt) continue;

            Set<Map.Entry<Long, TtRollJackpotOrnaments>> entries = surplusPrize.entrySet();

            for (Map.Entry<Long, TtRollJackpotOrnaments> entry : entries) {

                Integer numb = entry.getValue().getOrnamentsNum();
                if (numb <= 0) continue;

                // 等级信息
                TtOrnamentsLevel level = ttOrnamentsLevelService.getById(entry.getValue().getOrnamentLevelId());

                TtBoxRecords boxRecords = TtBoxRecords.builder()
                        .rollId(rollId)
                        .createTime(new Timestamp(System.currentTimeMillis()))
                        .holderUserId(rollUser.getUserId())
                        .ornamentId(entry.getKey())
                        .ornamentsPrice(entry.getValue().getPrice())
                        .imageUrl(entry.getValue().getImgUrl())
                        .ornamentName(entry.getValue().getOrnamentName())
                        .ornamentLevelImg(level.getLevelImg())
                        .source(TtboxRecordSource.ROLL.getCode())
                        .userId(rollUser.getUserId())
                        .updateTime(new Timestamp(System.currentTimeMillis()))
                        .status(IN_PACKSACK_ON.getCode())
                        .build();
                res.add(boxRecords);

                log.info("{}获得{}", rollUser.getNickName(), entry.getValue().getOrnamentName());

                // 扣减数量
                TtRollJackpotOrnaments value = entry.getValue();
                value.setOrnamentsNum(numb - 1);
                surplusPrize.put(entry.getKey(), value);

                break;

            }

        }

        log.info("保存开奖结果：" + res);

        boxRecordsService.saveBatch(res);

        // 6 更新roll房状态
        new LambdaUpdateChainWrapper<>(rollMapper)
                .eq(TtRoll::getId, ttRoll.getId())
                .set(TtRoll::getRollStatus, RollStatus.END.getCode())
                .set(TtRoll::getUpdateTime, new Date())
                .update();

        System.out.println("server roll房开奖" + ttRoll.getId() + ttRoll.getRollName() + "成功");

        return R.ok();

    }

    //{ornamentId:TtRollJackpotOrnaments} TtRollJackpotOrnaments中有数量
    public Map<Long, TtRollJackpotOrnaments> rollSurplusOrnaments(List<TtRollUser> rollUsers, TtRoll ttRoll) {

        // 奖池所有物品
        List<TtRollJackpotOrnaments> jackpotOrnaments = new LambdaQueryChainWrapper<>(rollJackpotOrnamentsMapper)
                .eq(TtRollJackpotOrnaments::getJackpotId, ttRoll.getJackpotId())
                .list();

        HashMap<Long, TtRollJackpotOrnaments> AllPrizePool = new HashMap<>();
        for (TtRollJackpotOrnaments i : jackpotOrnaments) {
            AllPrizePool.put(i.getOrnamentsId(), i);
        }

        // 没有系统指定，直接返回奖池所有物品
        if (rollUsers.isEmpty()) return AllPrizePool;

        List<Integer> rollUserIds = rollUsers.stream()
                .map(TtRollUser::getId)
                .collect(Collectors.toList());
        // 系统指定信息
        List<RollUserPrizeVO> RollUserPrizeVOs = ttRollUserPrizeMapper.byRollUserIds(rollUserIds);
        // 没有系统指定，直接返回奖池所有物品
        if (RollUserPrizeVOs.isEmpty()) return AllPrizePool;

        // 分配系统指定
        List<TtBoxRecords> res = new ArrayList<>();
        for (RollUserPrizeVO userPrize : RollUserPrizeVOs) {

            // 分配奖品
            for (int i = 0; i < userPrize.getNumber(); i++) {

                TtBoxRecords boxRecords = TtBoxRecords.builder()
                        .rollId(ttRoll.getId())
                        .createTime(new Timestamp(System.currentTimeMillis()))
                        .holderUserId(userPrize.getUserId())
                        .ornamentsPrice(userPrize.getPrice())
                        .imageUrl(userPrize.getImgUrl())
                        .ornamentId(userPrize.getOrnamentId())
                        .source(TtboxRecordSource.ROLL.getCode())
                        .userId(userPrize.getUserId())
                        .updateTime(new Timestamp(System.currentTimeMillis()))
                        .status(IN_PACKSACK_ON.getCode())
                        .build();
                res.add(boxRecords);

            }

            // 在所有奖品中减去当前用户获得的奖品
            for (Long ornamentId : AllPrizePool.keySet()) {
                if (ornamentId.equals(userPrize.getOrnamentId())) {
                    // 减去
                    TtRollJackpotOrnaments ornaments = AllPrizePool.get(ornamentId);
                    int i = ornaments.getOrnamentsNum() - userPrize.getNumber();
                    ornaments.setOrnamentsNum(i >= 0 ? i : 0);
                    AllPrizePool.put(ornamentId, ornaments);
                    break;
                }
            }

        }

        // 保存已分配
        boxRecordsService.saveBatch(res, 1);

        return AllPrizePool;
    }

    @Override
    public List<RollListDataVO> getRollList(GetRollListParam param) {

        param.setLimit((param.getPage() - 1) * param.getSize());

        if (StringUtils.isBlank(param.getRollName())) {
            param.setRollName(null);
        }

        List<RollListDataVO> rollList = apiRollMapper.getRollList(param);

        List<RollListDataVO> collect = rollList.stream().peek(item -> {
            // todo 要优化！！
            TtRoll roll = this.getById(item.getId());
            // 补充奖池信息
            List<SimpleRollOrnamentVO> prizeList = ttRollJackpotOrnamentsMapper.rollShow(roll.getJackpotId());
            item.setOrnamentsList(JSONUtil.toJsonStr(prizeList));

            Integer ornamentsNumberOfRoll = ttRollJackpotOrnamentsMapper.ornamentsNumberOfRoll(prizeList.get(0).getJackpotId());

            item.setOrnamentsNum(ornamentsNumberOfRoll);

            if (!StringUtils.isBlank(item.getRollPassword())) {
                item.setHasPW(true);
                item.setRollPassword(null);
            } else {
                item.setHasPW(false);
            }
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public R<RollDetailsDataVO> getRollDetails(Integer rollId) {

        TtRoll ttRoll = getById(rollId);
        if (ObjectUtil.isEmpty(ttRoll)) return R.fail("不存在的roll房。");

        RollDetailsDataVO data = apiRollMapper.getRollDetails(rollId);
        data.setHasPW(!StringUtils.isBlank(ttRoll.getRollPassword()));

        // 成员
        // List<TtRollUser> list = new LambdaQueryChainWrapper<>(ttRollUserMapper)
        //         .eq(TtRollUser::getRollId, rollId)
        //         .list();
        // List<SimpleRollUserVO> players = list.stream().map(item -> {
        //     SimpleRollUserVO playerVo = new SimpleRollUserVO();
        //     BeanUtil.copyProperties(item, playerVo);
        //     playerVo.setUserName(null);
        //     return playerVo;
        // }).collect(Collectors.toList());
        // data.setPlayerList(players);

        // 奖池
        List<SimpleRollOrnamentVO> prizeList = ttRollJackpotOrnamentsMapper.rollShow(ttRoll.getJackpotId());
        data.setJackpotOrnamentsDataList(prizeList);

        // 获奖名单
        // List<TtBoxRecordsVO> boxRecords = ttBoxRecordsMapper.rollOpenPrize(rollId);
        // data.setOpenPrizeList(boxRecords);
        return R.ok(data);
    }

    @Override
    public R<List<RollUserVO>> getRollPlayers(GetRollPlayersParam param) {

        param.setLimit((param.getPage() - 1) * param.getSize());

        List<RollUserVO> collect = ttRollUserMapper.pageByRollId(param);

        return R.ok(collect);
    }

    @Override
    public R<List<RollJackpotOrnamentsVO>> getRollPrizePool(GetRollPrizePool param) {

        param.setLimit((param.getPage() - 1) * param.getSize());

        TtRoll one = new LambdaQueryChainWrapper<>(ttRollMapper)
                .eq(TtRoll::getId, param.getRollId())
                .eq(TtRoll::getDelFlag, 0)
                .one();
        if (ObjectUtil.isNull(one)) return R.fail("不存在的roll房。");

        param.setJackpotId(one.getJackpotId());
        List<RollJackpotOrnamentsVO> list = ttRollJackpotOrnamentsMapper.listByRollId(param);

        return R.ok(list);
    }

    @Override
    public R<List<TtBoxRecordsVO>> getRollOpenPrize(GetRollOpenPrizeParam param) {

        param.setLimit((param.getPage() - 1) * param.getSize());

        // 获奖名单
        List<TtBoxRecordsVO> boxRecords = ttBoxRecordsMapper.rollOpenPrize(param);
        return R.ok(boxRecords);
    }

    private TtBoxRecords addBoxRecordsData(Integer rollId, Long ornamentsId, Integer userId, TtRollJackpotOrnaments rollJackpotOrnaments) {
        TtOrnament ttOrnament = new LambdaQueryChainWrapper<>(ornamentsMapper).eq(TtOrnament::getId, ornamentsId).one();
        TtBoxRecords boxRecords = TtBoxRecords.builder().build();
        boxRecords.setUserId(userId);
        boxRecords.setOrnamentId(ornamentsId);
        boxRecords.setOrnamentsPrice(ttOrnament.getUsePrice());
        boxRecords.setOrnamentsLevelId(rollJackpotOrnaments.getOrnamentLevelId());
        boxRecords.setStatus(IN_PACKSACK_ON.getCode());
        boxRecords.setCreateTime(DateUtils.getNowDate());
        boxRecords.setSource(TtboxRecordSource.ROLL.getCode());
        boxRecords.setRollId(rollId);
        boxRecords.setHolderUserId(userId);
        return boxRecords;
    }

    private TtRollJackpotOrnaments getRollJackpotOrnaments(Long ornamentsId, Map<Long, TtRollJackpotOrnaments> map, Integer jackpotId) {
        TtRollJackpotOrnaments ttRollJackpotOrnaments = map.get(ornamentsId);
        if (ttRollJackpotOrnaments == null) {
            ttRollJackpotOrnaments = new LambdaQueryChainWrapper<>(rollJackpotOrnamentsMapper).eq(TtRollJackpotOrnaments::getJackpotId, jackpotId)
                    .eq(TtRollJackpotOrnaments::getOrnamentsId, ornamentsId).one();
            map.put(ornamentsId, ttRollJackpotOrnaments);
        }
        return ttRollJackpotOrnaments;
    }

    private List<Long> getSurplusOrnamentsList(Integer rollId, Integer jackpotId) {

        // roll房奖池
        List<TtRollJackpotOrnaments> ornamentsList = new LambdaQueryChainWrapper<>(rollJackpotOrnamentsMapper)
                .eq(TtRollJackpotOrnaments::getJackpotId, jackpotId)
                .list();

        Map<Long, Integer> data = new HashMap<>();
        for (TtRollJackpotOrnaments ornaments : ornamentsList) {
            data.put(ornaments.getOrnamentsId(), ornaments.getOrnamentsNum());
        }

        // 打乱奖池
        List<Long> surplusOrnamentsList = RandomUtils.toList(data);

        // 系统指定的获奖者
        List<TtRollUser> allocatedRollUserList = new LambdaQueryChainWrapper<>(rollUserService.getBaseMapper())
                .eq(TtRollUser::getRollId, rollId)
                .eq(TtRollUser::getStatus, "2")
                .list();
        List<Long> allocated = allocatedRollUserList.stream().map(TtRollUser::getOrnamentsId).collect(Collectors.toList());

        for (Long i : allocated) {
            Iterator<Long> iterator = surplusOrnamentsList.iterator();
            while (iterator.hasNext()) {
                Long next = iterator.next();
                if (Objects.equals(next, i)) {
                    iterator.remove();
                    break;
                }
            }
        }
        return surplusOrnamentsList;
    }

    private BigDecimal getRechargeTotalAmount(Integer userId, Date rechargeStartTime) {
        LambdaQueryChainWrapper<TtRechargeRecord> wrapper = new LambdaQueryChainWrapper<>(rechargeRecordMapper).eq(TtRechargeRecord::getUserId, userId);
        if (StringUtils.isNotNull(rechargeStartTime)) wrapper.gt(TtRechargeRecord::getCreateTime, rechargeStartTime);
        return wrapper.list().stream().map(TtRechargeRecord::getAmountActuallyPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
