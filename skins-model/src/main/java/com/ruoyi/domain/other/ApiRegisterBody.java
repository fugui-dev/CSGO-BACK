package com.ruoyi.domain.other;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class ApiRegisterBody {

    @Length(min = 2,max = 12)
    private String nickName;

    private String phoneNumber;

    private String password;

    private String parentInvitationCode;

    private String code;

    @ApiModelProperty("百度推广线索url（注册url带百度bd_vid）")
    private String logidUrl;
}
