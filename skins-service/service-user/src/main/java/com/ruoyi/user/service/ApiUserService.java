package com.ruoyi.user.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.ApiForgetPasswordBody;
import com.ruoyi.domain.other.ApiUpdateUserDetailsBody;
import com.ruoyi.domain.other.RealNameAuthenticationBody;
import org.springframework.web.multipart.MultipartFile;

public interface ApiUserService {

    String profilePictureUpload(TtUser ttUser, MultipartFile file);

    String updateUserDetails(TtUser ttUser, ApiUpdateUserDetailsBody updateUserDetailsBody);

    String forgetPassword(ApiForgetPasswordBody apiForgetPasswordBody);

    String realNameAuthentication(TtUser ttUser, RealNameAuthenticationBody realNameAuthenticationBody);

    R<String> realNameAuthentication2(TtUser ttUser, RealNameAuthenticationBody realNameAuthenticationBody);

    String authenticationOk(TtUser ttUser);

    R<Object> changePW(TtUser ttUser, ApiUpdateUserDetailsBody updateUserDetailsBody,String token);

    R bindBoss(TtUser ttUser, ApiUpdateUserDetailsBody updateUserDetailsBody);

}
