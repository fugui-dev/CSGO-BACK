package com.ruoyi.thirdparty.common.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ApiSmsBody {
    private String phoneNumber;
    @ApiModelProperty("1注册 2登录 3更换手机号 4找回密码 5更新stream链接")
    private String type;
}
