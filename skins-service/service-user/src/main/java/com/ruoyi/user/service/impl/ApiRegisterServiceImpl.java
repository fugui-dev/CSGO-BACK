package com.ruoyi.user.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.mapper.TtPromotionUpdateMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.domain.entity.sys.TtPromotionUpdate;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtUserAvatar;
import com.ruoyi.admin.service.TtUserAvatarService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.baiduPromotion.BdPromotionProcess;
import com.ruoyi.thirdparty.common.service.ApiSmsService;
import com.ruoyi.user.service.ApiRegisterService;
import com.ruoyi.domain.other.ApiRegisterBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
public class ApiRegisterServiceImpl implements ApiRegisterService {

    private final TtUserAvatarService userAvatarService;
    private final TtUserService userService;
    private final ISysConfigService configService;
    private final ApiSmsService apiSmsService;

    @Autowired
    private BdPromotionProcess bdPromotionProcess;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public ApiRegisterServiceImpl(TtUserAvatarService userAvatarService,
                                  TtUserService userService,
                                  ISysConfigService configService,
                                  ApiSmsService apiSmsService) {
        this.userAvatarService = userAvatarService;
        this.userService = userService;
        this.configService = configService;
        this.apiSmsService = apiSmsService;
    }

    @Autowired
    private TtPromotionUpdateMapper ttPromotionUpdateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String register(ApiRegisterBody registerBody) {
        String nickName = registerBody.getNickName(), phoneNumber = registerBody.getPhoneNumber(),
                password = registerBody.getPassword(), parentInvitationCode = registerBody.getParentInvitationCode(),
                code = registerBody.getCode();
        if (StringUtils.isEmpty(nickName)) return "用户昵称不能为空";
        if (StringUtils.isEmpty(phoneNumber)) return "手机号不能为空";
        if (!Validator.isMobile(phoneNumber)) return "手机号格式错误，请检查手机号是否输入正确！";
        if (StringUtils.isEmpty(password)) return "用户密码不能为空";
        if (StringUtils.isEmpty(code)) return "验证码不能为空";
        if (!NumberUtil.isNumber(code) || code.trim().length() != 4) return "验证码错误";
        TtUser ttUser = TtUser.builder().build();
        ttUser.setUserName(phoneNumber);
        ttUser.setPhoneNumber(phoneNumber);
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            return "密码长度必须在5到20个字符之间";
        }
        if (!userService.checkPhoneUnique(ttUser)) return "注册失败，" + "手机号'" + phoneNumber + "'已被注册！";
        if (!userService.checkUserNameUnique(ttUser))
            return "注册失败，" + "用户名'" + ttUser.getUserName() + "'已存在！";
        ttUser.setNickName(nickName);
        ttUser.setUserType("02");
        List<TtUserAvatar> userAvatarList = new LambdaQueryChainWrapper<>(userAvatarService.getBaseMapper()).eq(TtUserAvatar::getIsDefault, "1").list();
        if (!userAvatarList.isEmpty()) {
            ttUser.setAvatar(userAvatarList.get(0).getAvatar());
        } else ttUser.setAvatar("");
        ttUser.setPassword(SecurityUtils.encryptPassword(password));
        String registerRedPacketStr = configService.selectConfigByKey("registerRedPacket"); //注册红包
        BigDecimal registerRedPacket = new BigDecimal(registerRedPacketStr);
        ttUser.setAccountAmount(registerRedPacket);
        ttUser.setInvitationCode(userService.getInvitationCode());
        if (StringUtils.isNotEmpty(parentInvitationCode) && parentInvitationCode.trim().length() == 6) {
            TtUser parentUser = new LambdaQueryChainWrapper<>(userService.getBaseMapper())
                    .eq(TtUser::getInvitationCode, parentInvitationCode.trim().toUpperCase())
                    .eq(TtUser::getDelFlag, "0").one();
            if (StringUtils.isNull(parentUser)) {
                return "上级邀请码填写错误！";
            } else {
                // 保存推广更新记录
                TtPromotionUpdate build = TtPromotionUpdate.builder()
                        .employeeId(ttUser.getUserId())
                        .bossId(parentUser.getUserId())
                        .createTime(new Timestamp(System.currentTimeMillis()))
                        .updateTime(new Timestamp(System.currentTimeMillis()))
                        .build();
                ttPromotionUpdateMapper.insert(build);
            }
            ttUser.setParentId(parentUser.getUserId());
        }
        ttUser.setCreateBy("网站注册");
        ttUser.setCreateTime(DateUtils.getNowDate());
        ttUser.setRemark("明文密码:" + password);
        ttUser.setTotalRecharge(new BigDecimal("0.00"));
        String validateCaptcha = apiSmsService.validateCaptcha(code.trim(), "ApiRegister_" + phoneNumber);
        if (!"success".equals(validateCaptcha)) throw new ServiceException(validateCaptcha);
        boolean regFlag = userService.save(ttUser);
        if (!regFlag) throw new ServiceException("注册失败,请联系系统管理人员");
        else {
            if (BigDecimal.ZERO.compareTo(registerRedPacket) < 0) {
                userService.insertUserAmountRecords(ttUser.getUserId(), TtAccountRecordType.INPUT, TtAccountRecordSource.REGIST_AWARD, registerRedPacket, ttUser.getAccountAmount());
            }
            AsyncManager.me().execute(AsyncFactory.recordLogininfor("api_" + ttUser.getUserName(), Constants.REGISTER, MessageUtils.message("user.register.success")));
        }

        //异步处理百度推广渠道数据(存在)
//        if (StringUtils.isNotBlank(registerBody.getLogidUrl())){
//            log.info("用户【{}】存在百度推广数据==>【{}】",ttUser.getUserId(), registerBody.getLogidUrl());
//            threadPoolTaskExecutor.execute(()->{
//                log.info("开始异步处理注册用户【{}】百度推广数据==>【{}】",ttUser.getUserId(), registerBody.getLogidUrl());
//                bdPromotionProcess.registerInfo(ttUser.getUserId(), registerBody.getLogidUrl());
//            });
//        }

        return "";
    }

}
