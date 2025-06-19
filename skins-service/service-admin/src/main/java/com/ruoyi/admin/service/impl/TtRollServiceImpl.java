package com.ruoyi.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.config.DeleteFlag;
import com.ruoyi.admin.config.RedisConstants;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.TtRollJackpotService;
import com.ruoyi.admin.service.TtRollUserService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.redis.config.RedisLock;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.common.constant.UserType;
import com.ruoyi.domain.common.constant.roll.RollGetPrizeWay;
import com.ruoyi.domain.common.constant.roll.RollType;
import com.ruoyi.domain.dto.roll.GetRollPrizePool;
import com.ruoyi.domain.dto.roll.InviteRollUser;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.roll.*;
import com.ruoyi.admin.service.TtRollService;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.vo.SimpleUserVO;
import com.ruoyi.domain.vo.TtRollPrizeDataVO;
import com.ruoyi.common.rabbitmq.config.DelayedQueueConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.vo.roll.RollJackpotOrnamentsByPageVO;
import com.ruoyi.domain.vo.roll.RollJackpotOrnamentsVO;
import com.ruoyi.domain.vo.roll.RollUserPrizeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ruoyi.domain.common.constant.TtboxRecordSource.ROLL;

@Service
@Slf4j
public class TtRollServiceImpl extends ServiceImpl<TtRollMapper, TtRoll> implements TtRollService {

    @Autowired
    private TtRollUserMapper rollUserMapper;

    @Autowired
    private TtRollUserService rollUserService;

    @Autowired
    private TtBoxRecordsMapper boxRecordsMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // public TtRollServiceImpl(TtRollUserMapper rollUserMapper,
    //                          TtBoxRecordsMapper boxRecordsMapper,
    //                          RabbitTemplate rabbitTemplate) {
    //     this.rollUserMapper = rollUserMapper;
    //     this.boxRecordsMapper = boxRecordsMapper;
    //     this.rabbitTemplate = rabbitTemplate;
    // }

    @Autowired
    private TtRollJackpotService ttRollJackpotService;

    @Autowired
    private TtRollJackpotMapper ttRollJackpotMapper;

    @Autowired
    private TtRollJackpotOrnamentsMapper ttRollJackpotOrnamentsMapper;

    @Autowired
    private TtRollUserPrizeMapper ttRollUserPrizeMapper;

    // @Autowired
    // private TtRollUserMapper ttRollUserMapper;

    @Autowired
    private TtOrnamentMapper ttOrnamentMapper;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private TtUserMapper userMapper;

    @Override
    public List<TtRoll> queryList(TtRollBody ttRollBody) {
        LambdaQueryWrapper<TtRoll> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotNull(ttRollBody.getId())) wrapper.eq(TtRoll::getId, ttRollBody.getId());
        if (StringUtils.isNotEmpty(ttRollBody.getRollName()))
            wrapper.like(TtRoll::getRollName, ttRollBody.getRollName());
        if (StringUtils.isNotEmpty(ttRollBody.getRollStatus()))
            wrapper.eq(TtRoll::getRollStatus, ttRollBody.getRollStatus());
        wrapper.eq(TtRoll::getDelFlag, DeleteFlag.NORMAL);
        wrapper.orderByAsc(TtRoll::getRollStatus).orderByDesc(TtRoll::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public AjaxResult createRoll(TtRoll ttRoll) {

        // 创建roll房
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 5);
        Timestamp criticalTime = new Timestamp(c.getTimeInMillis());

        long endTime = ttRoll.getEndTime().getTime();
        // long nowDate = DateUtils.getNowDate().getTime();
        long difference = endTime - criticalTime.getTime();
        if (difference < 0) return AjaxResult.error("开奖时间在临界保护区【5分钟】内。");
        baseMapper.insert(ttRoll);

        System.out.println("创建roll房:" + ttRoll.getId() + ttRoll.getRollName());

        // 延时队列实现开奖
        // rabbitTemplate.convertAndSend(
        //         DelayedQueueConfig.DELAYED_EXCHANGE,
        //         DelayedQueueConfig.DELAYED_ROUTING_KEY,
        //         String.valueOf(ttRoll.getId()),
        //         (msg -> {
        //             msg.getMessageProperties().setDelay((int) difference);
        //             return msg;
        //         }));

        return AjaxResult.success();
    }

