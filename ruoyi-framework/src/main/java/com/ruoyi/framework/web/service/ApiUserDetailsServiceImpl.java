package com.ruoyi.framework.web.service;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.entity.UserData;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * 用户验证处理
 */
@Service
public class ApiUserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(ApiUserDetailsServiceImpl.class);
    private final TtUserService ttUserService;
    private final ISysUserService userService;
    private final SysPasswordService passwordService;
    private final SysPermissionService permissionService;

    public ApiUserDetailsServiceImpl(TtUserService ttUserService,
                                     ISysUserService userService,
                                     SysPasswordService passwordService,
                                     SysPermissionService permissionService) {
        this.ttUserService = ttUserService;
        this.userService = userService;
        this.passwordService = passwordService;
        this.permissionService = permissionService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (username.startsWith("api_")) {
            username = username.substring(4);
            TtUser user = new LambdaQueryChainWrapper<>(ttUserService.getBaseMapper()).eq(TtUser::getUserName, username).one();
            if (StringUtils.isNull(user)) {
                log.info("登录用户：{} 不存在.", username);
                throw new ServiceException(MessageUtils.message("user.not.exists"));
            } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
                log.info("登录用户：{} 已被删除.", username);
                throw new ServiceException(MessageUtils.message("user.password.delete"));
            } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
                log.info("登录用户：{} 已被停用.", username);
                throw new ServiceException(MessageUtils.message("user.blocked"));
            }
            // 获取上级用户
            TtUser ParentUser = new LambdaQueryChainWrapper<>(ttUserService.getBaseMapper()).eq(TtUser::getUserId, user.getParentId()).one();

            UserData userData = UserData.builder().build();
            // 对象拷贝
            BeanUtils.copyBeanProp(userData, user);

            if (ObjectUtils.isEmpty(ParentUser)) {
                userData.setParentInvitationCode("");
            } else {
                userData.setParentInvitationCode(ParentUser.getInvitationCode());
            }
            return createLoginUser(userData);
        } else if (username.startsWith("promo_")) {
            username = username.substring(6); // 去掉前缀
            TtUser user = new LambdaQueryChainWrapper<>(ttUserService.getBaseMapper())
                    .eq(TtUser::getUserName, username)
                    .eq(TtUser::getUserType, "01") // 01主播
                    .one();
            if (StringUtils.isNull(user)) {
                log.info("登录用户：{} 不存在.", username);
                throw new ServiceException(MessageUtils.message("user.not.exists"));
            } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
                log.info("登录用户：{} 已被删除.", username);
                throw new ServiceException(MessageUtils.message("user.password.delete"));
            } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
                log.info("登录用户：{} 已被停用.", username);
                throw new ServiceException(MessageUtils.message("user.blocked"));
            }
            UserData userData = UserData.builder().build();
            // 对象拷贝
            BeanUtils.copyBeanProp(userData, user);

            return createLoginUser(userData);
        } else {
            SysUser user = userService.selectUserByUserName(username);
            if (StringUtils.isNull(user)) {
                log.info("登录用户：{} 不存在.", username);
                throw new ServiceException(MessageUtils.message("user.not.exists"));
            } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
                log.info("登录用户：{} 已被删除.", username);
                throw new ServiceException(MessageUtils.message("user.password.delete"));
            } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
                log.info("登录用户：{} 已被停用.", username);
                throw new ServiceException(MessageUtils.message("user.blocked"));
            }

            passwordService.validate(user);

            return createLoginUser(user);
        }
    }

    private UserDetails createLoginUser(UserData userData) {
        return new LoginUser(userData.getUserId().longValue(), userData);
    }

    private UserDetails createLoginUser(SysUser user) {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
