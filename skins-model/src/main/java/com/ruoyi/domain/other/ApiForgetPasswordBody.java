package com.ruoyi.domain.other;

import lombok.Data;

@Data
public class ApiForgetPasswordBody {

    private String phoneNumber;

    private String code;

    private String password;

    private String confirmPassword;
}