    @Override
    public AjaxResult updateRollById(TtRoll ttRoll) {

        TtRoll one = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(TtRoll::getId, ttRoll.getId())
                .eq(TtRoll::getRollStatus, 0)
                .eq(TtRoll::getDelFlag, 0)
                .one();

        Date oldEndTime = one.getEndTime();

        // 此刻
        Calendar c = Calendar.getInstance();

        long endTime = ttRoll.getEndTime().getTime();
        long difference = endTime - c.getTimeInMillis();
        if (difference < 10) return AjaxResult.error("开奖时间不能早于现在。");

        // 临界区检查
        c.add(Calendar.MINUTE, 5);
        long criticalTime = c.getTimeInMillis();
        if (criticalTime > oldEndTime.getTime()) {
            return AjaxResult.error("该roll房开奖时间已处于临界保护区【5分钟】内，请勿修改开奖时间。");
        }

        baseMapper.updateById(ttRoll);
        return AjaxResult.success();
    }

    @Override
    public List<TtRollPrizeDataVO> getRollPrizeList(Integer rollId) {

        TtRoll ttRoll = this.getById(rollId);

        // TtRollJackpot jackpot = new LambdaQueryChainWrapper<>(ttRollJackpotMapper)
        //         .eq(TtRollJackpot::getJackpotId, ttRoll.getJackpotId())
        //         .one();

        List<TtRollJackpotOrnaments> prizeList = new LambdaQueryChainWrapper<>(ttRollJackpotOrnamentsMapper)
                .eq(TtRollJackpotOrnaments::getJackpotId, ttRoll.getJackpotId())
                .list();

        // 所有系统指定
        List<RollUserPrizeVO> rollUserPrizes = ttRollUserPrizeMapper.byRollId(rollId);

        List<TtRollPrizeDataVO> res = new ArrayList<>();
        for (TtRollJackpotOrnaments orn : prizeList) {
            for (int i = 0; i < orn.getOrnamentsNum(); i++) {

                TtOrnament ornament = new LambdaQueryChainWrapper<>(ttOrnamentMapper)
                        .eq(TtOrnament::getId, orn.getOrnamentsId())
                        .one();

                TtRollPrizeDataVO vo = TtRollPrizeDataVO.builder()
                        .rollId(rollId)
                        .rollUserId(ttRoll.getUserId())
                        .rollJackpotOrnamentId(orn.getId())
                        .ornamentsId(orn.getOrnamentsId())
                        .usePrice(ObjectUtil.isNotEmpty(ornament.getUsePrice()) ? ornament.getUsePrice() : ornament.getPrice())
                        .shortName(ornament.getName())
                        .itemName(ornament.getName())
                        .imageUrl(ornament.getImageUrl())
                        .ornamentNum(1)
                        .build();

                if (rollUserPrizes.isEmpty()) {
                    res.add(vo);
                    continue;
                }

                for (RollUserPrizeVO prize : rollUserPrizes) {
                    if (prize.getOrnamentId().equals(ornament.getId())) {
                        vo.setUserId(prize.getUserId());
                        vo.setNickName(prize.getNickName());
                        vo.setRollUserPrizeId(prize.getRollUserPrizeId());

                        rollUserPrizes.remove(prize);
                        break;
                    }
                }

                res.add(vo);
            }
        }

        if (res.isEmpty()) {
            return res;
        }

        // 排序
        Collections.sort(res, new Comparator<TtRollPrizeDataVO>() {
            @Override
            public int compare(TtRollPrizeDataVO o1, TtRollPrizeDataVO o2) {
                return o1.getUsePrice().compareTo(o2.getUsePrice());
            }
        });

        return res;

        // roll房奖池关联的饰品列表
        // List<TtRollPrizeDataVO> rollJackpotOrnamentsList = baseMapper.getRollJackpotOrnamentsList(rollId);
        //
        // List<TtRollPrizeDataVO> list = new ArrayList<>();
        // for (TtRollPrizeDataVO rollPrizeData : rollJackpotOrnamentsList) {
        //     Integer count = new LambdaQueryChainWrapper<>(rollUserMapper)
        //             .eq(TtRollUser::getRollId, rollPrizeData.getRollId())
        //             .eq(TtRollUser::getJackpotOrnamentsId, rollPrizeData.getJackpotOrnamentsListId())
        //             .count();
        //     for (int i = 0; i < rollPrizeData.getOrnamentsNum() - count; i++) {
        //         list.add(rollPrizeData);
        //     }
        // }
        // List<TtRollPrizeDataVO> specifiedRollJackpotOrnamentsList = baseMapper.getSpecifiedRollJackpotOrnamentsList(rollId);
        //
        // list.addAll(specifiedRollJackpotOrnamentsList);
        //
        // List<TtRollPrizeDataVO> collect = list.stream().peek(rollPrizeData -> {
        //     rollPrizeData.setOrnamentsNum(1);
        // }).collect(Collectors.toList());
        //
        // return collect;
    }

