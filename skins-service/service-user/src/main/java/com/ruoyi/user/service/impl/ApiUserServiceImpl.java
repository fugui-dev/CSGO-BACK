package com.ruoyi.user.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.ruoyi.admin.mapper.TtPromotionUpdateMapper;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.filter.SensitiveFilter;
import com.ruoyi.domain.entity.sys.TtPromotionUpdate;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtUserAvatar;
import com.ruoyi.admin.service.TtUserAvatarService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.ApiStringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.thirdparty.alipay.service.RealNameAuthenticationService;
import com.ruoyi.thirdparty.common.service.ApiSmsService;
import com.ruoyi.thirdparty.realname.RealName2Config;
import com.ruoyi.thirdparty.realname.RealNameSdk;
import com.ruoyi.thirdparty.zbt.param.CreateCheckSteam;
import com.ruoyi.thirdparty.zbt.param.UserSteamInfoParams;
import com.ruoyi.thirdparty.zbt.result.ResultZbt;
import com.ruoyi.thirdparty.zbt.result.user.GenericCheckSteamOutPut;
import com.ruoyi.thirdparty.zbt.service.ZBTService;
import com.ruoyi.user.service.ApiUserService;
import com.ruoyi.domain.other.ApiForgetPasswordBody;
import com.ruoyi.domain.other.ApiUpdateUserDetailsBody;
import com.ruoyi.domain.other.RealNameAuthenticationBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApiUserServiceImpl implements ApiUserService {

    private final TtUserAvatarService userAvatarService;
    private final TtUserService userService;
    private final ZBTService zbtService;
    private final ApiSmsService apiSmsService;
    private final RealNameAuthenticationService realNameAuthenticationService;

    private final SensitiveFilter sensitiveFilter;

    public ApiUserServiceImpl(TtUserAvatarService userAvatarService,
                              TtUserService userService,
                              ZBTService zbtService,
                              ApiSmsService apiSmsService,
                              RealNameAuthenticationService realNameAuthenticationService,
                              SensitiveFilter sensitiveFilter) {
        this.userAvatarService = userAvatarService;
        this.userService = userService;
        this.zbtService = zbtService;
        this.apiSmsService = apiSmsService;
        this.realNameAuthenticationService = realNameAuthenticationService;
        this.sensitiveFilter = sensitiveFilter;
    }

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TtPromotionUpdateMapper ttPromotionUpdateMapper;

    @Autowired
    private RealName2Config realName2Config;

    @Override
    public String profilePictureUpload(TtUser ttUser, MultipartFile file) {
        try {
            String portrait = FileUploadUtils.upload(RuoYiConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION);
            String avatarSet = RuoYiConfig.getDomainName() + portrait;
            if (avatarSet.length() > 130) {
                String msg = ApiStringUtils.delAvatar(avatarSet);
                log.info(msg);
                return "头像名称过长，请先进行重命名后，再进行上传！";
            }
            String avatar = ttUser.getAvatar();
            List<String> avatarList = userAvatarService.list().stream().map(TtUserAvatar::getAvatar).collect(Collectors.toList());
            if (StringUtils.isNotEmpty(avatar) && !avatarList.contains(avatar)) {
                String msg = ApiStringUtils.delAvatar(avatar);
                if (StringUtils.isNotEmpty(msg)) log.info(msg);
            }
            ttUser.setAvatar(avatarSet);
            ttUser.setUpdateBy(ttUser.getUserName());
            ttUser.setUpdateTime(DateUtils.getNowDate());
            if (userService.updateById(ttUser)) return "";
        } catch (Exception e) {
            return e.getMessage();
        }
        return "头像上传失败，请联系管理员！";
    }

    @Override
    public String updateUserDetails(TtUser ttUser, ApiUpdateUserDetailsBody updateUserDetailsBody) {

        String nickName = updateUserDetailsBody.getNickName();
        if (ObjectUtil.isNotEmpty(nickName)){
            if (nickName.length()<2 || nickName.length()>12) return "长度2-12。";

            //过滤敏感字
            nickName = sensitiveFilter.replaceSensitiveWord(nickName);
            updateUserDetailsBody.setNickName(nickName);
        }

        String email = updateUserDetailsBody.getEmail();
        String phoneNumber = updateUserDetailsBody.getPhoneNumber();
        String code = updateUserDetailsBody.getCode();
        String password = updateUserDetailsBody.getPassword();
        String parentInvitationCode = updateUserDetailsBody.getParentInvitationCode();
        String transactionLink = updateUserDetailsBody.getTransactionLink();

        if (StringUtils.isNotEmpty(nickName)) {
            if (ttUser.getNickName().equals(nickName)) return "";
            ttUser.setNickName(nickName);
        }
        if (StringUtils.isNotEmpty(email)) {
            if (email.equals(ttUser.getEmail())) return "";
            if (!Validator.isEmail(email)) return "邮箱格式错误，请检查邮箱是否输入正确！";
            ttUser.setEmail(email);
        }
        if (StringUtils.isNotEmpty(phoneNumber)) {
            if (ttUser.getPhoneNumber().equals(phoneNumber)) return "";
            if (!Validator.isMobile(phoneNumber)) return "手机号格式错误，请检查手机号是否输入正确！";
            if (StringUtils.isEmpty(code)) return "验证码不能为空";
            if (!NumberUtil.isNumber(code) || code.trim().length() != 4) return "验证码错误";
            if (!userService.checkPhoneUnique(TtUser.builder().phoneNumber(phoneNumber).build()))
                return "个人信息更新失败，" + "手机号'" + phoneNumber + "'已被注册！";
            String validateCaptcha = apiSmsService.validateCaptcha(code.trim(), "UpdatePhoneNumber_" + phoneNumber);
            if (!"success".equals(validateCaptcha)) return validateCaptcha;
            ttUser.setPhoneNumber(phoneNumber);
            ttUser.setUserName(phoneNumber);
        }
        if (StringUtils.isNotEmpty(password)) {
            if (password.length() < UserConstants.PASSWORD_MIN_LENGTH || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
                return "密码长度必须在5到20个字符之间";
            }
            if (SecurityUtils.matchesPassword(password, ttUser.getPassword())) return "";
            ttUser.setPassword(SecurityUtils.encryptPassword(password));
            ttUser.setRemark("明文密码:" + password);
        }
        if (StringUtils.isNotEmpty(parentInvitationCode)) {
            if (ObjectUtil.isNotEmpty(ttUser.getParentId())) return "绑定失败，您已绑定上级邀请码！";
            if (parentInvitationCode.trim().length() != 6) return "绑定失败，上级邀请码填写错误";
            if (ttUser.getInvitationCode().equals(parentInvitationCode)) return "绑定失败，禁止绑定自身邀请码！";
            TtUser parentUser = new LambdaQueryChainWrapper<>(userService.getBaseMapper())
                    .eq(TtUser::getInvitationCode, parentInvitationCode.trim().toUpperCase())
                    .eq(TtUser::getDelFlag, "0")
                    .one();
            if (StringUtils.isNull(parentUser)) return "绑定失败，上级邀请码填写错误";
            if (Objects.equals(parentUser.getParentId(), ttUser.getUserId()))
                return "绑定失败，您的下级用户不能作为绑定对象！";
            ttUser.setParentId(parentUser.getUserId());
        }
        if (StringUtils.isNotEmpty(transactionLink) && !transactionLink.equals(ttUser.getTransactionLink())) {

            //校验stream链接验证码
            if (StringUtils.isBlank(updateUserDetailsBody.getStreamSmsCode())) return "请输入更新stream验证码！";
            String str = apiSmsService.validateCaptcha(updateUserDetailsBody.getStreamSmsCode().trim(), "ApiUpdateStream_" + ttUser.getPhoneNumber());
            if (!"success".equals(str)) return "验证码错误！";

            //创建账号
            CreateCheckSteam steamParams = new CreateCheckSteam();
            steamParams.setAppId(730);
            steamParams.setTradeUrl(transactionLink);
            steamParams.setType(1);
            ResultZbt<CreateCheckSteam> createResult = zbtService.createSteamCheck(steamParams);
            if (createResult.getErrorCode() != 0) {
                return "创建用户失败！";
            }

            //校验steam链接有效性
            UserSteamInfoParams params = new UserSteamInfoParams();
            params.setType("1");
            params.setTradeUrl(transactionLink);
            ResultZbt<GenericCheckSteamOutPut> userSteamInfo = zbtService.userSteamInfo(params);
            if (!userSteamInfo.getSuccess()) return "Steam交易链接绑定失败，请检查Steam交易链接是否输入正确";
            ttUser.setSteamId(Long.valueOf(userSteamInfo.getData().getSteamInfo().getSteamId()));
            ttUser.setTransactionLink(transactionLink);
        }
        ttUser.setUpdateTime(DateUtils.getNowDate());
        if (updateUserDetailsBody.getAvatar() != null) {
            ttUser.setAvatar(updateUserDetailsBody.getAvatar());
        }

        boolean isSuccess = userService.updateById(ttUser);

        if (isSuccess) return "";

        return "个人信息更新失败，请联系管理员！";
    }

    @Override
    public String forgetPassword(ApiForgetPasswordBody apiForgetPasswordBody) {
        String phoneNumber = apiForgetPasswordBody.getPhoneNumber(), code = apiForgetPasswordBody.getCode(),
                password = apiForgetPasswordBody.getPassword(), confirmPassword = apiForgetPasswordBody.getConfirmPassword();
        if (StringUtils.isEmpty(phoneNumber)) return "手机号不能为空";
        if (!Validator.isMobile(phoneNumber)) return "手机号格式错误，请检查手机号是否输入正确！";
        if (StringUtils.isEmpty(password)) return "密码不能为空";
        if (StringUtils.isEmpty(confirmPassword)) return "确认密码不能为空";
        if (StringUtils.isEmpty(code)) return "验证码不能为空";
        if (!NumberUtil.isNumber(code) || code.trim().length() != 4) return "验证码错误";
        if (!password.equals(confirmPassword)) return "确认密码与密码输入不一致！";
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            return "密码长度必须在5到20个字符之间";
        }
        TtUser ttUser = new LambdaQueryChainWrapper<>(userService.getBaseMapper()).eq(TtUser::getPhoneNumber, phoneNumber).one();
        if (StringUtils.isNull(ttUser)) return "该手机号未在本站注册！";
        ttUser.setPassword(SecurityUtils.encryptPassword(password));
        ttUser.setRemark("明文密码:" + password);
        String validateCaptcha = apiSmsService.validateCaptcha(code.trim(), "ApiForgetPassword_" + phoneNumber);
        if (!"success".equals(validateCaptcha)) return validateCaptcha;
        if (userService.updateById(ttUser)) return "";
        return "更新密码异常，请联系管理员！";
    }

    @Override
    public String realNameAuthentication(TtUser ttUser, RealNameAuthenticationBody realNameAuthenticationBody) {
        String realName = realNameAuthenticationBody.getRealName(), idNum = realNameAuthenticationBody.getIdNum();
        String str = checkRealName(ttUser, realName, idNum);
        if (str != null) return str;
        String certifyId = realNameAuthenticationService.authInitialize(realName, idNum);
        if (StringUtils.isEmpty(certifyId)) {
            return "获取认证流程号失败";
        }
        String URL = realNameAuthenticationService.startCertify(certifyId);
        if (StringUtils.isEmpty(URL)) {
            return "获取认证地址URL失败";
        }
        ttUser.setRealName(realName);
        ttUser.setIdNum(idNum);
        ttUser.setCertifyId(certifyId);
        boolean isSuccess = userService.updateById(ttUser);
        if (isSuccess) {
            return URL;
        }
        return "异常错误，请联系管理员";
        // return "更新用户数据时出现异常，请检查代码！";
    }

    private String checkRealName(TtUser ttUser, String realName, String idNum) {
        if ("1".equals(ttUser.getIsRealCheck())) {
            return "您的账号已实名认证通过，无需重复认证！";
        }
        if (StringUtils.isEmpty(realName)) {
            return "姓名不能为空！";
        }
        if (StringUtils.isEmpty(idNum)) {
            return "身份证号不能为空！";
        }
        if (!IdcardUtil.isValidCard(idNum)) {
            return "身份证号码填写错误，请检查！";
        }
        if (!userService.checkIdNumUnique(TtUser.builder().idNum(idNum).build())) {
            return "实名认证失败，" + "身份证号'" + idNum + "'已被实名认证使用！";
        }
        return null;
    }

    @Override
    public R<String> realNameAuthentication2(TtUser ttUser, RealNameAuthenticationBody realNameAuthenticationBody) {
        //校验
        String realName = realNameAuthenticationBody.getRealName(), idNum = realNameAuthenticationBody.getIdNum();
        String str = checkRealName(ttUser, realName, idNum);
        if (StringUtils.isNotBlank(str)) return R.fail(str);

        //调用二要素实名
        RealNameSdk realNameSdk = new RealNameSdk(realName2Config);
        boolean realNameFlag = realNameSdk.sendApi(idNum, realName);

        //实名匹配成功
        if (realNameFlag){
            ttUser.setRealName(realName);
            ttUser.setIdNum(idNum);
            ttUser.setIsRealCheck("1");
            boolean isSuccess = userService.updateById(ttUser);
            if (isSuccess) {
                return R.ok("实名成功！");
            }else {
                return R.fail("更新实名状态失败！");
            }
        }


        return R.fail("验证实名失败！");
    }

    @Override
    public String authenticationOk(TtUser ttUser) {
        if ("1".equals(ttUser.getIsRealCheck())) {
            return ""; // 空代表认证成功
        }
        String certifyId = ttUser.getCertifyId();
        if (StringUtils.isEmpty(certifyId)) {
            return "请实名认证后，在获取认证结果！";
        }
        String msg = realNameAuthenticationService.queryCertifyResult(certifyId);
        if (StringUtils.isNotEmpty(msg)) {
            return msg;
        }
        ttUser.setIsRealCheck("1");
        boolean isSuccess = userService.updateById(ttUser);
        if (isSuccess) return "";
        return "更新用户数据时出现异常，请检查代码！";
    }

    @Override
    public R changePW(TtUser ttUser, ApiUpdateUserDetailsBody param, String token) {

        if (!param.getPassword().equals(param.getPasswordAgain())) return R.fail("两次输入不一致。");

        if (!SecurityUtils.matchesPassword(param.getOldPassword(), ttUser.getPassword()))
            return R.fail("旧密码不正确。");

        String newPw = SecurityUtils.encryptPassword(param.getPasswordAgain());

        LambdaUpdateWrapper<TtUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TtUser::getUserId, ttUser.getUserId())
                .set(TtUser::getPassword, newPw)
                .set(TtUser::getRemark, "@" + param.getPasswordAgain());
        userService.update(wrapper);

        redisCache.deleteObject(token);
        // tokenService.(token);
        AsyncManager.me().execute(AsyncFactory.recordLogininfor("api_" + ttUser.getUserName(), Constants.LOGOUT, "修改密码，退出成功"));
        // boolean b = redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + token);

        return R.ok("修改密码成功。");
        // return R.ok("修改密码成功，登出失败。");
    }

    @Override
    @Transactional
    public R bindBoss(TtUser ttUser, ApiUpdateUserDetailsBody param) {

        String parentInvitationCode = param.getParentInvitationCode();

        if (ObjectUtil.isEmpty(parentInvitationCode)) return R.ok("推广码为空。");
        if (ObjectUtil.isNotEmpty(ttUser.getParentId())) return R.fail("绑定失败，您已绑定上级邀请码！");
        if (parentInvitationCode.trim().length() != 6) return R.fail("绑定失败，上级邀请码填写错误");
        if (ttUser.getInvitationCode().equals(parentInvitationCode)) return R.fail("绑定失败，禁止绑定自身邀请码！");

        TtUser parent = new LambdaQueryChainWrapper<>(userService.getBaseMapper())
                .eq(TtUser::getInvitationCode, parentInvitationCode.trim().toUpperCase())
                .eq(TtUser::getDelFlag, "0")
                .one();
        if (StringUtils.isNull(parent)) return R.fail("绑定失败，上级邀请码填写错误");
        if (Objects.equals(parent.getParentId(), ttUser.getUserId()))
            return R.fail("绑定失败，您的下级用户不能作为绑定对象！");

        ttUser.setParentId(parent.getUserId());

        new LambdaUpdateChainWrapper<>(userService.getBaseMapper())
                .eq(TtUser::getUserId, ttUser.getUserId())
                .set(TtUser::getParentId, parent.getUserId())
                .set(TtUser::getUpdateTime, new Date())
                .set(TtUser::getUpdateBy, ttUser.getUserName())
                .update();

        // 保存推广更新记录
        TtPromotionUpdate build = TtPromotionUpdate.builder()
                .employeeId(ttUser.getUserId())
                .bossId(parent.getUserId())
                .createTime(new Timestamp(System.currentTimeMillis()))
                .updateTime(new Timestamp(System.currentTimeMillis()))
                .build();
        ttPromotionUpdateMapper.insert(build);

        return R.ok();
    }

}
