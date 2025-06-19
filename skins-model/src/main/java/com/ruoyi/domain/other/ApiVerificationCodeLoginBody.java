package com.ruoyi.domain.other;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApiVerificationCodeLoginBody {

    @NotNull(message = "手机号不能为空")
    private String phoneNumber;

    @NotNull(message = "验证码不能为空")
    private String code;
}