    // roll指定获奖
    @Override
    public AjaxResult namedWinner(TtRollPrizeDataVO param) {

        // roll成员
        TtRollUser ttRollUser = rollUserMapper.selectOne(
                Wrappers.lambdaQuery(TtRollUser.class)
                        .eq(TtRollUser::getRollId, param.getRollId())
                        .eq(TtRollUser::getUserId, param.getRollUserId())
        );
        if (ObjectUtil.isEmpty(ttRollUser)) {
            return AjaxResult.error("不存在的roll房成员。");
        }

        // roll房
        TtRoll roll = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(TtRoll::getId, ttRollUser.getRollId())
                .eq(TtRoll::getRollStatus, 0)
                .eq(TtRoll::getDelFlag, 0)
                .one();

        if (ObjectUtil.isEmpty(roll)) return AjaxResult.error("roll房已结束。");

        // 检查roll是否已结束
        Date endTime = roll.getEndTime();
        // 此刻
        Calendar c = Calendar.getInstance();
        if (c.getTime().compareTo(endTime) >= 0) return AjaxResult.error("该roll房已经结束。");
        // 临界区检查（要不要都无所谓）
        c.add(Calendar.MINUTE, 5);
        long criticalTime = c.getTimeInMillis();
        if (criticalTime > endTime.getTime())
            return AjaxResult.error("该roll房开奖时间已处于临界保护区【5分钟】内，禁止修改。");


        ttRollUser.setGetPrizeWay(RollGetPrizeWay.SYS.getCode());
        ttRollUser.setUpdateTime(new Date());

        // 检查是否已指定
        List<TtRollUserPrize> userPrizes = new LambdaQueryChainWrapper<>(ttRollUserPrizeMapper)
                .eq(TtRollUserPrize::getRollUserId, ttRollUser.getId())
                .list();
        if (!userPrizes.isEmpty())
            return AjaxResult.error("已为roll房成员【" + ttRollUser.getNickName() + "】已指定奖品，请勿重复操作。");

        // 奖品信息
        TtRollJackpotOrnaments prizeInfo = new LambdaQueryChainWrapper<>(ttRollJackpotOrnamentsMapper)
                .eq(TtRollJackpotOrnaments::getId, param.getRollJackpotOrnamentId())
                .one();

        // 检查已指定的物品数量
        Integer ownOrnamentNumber = ttRollUserPrizeMapper.ownOrnamentNumber(prizeInfo.getId(), ttRollUser.getRollId());
        ownOrnamentNumber = ObjectUtil.isNotEmpty(ownOrnamentNumber) ? ownOrnamentNumber : 0;
        if (prizeInfo.getOrnamentsNum() <= ownOrnamentNumber) {
            return AjaxResult.error("该物品已经被指派完，没有多余的物品可以指定。");
        }

        // 指定奖品
        TtRollUserPrize build = TtRollUserPrize.builder()
                .rollUserId(ttRollUser.getId())
                .rollJackpotId(prizeInfo.getJackpotId())
                .rollJackpotOrnamentId(param.getRollJackpotOrnamentId())
                .ornamentId(prizeInfo.getOrnamentsId())
                .ornamentName(prizeInfo.getOrnamentName())
                .price(prizeInfo.getPrice())
                .imgUrl(prizeInfo.getImgUrl())
                .number(param.getOrnamentNum())
                .build();

        new LambdaUpdateChainWrapper<>(rollUserMapper)
                .eq(TtRollUser::getId, ttRollUser.getId())
                .set(TtRollUser::getGetPrizeWay, RollGetPrizeWay.SYS.getCode())
                .set(TtRollUser::getUpdateTime, new Date())
                .set(TtRollUser::getDesignatedBy, "系统指定prize。")
                .update();
        ttRollUserPrizeMapper.insert(build);

        return AjaxResult.success();

    }

