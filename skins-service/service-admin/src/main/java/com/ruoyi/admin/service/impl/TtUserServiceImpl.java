package com.ruoyi.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.TtOrderService;
import com.ruoyi.admin.service.TtUserAvatarService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.domain.common.constant.TtboxRecordSource;
import com.ruoyi.domain.dto.sys.TeamUsersParam;
import com.ruoyi.domain.entity.sys.TtPromotionUpdate;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.entity.recorde.TtUserAmountRecords;
import com.ruoyi.domain.entity.recorde.TtUserCreditsRecords;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.admin.util.PhoneNumberUtil;
import com.ruoyi.admin.util.RandomUtils;
import com.ruoyi.domain.other.TtUserAvatar;
import com.ruoyi.domain.other.TtUserBody;
import com.ruoyi.domain.other.TtUserPackSackBody;
import com.ruoyi.domain.vo.TeamDetailSimpleVO;
import com.ruoyi.domain.vo.TtUserPackSackDataVO;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.domain.other.ApiUserOnline;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.vo.sys.SimpleTtUserVO;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TtUserServiceImpl extends ServiceImpl<TtUserMapper, TtUser> implements TtUserService {

    private final TtUserAmountRecordsMapper userAmountRecordsMapper;
    private final TtUserCreditsRecordsMapper userCreditsRecordsMapper;

    private final TtUserAvatarService userAvatarService;

    public TtUserServiceImpl(TtUserAmountRecordsMapper userAmountRecordsMapper,
                             TtUserCreditsRecordsMapper userCreditsRecordsMapper,
                             TtUserAvatarService userAvatarService) {
        this.userAmountRecordsMapper = userAmountRecordsMapper;
        this.userCreditsRecordsMapper = userCreditsRecordsMapper;
        this.userAvatarService = userAvatarService;
    }

    @Autowired
    private TtOrderService orderService;

    @Autowired
    private TtUserService userService;

    @Autowired
    private TtUserMapper userMapper;

    @Autowired
    private TtBoxRecordsMapper boxRecordsMapper;

    @Autowired
    private TtPromotionUpdateMapper ttPromotionUpdateMapper;

    @Autowired
    private TtUserBlendErcashMapper userBlendErcashMapper;

    @Override
    public List<TtUser> queryList(TtUserBody ttUserBody) {
        LambdaQueryWrapper<TtUser> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotNull(ttUserBody.getUserId())) wrapper.eq(TtUser::getUserId, ttUserBody.getUserId());
        if (StringUtils.isNotEmpty(ttUserBody.getUserName()))
            wrapper.like(TtUser::getUserName, ttUserBody.getUserName());
        if (StringUtils.isNotNull(ttUserBody.getNickName()))
            wrapper.like(TtUser::getNickName, ttUserBody.getNickName());
        if (StringUtils.isNotNull(ttUserBody.getUserType())) wrapper.eq(TtUser::getUserType, ttUserBody.getUserType());
        if (StringUtils.isNotEmpty(ttUserBody.getPhoneNumber()))
            wrapper.likeRight(TtUser::getPhoneNumber, ttUserBody.getPhoneNumber());
        if (StringUtils.isNotEmpty(ttUserBody.getStatus())) wrapper.eq(TtUser::getStatus, ttUserBody.getStatus());
        if (StringUtils.isNotNull(ttUserBody.getBdChannelId())) wrapper.eq(TtUser::getBdChannelId, ttUserBody.getBdChannelId());
        if (StringUtils.isNotNull(ttUserBody.getParentId())) wrapper.eq(TtUser::getParentId, ttUserBody.getParentId());

        //如果commissionRate为0表示查询佣金为0，为1表示查询佣金大于0
        if (StringUtils.isNotNull(ttUserBody.getCommissionRate())){
            if (ttUserBody.getCommissionRate().equals(0)) wrapper.eq(TtUser::getCommissionRate, 0);
            if (ttUserBody.getCommissionRate().equals(1)) wrapper.gt(TtUser::getCommissionRate, 0);
        }

        PageUtils.startPage();
        return this.list(wrapper);
    }

    public String userInfoCheck(TtUser ttUser) {

        String nickName = ttUser.getNickName();
        if (ObjectUtil.isNotEmpty(nickName)) {
            if (nickName.length() < 2 || nickName.length() > 12) {
                return "昵称长度2-12。";
            }
        }

        //如果修改了手机号，需要同时修改user_name，并且重置密码
        if (!ttUser.getPhoneNumber().equals(ttUser.getUserName())){
            Assert.isTrue(StringUtils.isNotBlank(ttUser.getPassword()), "修改手机号请重置密码！");

            ttUser.setUserName(ttUser.getPhoneNumber());

            //查询手机号占用
            List<TtUser> users = userMapper.selectList(Wrappers.lambdaQuery(TtUser.class)
                    .eq(TtUser::getUserName, ttUser.getPhoneNumber()));
            for (TtUser user : users) {
                Assert.isTrue(Objects.equals(user.getUserId(), ttUser.getUserId()), "该手机号已占用！");
            }

//            String newPassword = SecurityUtils.encryptPassword(ttUser.getPassword()); //新密码
//
//            ttUser.setPassword(newPassword);
        }

        return "";
    }

    @Override
    @Transactional
    public AjaxResult updateUserById(TtUser ttUser) {

        String check = userInfoCheck(ttUser);
        if (!StringUtils.isBlank(check)) return AjaxResult.error(check);

        String password = ttUser.getPassword();
        String avatar = ttUser.getAvatar();

        TtUser oldUser = this.getById(ttUser.getUserId());

        ttUser.setAvatar(RuoYiConfig.getDomainName() + avatar);

        if (StringUtils.isNotEmpty(password)) {
            ttUser.setPassword(SecurityUtils.encryptPassword(password));
            ttUser.setRemark("明文密码:" + password);
        } else {
            ttUser.setPassword(oldUser.getPassword());
            ttUser.setRemark(oldUser.getRemark());
        }

        if (StringUtils.isNotBlank(ttUser.getInvitationCode())){
            List<TtUser> users = userMapper.selectList(Wrappers.lambdaQuery(TtUser.class)
                    .eq(TtUser::getInvitationCode, ttUser.getInvitationCode()));
            for (TtUser user : users) {
                Assert.isTrue(Objects.equals(user.getUserId(), ttUser.getUserId()), "该邀请码已占用！");
            }

        }

        this.updateById(ttUser);

        if (oldUser.getParentId() != ttUser.getParentId()) {
            if (ObjectUtil.isNotEmpty(oldUser.getParentId()) && ObjectUtil.isNotEmpty(ttUser.getParentId()) && oldUser.getParentId().equals(ttUser.getParentId())) {
                return AjaxResult.success();
            }
            // 上下级绑定关系表
            TtPromotionUpdate build = TtPromotionUpdate.builder()
                    .employeeId(ttUser.getUserId())
                    .bossId(ttUser.getParentId())
                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()))
                    .build();
            ttPromotionUpdateMapper.insert(build);
            return AjaxResult.success();
        }
        return AjaxResult.success();

    }

    @Override
    public void generateAccount(HttpServletResponse response, List<TtUser> accountList) {
        String folderPath = RuoYiConfig.getDownloadPath() + "/";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        ServletOutputStream os = null;
        BufferedWriter bw = null;
        String uuid = IdUtils.fastSimpleUUID();
        try {
            bw = new BufferedWriter(new FileWriter(folderPath + uuid + ".txt"));
            for (int i = 0; i < accountList.size(); i++) {
                bw.write("账号[" + (i + 1) + "]: " + accountList.get(i).getUserName() + "  密码: " + accountList.get(i).getRemark());
                bw.newLine();
                bw.flush();
            }
            File uploadFile = new File(folderPath + uuid + ".txt");
            os = response.getOutputStream();
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/octet-stream");
            response.addHeader("Content-disposition", "attachment");
            os.write(FileUtil.readBytes(uploadFile));
            IoUtil.flush(os);
        } catch (IOException ignored) {
        } finally {
            if (os != null) IoUtil.close(os);
            if (bw != null) IoUtil.close(bw);
        }
    }

    @Override
    public List<TtUser> getAccountList(Integer num) {

        String avatar = "";
        List<TtUserAvatar> userAvatarList = new LambdaQueryChainWrapper<>(userAvatarService.getBaseMapper()).eq(TtUserAvatar::getIsDefault, "1").list();
        if (!userAvatarList.isEmpty()) {
            avatar = userAvatarList.get(0).getAvatar();
        }

        if (num <= 0 || num > 10) return null;
        List<TtUser> userList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String phoneNumber = getRandomPhoneNumber();
            String nickName = RandomUtils.getRandomName(new Random().nextInt(2));
            String password = RandomUtil.randomString(10);
            String invitationCode = getInvitationCode();
            TtUser ttUser = TtUser.builder().build();
            ttUser.setUserName(phoneNumber);
            ttUser.setNickName(nickName);
            ttUser.setUserType("01"); //默认生成主播
            ttUser.setPhoneNumber(phoneNumber);
            ttUser.setAvatar(avatar);
            ttUser.setPassword(SecurityUtils.encryptPassword(password));
            ttUser.setInvitationCode(invitationCode);
            ttUser.setIsRealCheck("1");
            ttUser.setAccountAmount(new BigDecimal(100000));
            ttUser.setAccountCredits(new BigDecimal(0));
            ttUser.setTotalRecharge(new BigDecimal(100000));
            ttUser.setRemark(password);
            ttUser.setCreateBy(SecurityUtils.getUsername());
            ttUser.setCreateTime(DateUtils.getNowDate());
            this.save(ttUser);
            userList.add(ttUser);
        }
        return userList;
    }

    @Override
    public boolean checkUserNameUnique(TtUser user) {
        int userId = StringUtils.isNull(user.getUserId()) ? -1 : user.getUserId();
        TtUser info = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(TtUser::getUserName, user.getUserName())
                .eq(TtUser::getDelFlag, "0")
                .one();
        if (StringUtils.isNotNull(info) && info.getUserId() != userId) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public boolean checkPhoneUnique(TtUser user) {
        int userId = StringUtils.isNull(user.getUserId()) ? -1 : user.getUserId();
        TtUser info = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(TtUser::getPhoneNumber, user.getPhoneNumber())
                .eq(TtUser::getDelFlag, "0")
                .one();
        if (StringUtils.isNotNull(info) && info.getUserId() != userId) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public boolean checkIdNumUnique(TtUser user) {
        int userId = StringUtils.isNull(user.getUserId()) ? -1 : user.getUserId();
        TtUser info = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(TtUser::getIdNum, user.getIdNum())
                .eq(TtUser::getIsRealCheck, "1")
                .eq(TtUser::getDelFlag, "0")
                .one();
        if (StringUtils.isNotNull(info) && info.getUserId() != userId) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public String getInvitationCode() {
        while (true) {
            try {
                String randomInvitationCode = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
                TtUser ttUser = new LambdaQueryChainWrapper<>(baseMapper)
                        .eq(TtUser::getInvitationCode, randomInvitationCode)
                        .eq(TtUser::getDelFlag, "0")
                        .one();
                if (StringUtils.isNull(ttUser)) {
                    return randomInvitationCode;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Override
    public TtUser selectTtUserById(Long id) {
        return baseMapper.selectTtUserById(id);
    }

    @Override
    public ApiUserOnline loginUserToUserOnline(LoginUser user) {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUserData())) {
            return null;
        }
        ApiUserOnline userOnline = new ApiUserOnline();
        BeanUtils.copyBeanProp(userOnline, user);
        userOnline.setNickName(user.getUserData().getNickName());
        userOnline.setUserName(user.getUsername());
        return userOnline;
    }

    @Override
    public ApiUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user) {
        if (StringUtils.equals(ipaddr, user.getIpaddr()) && StringUtils.equals(userName, user.getUsername())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    @Override
    public ApiUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user) {
        if (StringUtils.equals(ipaddr, user.getIpaddr())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    @Override
    public ApiUserOnline selectOnlineByUserName(String userName, LoginUser user) {
        if (StringUtils.equals(userName, user.getUsername())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    @Override
    public void insertUserAmountRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal amount, BigDecimal finalAmount) {
        insertUserAmountRecords(userId, type, source, amount, finalAmount, null, null, null);
    }

    @Override
    public void insertUserAmountRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal amount, BigDecimal finalAmount,
                                        Integer pwChildId, String pwChildName, BigDecimal childAccount) {

        TtUserAmountRecords userAmountRecords = TtUserAmountRecords.builder().build();
        userAmountRecords.setUserId(userId);
        userAmountRecords.setType(type.getCode());
        userAmountRecords.setSource(source.getCode());
        userAmountRecords.setAmount(amount);
        userAmountRecords.setFinalAmount(finalAmount);
        userAmountRecords.setCreateTime(DateUtils.getNowDate());
        userAmountRecords.setRemark(source.getMsg());
        if (source.equals(TtAccountRecordSource.P_WELFARE)) {
            userAmountRecords.setPwChildId(pwChildId);
            userAmountRecords.setPwChildName(pwChildName);
            userAmountRecords.setPwChildAccount(childAccount);
        }
        // switch (source) {
        //     case "0":
        //         userAmountRecords.setRemark("充值");
        //         break;
        //     case "1":
        //         userAmountRecords.setRemark("开箱消费");
        //         break;
        //     case "2":
        //         userAmountRecords.setRemark("创建对战消费");
        //         break;
        //     case "3":
        //         userAmountRecords.setRemark("加入对战消费");
        //         break;
        //     case "4":
        //         userAmountRecords.setRemark("对战结果收入");
        //         break;
        //     case "5":
        //         userAmountRecords.setRemark("分解收入");
        //         break;
        //     case "6":
        //         userAmountRecords.setRemark("注册红包收入");
        //         break;
        //     case "7":
        //         userAmountRecords.setRemark("口令红包收入");
        //         break;
        //     case "8":
        //         userAmountRecords.setRemark("商城弹药转换收入");
        //         break;
        //     case "9":
        //         userAmountRecords.setRemark("VIP等级返佣收入");
        //         break;
        //     case "10":
        //         userAmountRecords.setRemark("后台操作余额变动");
        //         break;
        //     case "11":
        //         userAmountRecords.setRemark("幸运升级消费");
        //         break;
        //     case "12":
        //         userAmountRecords.setRemark("商城购物消费");
        //         break;
        //     case "13":
        //         userAmountRecords.setRemark("推广福利收入");
        //         userAmountRecords.setPwChildId(pwChildId);
        //         userAmountRecords.setPwChildName(pwChildName);
        //         userAmountRecords.setPwChildAccount(account);
        //         break;
        // }
        userAmountRecordsMapper.insert(userAmountRecords);
    }

    @Override
    public void insertUserAmountRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal amount, BigDecimal finalAmount, Integer taskId) {

        TtUserAmountRecords userAmountRecords = TtUserAmountRecords.builder().build();
        userAmountRecords.setUserId(userId);
        userAmountRecords.setType(type.getCode());
        userAmountRecords.setSource(source.getCode());
        userAmountRecords.setAmount(amount);
        userAmountRecords.setFinalAmount(finalAmount);
        userAmountRecords.setCreateTime(DateUtils.getNowDate());
        userAmountRecords.setRemark(source.getMsg());
        if (source.equals(TtAccountRecordSource.TASK)) {
            userAmountRecords.setTaskId(taskId);
        }
        userAmountRecordsMapper.insert(userAmountRecords);
    }

    @Override
    public void insertUserCreditsRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal credits, BigDecimal finalCredits) {
        insertUserCreditsRecords(userId, type, source, credits, finalCredits, null, null, null);
    }

    /**
     * @param userId
     * @param type
     * @param source
     * @param credits      变动金额
     * @param finalCredits 账户最终金额
     * @param pwChildId    推广福利 上级id
     * @param pwChildName
     * @param childAccount 推广福利 上级流水
     */
    @Override
    public void insertUserCreditsRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal credits, BigDecimal finalCredits,
                                         Integer pwChildId, String pwChildName, BigDecimal childAccount) {
        TtUserCreditsRecords userCreditsRecords = TtUserCreditsRecords.builder().build();
        userCreditsRecords.setUserId(userId);
        userCreditsRecords.setType(type.getCode());
        userCreditsRecords.setSource(source.getCode());
        userCreditsRecords.setCredits(credits);
        userCreditsRecords.setFinalCredits(finalCredits);
        userCreditsRecords.setCreateTime(DateUtils.getNowDate());
        userCreditsRecords.setRemark(source.getMsg());
        if (source.equals(TtAccountRecordSource.P_WELFARE)) {
            userCreditsRecords.setPwChildId(pwChildId);
            userCreditsRecords.setPwChildName(pwChildName);
            userCreditsRecords.setPwChildAccount(childAccount);
        }
        // switch (source) {
        //     case "0":
        //         userCreditsRecords.setRemark("弹药商城兑换消费");
        //         break;
        //     case "1":
        //         userCreditsRecords.setRemark("开箱收入");
        //         break;
        //     case "2":
        //         userCreditsRecords.setRemark("创建对战收入");
        //         break;
        //     case "3":
        //         userCreditsRecords.setRemark("加入对战收入");
        //         break;
        //     case "4":
        //         userCreditsRecords.setRemark("商城弹药转换消费");
        //         break;
        //     case "5":
        //         userCreditsRecords.setRemark("幸运升级收入");
        //         break;
        //     case "10":
        //         userCreditsRecords.setRemark("后台操作弹药变动");
        //         break;
        //     case "11":
        //         // String remark = "";
        //         // if (remarks.length>0){
        //         //     for (int i = 0;i < remarks.length;i++){
        //         //         remark = remark + remarks[i];
        //         //     }
        //         // }
        //         userCreditsRecords.setRemark("推广福利弹药收入");
        //         userCreditsRecords.setPwChildId(pwChildId);
        //         userCreditsRecords.setPwChildName(pwChildName);
        //         userCreditsRecords.setPwChildAccount(account);
        //         break;
        //     case "task_1":
        //         userCreditsRecords.setRemark("{首次下载奖励}任务奖励");
        //         break;
        //     case "task_2":
        //         userCreditsRecords.setRemark("{每日流水奖励}任务奖励");
        //         break;
        // }
        userCreditsRecordsMapper.insert(userCreditsRecords);
    }

    @Override
    public void insertUserCreditsRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal credits, BigDecimal finalCredits, Integer taskId) {

        TtUserCreditsRecords userCreditsRecords = TtUserCreditsRecords.builder().build();
        userCreditsRecords.setUserId(userId);
        userCreditsRecords.setType(type.getCode());
        userCreditsRecords.setSource(source.getCode());
        userCreditsRecords.setCredits(credits);
        userCreditsRecords.setFinalCredits(finalCredits);
        userCreditsRecords.setCreateTime(DateUtils.getNowDate());
        userCreditsRecords.setRemark(source.getMsg());
        if (source.equals(TtAccountRecordSource.TASK)) {
            userCreditsRecords.setTaskId(taskId);
        }

        userCreditsRecordsMapper.insert(userCreditsRecords);

    }

    @Override
    public List<TtUserPackSackDataVO> getPackSack(TtUserPackSackBody ttUserPackSackBody) {
        return baseMapper.getPackSack(ttUserPackSackBody);
    }

    @Override
    public Map<String, BigDecimal> getUserProfitStatistics(Integer userId) {
        return baseMapper.getUserProfitStatistics(userId);
    }

    @Override
    public List<TtUserPackSackDataVO> propRankOfDay(Timestamp beginTime, Timestamp endTime, Integer number) {

        // String[] sources = new String[]{"0","1","2","6"};
        Integer[] sources = new Integer[]{
                TtboxRecordSource.BLIND_BOX.getCode(),
                TtboxRecordSource.FIGHT.getCode(),
                // TtboxRecordSource.ROLL.getCode(),
                TtboxRecordSource.UPGRADE.getCode()
        };

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginT = dateFormat.format(beginTime);
        String endT = dateFormat.format(endTime);

        // System.out.println("出货排行榜时间区间："+beginT+","+endT);

        List<TtUserPackSackDataVO> rank = boxRecordsMapper.propRankOfDay(beginT, endT, sources, number);

        return rank;
    }

    @Override
    public R<Page<SimpleTtUserVO>> teamUsers(TeamUsersParam param) {

        Page<TtUser> pageInfo = new Page<>(param.getPage(), param.getSize());
        pageInfo.setOptimizeCountSql(false);

        LambdaQueryWrapper<TtUser> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .like(ObjectUtil.isNotEmpty(param.getUserName()), TtUser::getNickName, param.getUserName())
                .or()
                .like(ObjectUtil.isNotEmpty(param.getUserName()), TtUser::getUserName, param.getUserName())
                .in(ObjectUtil.isNotEmpty(param.getEmployeeIds()), TtUser::getParentId, param.getBossIds())
                .in(ObjectUtil.isNotEmpty(param.getEmployeeIds()), TtUser::getUserId, param.getEmployeeIds());

        Page<TtUser> page = page(pageInfo, wrapper);

        List<SimpleTtUserVO> voList = page.getRecords().stream().map(item -> {
            SimpleTtUserVO vo = new SimpleTtUserVO();
            BeanUtil.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        Page<SimpleTtUserVO> p = new Page<>();
        BeanUtil.copyProperties(pageInfo, p, "records");
        p.setRecords(voList);
        return R.ok(p);
    }

    @Override
    public Map getPromotionDataInfo(Integer userId) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("anchorCount", userBlendErcashMapper.getAnchorCount(Long.valueOf(userId)));
        map.put("totalCharge", userBlendErcashMapper.getTotalCharge(userId));
        map.put("totalConsume", userBlendErcashMapper.getTotalConsume(userId));
        map.put("todayConsume", userBlendErcashMapper.getTodayConsume(userId));


        return map;
    }

    @Override
    public List<TeamDetailSimpleVO> teamDetailsList(Integer parentId, String beginTime, String endTime, Integer pageSize, Integer pageNum) {
        return userBlendErcashMapper.teamDetailsList(Long.valueOf(parentId), beginTime, endTime, pageSize, pageNum);
    }

    private String getRandomPhoneNumber() {
        while (true) {
            try {
                String phoneNumber = PhoneNumberUtil.createPhoneNumber(new Random().nextInt(3));
                TtUser ttUser = new LambdaQueryChainWrapper<>(baseMapper).eq(TtUser::getPhoneNumber, phoneNumber)
                        .eq(TtUser::getDelFlag, "0").one();
                if (StringUtils.isNull(ttUser)) {
                    return phoneNumber;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }
}
