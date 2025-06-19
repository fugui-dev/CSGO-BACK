package com.ruoyi.domain.other;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApiUpdateUserDetailsBody {

    private String nickName;

    private String email;

    private String phoneNumber;
    private String code;

    @NotNull
    private String oldPassword;

    @NotNull
    private String password;

    @NotNull
    private String passwordAgain;

    private String avatar;

    @ApiModelProperty("上级邀请码")
    private String parentInvitationCode;

    private String transactionLink;

    //仅修改stream链接时上传，为空则不校验验证码
    @ApiModelProperty("仅修改stream链接时上传，为空则不校验验证码")
    private String streamSmsCode;

}