    @Override
    public AjaxResult getRollUsers(Integer rollId) {

        List<TtRollUser> list = new LambdaQueryChainWrapper<>(rollUserMapper).eq(TtRollUser::getRollId, rollId).list();

        List<SimpleUserVO> collect = list.stream().map(item -> {
            return SimpleUserVO.builder()
                    .rollUserId(item.getId())
                    .userId(item.getUserId())
                    .nickName(item.getNickName())
                    .userName(item.getUserName())
                    .avatar(item.getAvatar())
                    .build();
        }).collect(Collectors.toList());

        return AjaxResult.success(collect);
    }

    @Override
    public R cancelNamedWinner(List<Integer> rollUserPrizeIds) {

        int i = ttRollUserPrizeMapper.deleteBatchIds(rollUserPrizeIds);
        if (i >= 0) return R.ok("成功取消" + i + "条奖品指定记录。");
        return R.fail();

    }

    @Override
    public R<RollJackpotOrnamentsByPageVO> getRollPrizePool(GetRollPrizePool param) {

        param.setLimit((param.getPage() - 1) * param.getSize());

        TtRoll one = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(TtRoll::getId, param.getRollId())
                .eq(TtRoll::getDelFlag, 0)
                .one();
        if (ObjectUtil.isNull(one)) return R.fail("不存在的roll房。");

        param.setJackpotId(one.getJackpotId());
        List<RollJackpotOrnamentsVO> list = ttRollJackpotOrnamentsMapper.listByRollId(param);

        Integer total = ttRollJackpotOrnamentsMapper.totalByCondition(param);

        RollJackpotOrnamentsByPageVO build = RollJackpotOrnamentsByPageVO.builder()
                .list(list)
                .total(total)
                .build();

        return R.ok(build);
    }

    public R<TtRoll> inviteRollUserCheck(InviteRollUser param) {

        // roll房
        TtRoll ttRoll = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(TtRoll::getId, param.getRollId())
                .eq(TtRoll::getRollStatus, "0")
                .gt(TtRoll::getEndTime, DateUtils.getNowDate())
                .one();
        if (StringUtils.isNull(ttRoll)) return R.fail("当前Roll房已结束");

        //
        List<TtRollUser> rollUserList = new LambdaQueryChainWrapper<>(rollUserMapper)
                .eq(TtRollUser::getRollId, param.getRollId())
                .list();
        if (ttRoll.getPeopleNum() <= rollUserList.size()) {
            return R.fail("roll房人满。");
        }

        if (ttRoll.getPeopleNum() < rollUserList.size() + param.getUserIds().size()) {
            return R.fail("加入人数超过roll房人数上限。");
        }

        //
        List<TtRollUser> rollUsers = new LambdaQueryChainWrapper<>(rollUserMapper)
                .in(TtRollUser::getUserId, param.getUserIds())
                .eq(TtRollUser::getRollId, param.getRollId())
                .list();
        if (!rollUsers.isEmpty()) return R.fail("已加入的成员不能重复加入。");

        return R.ok(ttRoll);
    }

    @Override
    public R inviteRollUser(InviteRollUser param) {

        // roll房
        // TtRoll ttRoll = new LambdaQueryChainWrapper<>(baseMapper)
        //         .eq(TtRoll::getId, param.getRollId())
        //         .eq(TtRoll::getRollStatus, "0")
        //         .gt(TtRoll::getEndTime, DateUtils.getNowDate())
        //         .one();
        // roll房成员
        // TtRollUser rollUser = new LambdaQueryChainWrapper<>(ttRollUserMapper)
        //         .eq(TtRollUser::getUserId, player.getUserId())
        //         .eq(TtRollUser::getRollId, param.getRollId())
        //         .one();

        // 检查
        // R<TtRoll> check = inviteRollUserCheck(param);
        // if (!check.getCode().equals(200)) return check;

        // 如果是主播房
        // if (ttRoll.getRollType().equals(RollType.ANCHOR.getCode())) {
        //     TtUser anchor = new LambdaQueryChainWrapper<>(userMapper)
        //             .eq(TtUser::getUserId, ttRoll.getUserId())
        //             .eq(TtUser::getUserType, UserType.ANCHOR.getCode())
        //             .eq(TtUser::getDelFlag, "0")
        //             .one();
        //     // 检查是否粉丝
        //     Integer parentId = player.getParentId();
        //     if (StringUtils.isNull(parentId) || !anchor.getUserId().equals(parentId)) {
        //         return R.fail("您不是该主播的粉丝，请先加入主播粉丝团才可加入房间！");
        //     }
        // }

        // 加入房间
        Boolean lock = false;
        for (int i = 0; i < 3; i++) {

            lock = redisLock.tryLock(RedisConstants.JOIN_ROLL_LOCK + param.getRollId(), 2L, 15L, TimeUnit.SECONDS);

            if (lock) break;

        }

        if (lock) {
            try {

                R<TtRoll> check = inviteRollUserCheck(param);
                if (!check.getCode().equals(200)) return check;

                TtRoll roll = check.getData();

                List<TtUser> userList = new LambdaQueryChainWrapper<>(userMapper)
                        .eq(TtUser::getDelFlag, "0")
                        .eq(TtUser::getStatus, "0")
                        .in(TtUser::getUserId, param.getUserIds())
                        .list();

                List<TtRollUser> rollUserList = new ArrayList<>();
                for (TtUser player : userList) {
                    TtRollUser ttRollUser = TtRollUser.builder()
                            .rollId(param.getRollId())
                            .userId(player.getUserId())
                            .userName(player.getUserName())
                            .nickName(player.getNickName())
                            .avatar(player.getAvatar())
                            .joinTime(DateUtils.getNowDate())
                            .build();
                    rollUserList.add(ttRollUser);
                }

                if (rollUserService.saveBatch(rollUserList, 10)) return R.ok();

                return R.fail("Sava db fail");

            } catch (Exception e) {
                e.printStackTrace();
                return R.fail("加入失败，请稍后重试。");
            } finally {
                redisLock.unlock(RedisConstants.JOIN_ROLL_LOCK + param.getRollId());
            }
        }

        // redisLock.unlock(RedisConstants.JOIN_ROLL_LOCK + param.getRollId());
        return R.ok("服务器繁忙,请稍后重试。");

    }
}
